package com.afollestad.inquiry;

import android.support.annotation.NonNull;

import java.lang.reflect.Constructor;

/**
 * @author Aidan Follestad (afollestad)
 */
class Util {

    public static Object newInstance(@NonNull Class<?> cls) {
        final Constructor ctor = getDefaultConstructor(cls);
        try {
            return ctor.newInstance();
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
}