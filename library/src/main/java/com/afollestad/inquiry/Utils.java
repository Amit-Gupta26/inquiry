package com.afollestad.inquiry;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.Constructor;

/**
 * @author Aidan Follestad (afollestad)
 */
class Utils {

    @SuppressWarnings("unchecked")
    public static <T> T newInstance(@NonNull Class<T> cls) {
        final Constructor ctor = getDefaultConstructor(cls);
        try {
            return (T) ctor.newInstance();
        } catch (Throwable t) {
            t.printStackTrace();
            throw new RuntimeException("Failed to instantiate " + cls.getName() + ": " + t.getLocalizedMessage());
        }
    }

    public static Constructor<?> getDefaultConstructor(@NonNull Class<?> cls) {
        final Constructor[] ctors = cls.getDeclaredConstructors();
        Constructor ctor = null;
        for (Constructor ct : ctors) {
            ctor = ct;
            if (ctor.getGenericParameterTypes().length == 0)
                break;
        }
        if (ctor == null)
            throw new IllegalStateException("No default constructor found for " + cls.getName());
        ctor.setAccessible(true);
        return ctor;
    }

    public static void closeQuietely(@Nullable Closeable c) {
        if (c != null) {
            try {
                c.close();
            } catch (IOException ignored) {
            }
        }
    }
}