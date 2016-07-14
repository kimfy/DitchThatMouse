package net.kimfy.ditchthatmouse.client.key;

import net.kimfy.ditchthatmouse.DitchThatMouse;
import net.kimfy.ditchthatmouse.util.Constants;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import org.lwjgl.input.Keyboard;

public class KeyBindings
{
    public static final String     CATEGORY = Constants.MOD_NAME;
    public static final KeyBinding KEY_NEXT = new KeyBinding("Forward", Keyboard.KEY_TAB, CATEGORY);
    public static final KeyBinding KEY_BACK = new KeyBinding("Backwards", Keyboard.KEY_LSHIFT, CATEGORY);

    public static void init()
    {
        registerKeyBinding(KEY_NEXT);
        registerKeyBinding(KEY_BACK);
    }

    public static void registerKeyBinding(KeyBinding keyBinding)
    {
        DitchThatMouse.LOGGER.info("Registering Key Binding: {}", keyBinding.getKeyDescription());
        ClientRegistry.registerKeyBinding(keyBinding);
    }
}