package net.kimfy.ditchthatmouse.client.render;

import net.kimfy.ditchthatmouse.client.ClientProxy;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class RenderButtonOutline
{
    @SubscribeEvent
    public void onDrawScreen(GuiScreenEvent.DrawScreenEvent event)
    {
        if (ClientProxy.KEY_EVENT_HANDLER.selectedButton != null)
        {
            // render outline
        }
    }
}