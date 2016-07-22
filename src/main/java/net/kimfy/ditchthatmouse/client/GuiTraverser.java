package net.kimfy.ditchthatmouse.client;

import net.kimfy.ditchthatmouse.DitchThatMouse;
import net.kimfy.ditchthatmouse.client.ui.GuiUtil;
import net.kimfy.ditchthatmouse.util.Counter;
import net.kimfy.ditchthatmouse.util.ReflectionHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.event.GuiScreenEvent;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class GuiTraverser
{
    public static final GuiTraverser INSTANCE = new GuiTraverser();

    private Map<Class<?>, Method> methodCache = new HashMap<>();
    private final Counter COUNTER;
    private List<GuiButton> buttonList = new LinkedList<>();

    /**
     * Whether or not we're on the first operation. Used to determine which index to use on the first execute() call
     */
    private boolean firstOperation = true;
    @Nullable
    private GuiScreen currentScreen;
    @Nullable
    private GuiButton selectedButton = null;

    public GuiTraverser()
    {
        this.COUNTER = new Counter();
    }

    public void initialize(GuiScreenEvent event)
    {
        this.reset();
        this.setCurrentScreen(event.getGui());
        this.setButtonList(this.getCurrentScreen().buttonList);
        this.COUNTER.setMin(0);
        this.COUNTER.setMax(this.getButtonList().size() - 1);
        this.debug();
    }

    private void debug()
    {
        DitchThatMouse.LOGGER.info("Initializing GUI: {}", this.getCurrentScreen().getClass());
        DitchThatMouse.LOGGER.info("Buttons:");
        this.getButtonList().forEach(button -> DitchThatMouse.LOGGER
                .info("Button: {}, x={}, y={}", button.displayString, button.xPosition, button.yPosition));
    }

    private void reset()
    {
        this.setCurrentScreen(null);
        this.getButtonList().clear();
        this.setSelectedButton(null);
        this.setFirstOperation(true);
        this.COUNTER.reset();
    }

    public void execute(Operation operation)
    {
        int index = this.getIndex(operation);
        try
        {
            this.setSelectedButton(GuiTraverser.getNextValidButton(this, operation, index));
            if (operation == Operation.CLICK)
            {
                this.pressButton();
            }
        }
        catch (IndexOutOfBoundsException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e)
        {
            e.printStackTrace();
        }
    }

    public void execute(Operation operation, float multiplier)
    {
        boolean isButtonASlider = this.getSelectedButton() instanceof GuiOptionSlider;
        if (isButtonASlider)
        {
            GuiOptionSlider button = (GuiOptionSlider) this.getSelectedButton();

            if (operation == Operation.ADD)
            {
                button.sliderValue += multiplier;
            }
            else if (operation == Operation.SUBTRACT)
            {
                button.sliderValue -= multiplier;
            }

            button.sliderValue = MathHelper.clamp_float(button.sliderValue, 0.0F, 1.0F);
            float f = button.options.denormalizeValue(button.sliderValue);
            Minecraft.getMinecraft().gameSettings.setOptionFloatValue(button.options, f);
            button.sliderValue = button.options.normalizeValue(f);
            button.displayString = Minecraft.getMinecraft().gameSettings.getKeyBinding(button.options);
        }

    }

    private void pressButton() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException
    {
        Class<?> clz = this.getCurrentScreen().getClass();
        Method method = this.methodCache.containsKey(clz)
                        ? this.methodCache.get(clz)
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

        int xPos = this.getSelectedButton().xPosition;
        int yPos = this.getSelectedButton().yPosition;

        try
        {
            method.invoke(this.getCurrentScreen(), xPos, yPos, 0);
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }
    }

    private static GuiButton getNextValidButton(final GuiTraverser instance, Operation operation, int index)
    {
        GuiButton button = instance.getButtonList().get(index);
        if (!button.enabled)
        {
            int newIndex = instance.getIndex(operation);
            button = getNextValidButton(instance, operation, newIndex);
        }
        return button;
    }

    /**
     * Returns the index(used to retrieve the next button from {@link this#buttonList}) of the next button/setting based
     * on the given operation.
     */
    private int getIndex(GuiTraverser.Operation operation)
    {
        if (operation == GuiTraverser.Operation.NEXT)
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
        else if (operation == GuiTraverser.Operation.BACK)
        {
            this.COUNTER.decrement(1);
        }
        return this.COUNTER.getIndex();
    }

    private void setButtonList(List<GuiButton> buttonList)
    {
        this.getButtonList().addAll(buttonList);

        // Gui screens that contains row views(Video Settings)
        Field optionsRowList = ReflectionHelper.findField(this.getCurrentScreen().getClass(), GuiListExtended.class);
        if (optionsRowList != null)
        {
            Field guiOptionsRowListOptions;
            try
            {
                GuiOptionsRowList list = (GuiOptionsRowList) optionsRowList.get(this.getCurrentScreen());
                guiOptionsRowListOptions = ReflectionHelper.findField(list.getClass(), List.class);
                List<GuiOptionsRowList.Row> rows = (List<GuiOptionsRowList.Row>) guiOptionsRowListOptions.get(list);

                for (GuiOptionsRowList.Row row : rows)
                {
                    // get buttonA and buttonB
                    this.getButtonList().add(row.buttonA);
                    this.getButtonList().add(row.buttonB);
                }
            }
            catch (Exception e) // Catch any exception as anything can happen and catching it won't break the game
            {
                DitchThatMouse.LOGGER.error("Something went terribly wrong when retrieving information about the GUI," +
                                            " report this to the mod author if you want");
                e.printStackTrace();
            }
        }
        GuiUtil.sortButtonList(this.getButtonList());
    }

    public GuiScreen getCurrentScreen()
    {
        return this.currentScreen;
    }

    private void setCurrentScreen(@Nullable GuiScreen currentScreen)
    {
        this.currentScreen = currentScreen;
    }

    private List<GuiButton> getButtonList()
    {
        return this.buttonList;
    }

    @Nullable
    public GuiButton getSelectedButton()
    {
        return this.selectedButton;
    }

    private void setSelectedButton(@Nullable GuiButton selectedButton)
    {
        this.selectedButton = selectedButton;
    }

    public boolean isFirstOperation()
    {
        return this.firstOperation;
    }

    private void setFirstOperation(boolean firstOperation)
    {
        this.firstOperation = firstOperation;
    }

    public enum Operation
    {
        NONE,
        NEXT,
        BACK,
        ADD,
        SUBTRACT,
        CLICK;
    }
}