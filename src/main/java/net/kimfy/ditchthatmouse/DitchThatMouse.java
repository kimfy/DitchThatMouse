package net.kimfy.ditchthatmouse;

import net.kimfy.ditchthatmouse.client.ClientProxy;
import net.kimfy.ditchthatmouse.util.Constants;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = Constants.MOD_ID, name = Constants.MOD_NAME, version = Constants.MOD_VERSION, clientSideOnly = true)
public class DitchThatMouse
{
    public static final Logger LOGGER = LogManager.getLogger(Constants.MOD_NAME);

    @Mod.Instance
    public static DitchThatMouse INSTANCE;

    @SidedProxy(clientSide = Constants.PROXY_CLIENT)
    public static ClientProxy proxy;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent e)
    {
        proxy.preInit(e);
    }
}