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

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

public class KeyEventHandler
{
    public KeyEventHandler()
    {
    }

    private final Counter         COUNTER                     = new Counter(0, 0);
    private final List<GuiButton> buttonsInLastInitializedGui = new LinkedList<>();

    @SubscribeEvent
    public void initGuiEvent(GuiScreenEvent.InitGuiEvent.Post screenEvent)
    {
        DitchThatMouse.LOGGER.info("Initializing Screen {}", screenEvent.getGui().getClass());
        buttonsInLastInitializedGui.clear();
        this.firstOperation = true;
        this.COUNTER.reset();
        buttonsInLastInitializedGui.addAll(screenEvent.getButtonList());
        this.COUNTER.setMax(buttonsInLastInitializedGui.size() - 1);
    }

    private int clickCount = 0;
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
            return;

        if (Keyboard.isKeyDown(Keyboard.KEY_TAB))
        {
            this.handleOperation(Operation.NEXT);
            this.clickCount++;
            DitchThatMouse.LOGGER.info("NEXT, clickCount={}", clickCount);
        }
        else if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT))
        {
            this.handleOperation(Operation.BACK);
            this.clickCount++;
            DitchThatMouse.LOGGER.info("BACK, clickCount={}", clickCount);
        }
        else if (Keyboard.isKeyDown(Keyboard.KEY_RETURN))
        {
            this.clickCount++;
            DitchThatMouse.LOGGER.info("ENTER, clickCount={}", clickCount);
            if (this.selectedButton != null)
            {
                try
                {
                    this.pressButton(e.getGui(), this.selectedButton);
                    this.selectedButton = null;
                }
                catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException exception)
                {
                    exception.printStackTrace();
                }
            }
        }
    }

    /*
     *                if (guibutton.mousePressed(this.mc, mouseX, mouseY))
                {
                    net.minecraftforge.client.event.GuiScreenEvent.ActionPerformedEvent.Pre event = new net.minecraftforge.client.event.GuiScreenEvent.ActionPerformedEvent.Pre(this, guibutton, this.buttonList);
                    if (net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(event))
                        break;
                    guibutton = event.getButton();
                    this.selectedButton = guibutton;
                    guibutton.playPressSound(this.mc.getSoundHandler());
                    this.actionPerformed(guibutton);
                    if (this.equals(this.mc.currentScreen))
                        net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.GuiScreenEvent.ActionPerformedEvent.Post(this, event.getButton(), this.buttonList));
                }

     */
    public GuiButton selectedButton = null;
    private boolean firstOperation = true;



    private void handleOperation(Operation operation)
    {
        int numOfButtonsInGui = buttonsInLastInitializedGui.size();
        if (numOfButtonsInGui <= 0 && !this.forcePopulateButtons())
            return;
        int index = this.getIndex(operation);
        this.selectedButton = buttonsInLastInitializedGui.get(index);
        DitchThatMouse.LOGGER.info("Button={}", this.selectedButton.displayString);
    }

    // Returns true if successful
    private boolean forcePopulateButtons()
    {
        Minecraft mc = Minecraft.getMinecraft();
        GuiScreen gui = mc.currentScreen;
        if (gui != null)
        {
            Class<?> clz = gui.getClass();
            Field btns = ReflectionHelper.findField(clz, "buttonList");

            @SuppressWarnings("unchecked") List<GuiButton> buttons = null;
            try
            {
                buttons = (List<GuiButton>) btns.get(gui);
            }
            catch (IllegalAccessException e)
            {
                e.printStackTrace();
            }

            if (buttons != null && !buttons.isEmpty())
            {
                this.buttonsInLastInitializedGui.addAll(buttons);
                return true;
            }
        }
        return false;
    }

    private void pressButton(GuiScreen gui, GuiButton button) throws NoSuchMethodException, InvocationTargetException,
            IllegalAccessException
    {
        Class<?> clz = gui.getClass();
        int x = button.xPosition;
        int y = button.yPosition;
        button.enabled = true;

        try
        {
            Method method = net.kimfy.ditchthatmouse.util.ReflectionHelper
                    .findMethod(clz, "mouseClicked", int.class, int.class, int.class);/*clz
            .getDeclaredMethod
            ("mouseClicked", int.class, int.class, int.class);*/
            method.setAccessible(true);
            method.invoke(gui, x, y, 0);
        }
        finally
        {
            buttonsInLastInitializedGui.clear();
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