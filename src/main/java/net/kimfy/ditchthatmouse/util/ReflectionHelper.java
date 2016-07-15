package net.kimfy.ditchthatmouse.util;

import javax.annotation.Nullable;
import java.lang.reflect.Method;

public class ReflectionHelper
{
    /**
     * Finds any method even if it's only defined in the super class
     */
    @Nullable
    public static Method findMethod(Class<?> clz, String methodName, Class<?>... parameterTypes)
    {
        Class<?> zuper = clz;
        Method method = null;

        do
        {
            try
            {
                method = zuper.getDeclaredMethod(methodName, parameterTypes);
                method.setAccessible(true);
                return method;
            }
            catch (NoSuchMethodException e)
            {
                zuper = zuper.getSuperclass();
            }
        }
        while (zuper != null);
        return null;
    }

    @Nullable
    public static Method findMethod(Class<?> clz, String[] methodNames, Class<?>... parameterTypes)
    {
        Method method = null;
        for (String methodName : methodNames)
        {
            method = findMethod(clz, methodName, parameterTypes);
        }
        return method;
    }
}