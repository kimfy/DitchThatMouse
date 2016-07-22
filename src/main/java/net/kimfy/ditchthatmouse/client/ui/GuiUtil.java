package net.kimfy.ditchthatmouse.client.ui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class GuiUtil
{

    public static final Comparator<GuiButton> SORT_LEFT_RIGHT =
            (buttonA, buttonB) -> buttonA.yPosition < buttonB.yPosition
                                  ? -1
                                  : (buttonA.yPosition == buttonB.yPosition) ? Integer
                                          .compare(buttonA.xPosition, buttonB.xPosition) : 1;

    /**
     * Sorts the passed {@code List<GuiButton>} by their position on the screen. From highest y-position and lowest
     * x-position to lowest y-position
     *
     * @param buttonList
     */
    public static void sortButtonList(@Nonnull List<GuiButton> buttonList)
    {
        Collections.sort(buttonList, SORT_LEFT_RIGHT);
    }

    @Nullable
    public static GuiScreen getCurrentGui()
    {
        return Minecraft.getMinecraft().currentScreen;
    }


}