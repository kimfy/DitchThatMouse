package net.kimfy.ditchthatmouse.client.key;

import net.kimfy.ditchthatmouse.DitchThatMouse;
import net.kimfy.ditchthatmouse.client.GuiTraverser;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Keyboard;

public class KeyEventHandler
{
    public KeyEventHandler()
    {}

    private long eventTime;
    private final GuiTraverser GUI_TRAVERSER = GuiTraverser.INSTANCE;

    @SubscribeEvent
    public void initGuiEvent(GuiScreenEvent.InitGuiEvent.Post screenEvent)
    {
        this.GUI_TRAVERSER.initialize(screenEvent);
    }

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

    @SubscribeEvent
    public void onKeyEvent(GuiScreenEvent.KeyboardInputEvent e)
    {
        GuiScreen currentScreen = GUI_TRAVERSER.getCurrentScreen();
        if (!this.enoughTimeHasPassed() || currentScreen instanceof GuiContainer ||
            currentScreen instanceof GuiChat)
        {
            return;
        }

        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT))
        {
            if (Keyboard.isKeyDown(Keyboard.KEY_TAB))
            {
                // Select previous button
                DitchThatMouse.LOGGER.info("Select previous button");
                GUI_TRAVERSER.execute(GuiTraverser.Operation.BACK);
            }

            if (Keyboard.isKeyDown(Keyboard.KEY_ADD))
            {
                // Add 0.1F to button.sliderValue
                DitchThatMouse.LOGGER.info("Add 0.1F to button.sliderValue");
                GUI_TRAVERSER.execute(GuiTraverser.Operation.ADD, 0.1F);
            }
            else if (Keyboard.isKeyDown(Keyboard.KEY_SUBTRACT))
            {
                // Subtract 0.1F from button.sliderValue
                DitchThatMouse.LOGGER.info("Subtract 0.1F from button.sliderValue");
                GUI_TRAVERSER.execute(GuiTraverser.Operation.SUBTRACT, 0.1F);
            }
        }
        else if (Keyboard.isKeyDown(Keyboard.KEY_TAB))
        {
            // Select next button
            DitchThatMouse.LOGGER.info("Select next button");
            GUI_TRAVERSER.execute(GuiTraverser.Operation.NEXT);
        }
        else if (Keyboard.isKeyDown(Keyboard.KEY_RETURN) || Keyboard.isKeyDown(Keyboard.KEY_SPACE))
        {
            // Press Button
            DitchThatMouse.LOGGER.info("Select next button");
            GUI_TRAVERSER.execute(GuiTraverser.Operation.CLICK);
        }
        else if (Keyboard.isKeyDown(Keyboard.KEY_ADD))
        {
            // Add 0.01F to button.sliderValue
            DitchThatMouse.LOGGER.info("Add 0.01F to button.sliderValue");
            GUI_TRAVERSER.execute(GuiTraverser.Operation.ADD, 0.01F);
        }
        else if (Keyboard.isKeyDown(Keyboard.KEY_SUBTRACT))
        {
            // Subtract 0.01F from button.sliderValue
            DitchThatMouse.LOGGER.info("Subtract 0.01F from button.sliderValue");
            GUI_TRAVERSER.execute(GuiTraverser.Operation.SUBTRACT, 0.01F);
        }
    }
}