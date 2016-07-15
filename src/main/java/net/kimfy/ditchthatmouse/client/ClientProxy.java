package net.kimfy.ditchthatmouse.client;

import net.kimfy.ditchthatmouse.client.key.KeyEventHandler;
import net.kimfy.ditchthatmouse.client.render.RenderHoverEffect;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class ClientProxy
{
    public static final KeyEventHandler KEY_EVENT_HANDLER = new KeyEventHandler();

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent e)
    {
        MinecraftForge.EVENT_BUS.register(KEY_EVENT_HANDLER);
        MinecraftForge.EVENT_BUS.register(new RenderHoverEffect());
    }
}