package net.kimfy.ditchthatmouse.client.key;

import net.kimfy.ditchthatmouse.DitchThatMouse;
import net.kimfy.ditchthatmouse.util.Counter;
import net.kimfy.ditchthatmouse.util.ReflectionHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.settings.GameSettings;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Keyboard;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class KeyEventHandler
{
    public KeyEventHandler()
    {
    }

    private final Counter                    COUNTER          = new Counter(0, 0);
    private final List<GuiButton>            buttonList       = new LinkedList<>();
    private       List<GameSettings.Options> optionsList      = new LinkedList<>();
    private       GuiScreen                  currentGui       = null;
    private final Comparator<GuiButton>      SORT_VERSO_RECTO = (a, b) ->
    {
        if (a.yPosition <= b.yPosition)
        {
            if (a.xPosition < b.xPosition)
            {
                return -1;
            }
            return 0;
        }
        else
        {
            return 1;
        }
    };

    @SubscribeEvent
    public void initGuiEvent(GuiScreenEvent.InitGuiEvent.Post screenEvent)
    {
        DitchThatMouse.LOGGER.info("Initializing Screen {}", screenEvent.getGui().getClass());
        this.reset();
        this.currentGui = screenEvent.getGui();
        this.getButtonsAndSettings();
        this.COUNTER.setMax(this.buttonList.size() - 1);
        this.buttonList.forEach(button -> DitchThatMouse.LOGGER
                .info("button={}, x={}, y={}", button.displayString, button.xPosition, button.yPosition));
    }

    private final Map<Class<?>, Field> fieldCache = new HashMap<>();

    private void getButtonsAndSettings()
    {
        this.buttonList.addAll(this.currentGui.buttonList);

        // Gui screens that contains row views(Video Settings)
        Field optionsRowList = ReflectionHelper.findField(this.currentGui.getClass(), GuiListExtended.class);
        if (optionsRowList != null)
        {
            Field guiOptionsRowListOptions = null;
            try
            {
                GuiOptionsRowList list = (GuiOptionsRowList) optionsRowList.get(this.currentGui);
                guiOptionsRowListOptions = ReflectionHelper.findField(list.getClass(), List.class);
                List<GuiOptionsRowList.Row> rows = (List<GuiOptionsRowList.Row>) guiOptionsRowListOptions.get(list);

                for (GuiOptionsRowList.Row row : rows)
                {
                    // get buttonA and buttonB
                    this.buttonList.add(row.buttonA);
                    this.buttonList.add(row.buttonB);
                }
            }
            catch (Exception e) // Catch any exception as anything can happen and catching it won't break the game
            {
                DitchThatMouse.LOGGER.error("Something went terribly wrong when retrieving information about the GUI," +
                                            " report this to the mod author if you want");
                e.printStackTrace();
            }
        }
        Collections.sort(this.buttonList, this.SORT_VERSO_RECTO);
    }

    private void reset()
    {
        this.currentGui = null;
        this.buttonList.clear();
        this.selectedButton = null;
        this.firstOperation = true;
        this.COUNTER.reset();
    }

    private long eventTime;

    /**
     * Returns true if it's been 20 milliseconds since last time method was called. Used because
     * {@link KeyEventHandler#onKeyEvent(GuiScreenEvent.KeyboardInputEvent)} was being called multiple times each
     * time a key was pressed. We avoid that by assuring 20ms has passed.
     */
    private boolean enoughTimeHasPassed()
    {
        int maxMilli = 20; // 20ms sweetspot?
        long timeSinceLastEvent = System.currentTimeMillis() - eventTime;
        eventTime = System.currentTimeMillis();

        return timeSinceLastEvent >= maxMilli;
    }

    private enum Operation
    {
        NONE,
        NEXT,
        BACK,
        ADD,
        MINUS
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
        }
        else if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT))
        {
            this.handleOperation(Operation.BACK);
        }
        else if (Keyboard.isKeyDown(Keyboard.KEY_RETURN))
        {
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
        // TODO: Implement properly
        else if (Keyboard.isKeyDown(Keyboard.KEY_ADD))
        {
            this.handleOperation(Operation.ADD);
            boolean isButtonASlider = this.selectedButton instanceof GuiOptionSlider;
            if (isButtonASlider)
            {
                ((GuiOptionSlider) this.selectedButton).sliderValue -= .1F;
            }
        }
        // TODO: Implement properly
        else if (Keyboard.isKeyDown(Keyboard.KEY_MINUS))
        {
            this.handleOperation(Operation.MINUS);
            boolean isButtonASlider = this.selectedButton instanceof GuiOptionSlider;
            if (isButtonASlider)
            {
                ((GuiOptionSlider) this.selectedButton).sliderValue -= .1F;
            }
        }
    }

    public  GuiButton selectedButton = null;
    private boolean   firstOperation = true;

    private void handleOperation(Operation operation)
    {
        this.getButtonsInCurrentGUI();
        int index = this.getIndex(operation);
        try
        {
            this.selectedButton = this.buttonList.get(index);
        }
        catch (IndexOutOfBoundsException e)
        {
            e.printStackTrace();
        }
        DitchThatMouse.LOGGER.info("Button={}", this.selectedButton.displayString);
    }

    private boolean getButtonsInCurrentGUI()
    {
        GuiScreen screen = Minecraft.getMinecraft().currentScreen;
        if (screen != null)
        {
            this.currentGui = screen;
            if (this.buttonList.size() != screen.buttonList.size())
            {
                this.buttonList.clear();
                this.getButtonsAndSettings();
            }
            Collections.sort(this.buttonList, this.SORT_VERSO_RECTO);
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

        int xPos = button.xPosition;
        int yPos = button.yPosition;
        button.enabled = true;

        try
        {
            method.invoke(gui, xPos, yPos, 0);
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

    /**
     * Returns the index(used to retrieve the next button from {@link this#buttonList}) of the next button/setting based
     * on the given operation.
     */
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