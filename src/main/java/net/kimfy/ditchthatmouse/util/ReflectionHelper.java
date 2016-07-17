package net.kimfy.ditchthatmouse.util;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Field;
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

    public static Field findField(Class<?> clz, String... fieldNames)
    {
        Field returnField = null;

        for (String fieldName : fieldNames)
        {
            for (Field field : clz.getDeclaredFields())
            {
                if (field.getName().equals(fieldName))
                {
                    field.setAccessible(true);
                    returnField = field;
                    break;
                }
            }
        }

        return returnField;
    }

    /**
     * Finds a field in the given class based on the fieldType, will return the first field of that type.
     * Particularily useful when dealing with unknown field names.
     */
    @Nullable
    public static Field findField(@Nonnull Class<?> clz, @Nonnull Class<?> fieldType)
    {
        Field toFind = null;

        for (Field field : clz.getDeclaredFields())
        {
            Class<?> type = field.getType();
            if (fieldType.equals(type))
            {
                toFind = field;
                toFind.setAccessible(true);
                break;
            }
        }

        return toFind;
    }
}