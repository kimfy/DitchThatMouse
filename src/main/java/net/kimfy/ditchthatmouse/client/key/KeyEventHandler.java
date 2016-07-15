package net.kimfy.ditchthatmouse.client.key;

import net.kimfy.ditchthatmouse.DitchThatMouse;
import net.kimfy.ditchthatmouse.util.Counter;
import net.kimfy.ditchthatmouse.util.ReflectionHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Keyboard;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class KeyEventHandler
{
    public KeyEventHandler()
    {
    }

    private final Counter         COUNTER    = new Counter(0, 0);
    private final List<GuiButton> buttonList = new LinkedList<>();

    @SubscribeEvent
    public void initGuiEvent(GuiScreenEvent.InitGuiEvent.Post screenEvent)
    {
        DitchThatMouse.LOGGER.info("Initializing Screen {}", screenEvent.getGui().getClass());
        buttonList.clear();
        this.firstOperation = true;
        this.COUNTER.reset();
        buttonList.addAll(screenEvent.getButtonList());
        this.COUNTER.setMax(buttonList.size() - 1);
    }

    private long eventTime;
    private long timeSinceLastEvent;

    /**
     * Returns true if it's been 20 milliseconds since last time method was called. Used because
     * {@link KeyEventHandler#onKeyEvent(GuiScreenEvent.KeyboardInputEvent)} was being called multiple times each
     * time a key was pressed. We avoid that by assuring 20ms has passed.
     */
    private boolean enoughTimeHasPassed()
    {
        int maxMilli = 20; // 20ms sweetspot?
        timeSinceLastEvent = System.currentTimeMillis() - eventTime; // 0 on first run.
        eventTime = System.currentTimeMillis();

        return timeSinceLastEvent >= maxMilli;
    }

    private enum Operation
    {
        NONE,
        NEXT,
        BACK
    }

    @SubscribeEvent
    public void onKeyEvent(GuiScreenEvent.KeyboardInputEvent e)
    {
        if (!this.enoughTimeHasPassed())
        {
            return;
        }

        if (Keyboard.isKeyDown(Keyboard.KEY_TAB))
        {
            this.handleOperation(Operation.NEXT);
            DitchThatMouse.LOGGER.info("NEXT");
        }
        else if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT))
        {
            this.handleOperation(Operation.BACK);
            DitchThatMouse.LOGGER.info("BACK");
        }
        else if (Keyboard.isKeyDown(Keyboard.KEY_RETURN))
        {
            DitchThatMouse.LOGGER.info("ENTER");
            if (this.selectedButton != null)
            {
                try
                {
                    this.pressButton(e.getGui(), this.selectedButton);
                }
                catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException exception)
                {
                    exception.printStackTrace();
                }
            }
        }
    }

    public  GuiButton selectedButton = null;
    private boolean   firstOperation = true;

    private void handleOperation(Operation operation)
    {
        int numOfButtonsInGui = buttonList.size();
        if (numOfButtonsInGui <= 0 && !this.populateWithButtonsInCurrentGui())
        {
            return;
        }
        int index = this.getIndex(operation);
        this.selectedButton = buttonList.get(index);
        DitchThatMouse.LOGGER.info("Button={}", this.selectedButton.displayString);
    }

    private boolean populateWithButtonsInCurrentGui()
    {
        GuiScreen screen = Minecraft.getMinecraft().currentScreen;
        if (screen != null)
        {
            this.buttonList.addAll(screen.buttonList);
            return true;
        }
        return false;
    }

    private Map<Class<?>, Method> methodCache = new HashMap<>();

    private void pressButton(GuiScreen gui, GuiButton button) throws NoSuchMethodException, InvocationTargetException,
            IllegalAccessException
    {
        Class<?> clz = gui.getClass();
        Method method = methodCache.containsKey(clz)
                        ? methodCache.get(clz)
                        : ReflectionHelper
                                .findMethod(clz, new String[] {"a", "mouseClicked"}, int.class, int.class, int.class);
        if (method == null)
        {
            DitchThatMouse.LOGGER
                    .error("Couldn't find method {}.mouseClicked(int, int, int). Report this to the mod author",
                           clz.getName());
            return;
        }
        else if (!methodCache.containsKey(clz))
        {
            methodCache.put(clz, method);
        }

        int x = button.xPosition;
        int y = button.yPosition;
        button.enabled = true;

        try
        {
            method.invoke(gui, x, y, 0);
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }
        finally
        {
            this.selectedButton = null;
            buttonList.clear();
        }
    }

    private int getIndex(Operation operation)
    {
        if (operation == Operation.NEXT)
        {
            if (!this.firstOperation)
            {
                this.COUNTER.increment(1);
            }
            else
            {
                this.firstOperation = false;
            }
        }
        else if (operation == Operation.BACK)
        {
            this.COUNTER.decrement(1);
        }
        return this.COUNTER.getIndex();
    }
}