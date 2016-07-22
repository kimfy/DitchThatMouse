package net.kimfy.ditchthatmouse.client.render;

import net.kimfy.ditchthatmouse.client.GuiTraverser;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class RenderHoverEffect
{
    @SubscribeEvent
    public void onDrawScreen(GuiScreenEvent.DrawScreenEvent e)
    {
        GuiButton button = GuiTraverser.INSTANCE.getSelectedButton();
        if (button != null && button.visible)
        {
            Minecraft mc = Minecraft.getMinecraft();
            int mouseX = button.xPosition;
            int mouseY = button.yPosition;
            button.drawButton(mc, mouseX, mouseY);
        }
    }
}