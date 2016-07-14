package net.kimfy.ditchthatmouse.util;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ReflectionHelper
{
    @Nullable
    public static Field findField(Class<?> clz, String... fieldNames)
    {
        Class<?> zuper = clz;
        Field field = null;

        for (String fieldName : fieldNames)
        {
            do
            {
                try
                {
                    field = zuper.getDeclaredField(fieldName);
                    field.setAccessible(true);
                    return field;
                }
                catch (NoSuchFieldException e)
                {
                    zuper = zuper.getSuperclass();
                }
            }
            while (zuper != null);
        }
        return null;
    }

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
}