package me.basiqueevangelist.dynreg.util;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.transformer.meta.MixinMerged;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

public final class ReflectionUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger("DynReg/ReflectionUtil");

    private ReflectionUtil() {

    }

    public static @Nullable MethodHandle findFieldWith(Class<?> klass, String match, String error) {
        try {
            for (var f : klass.getDeclaredFields()) {
                if (!f.getName().contains(match)) continue;

                f.setAccessible(true);

                return MethodHandles.lookup().unreflectGetter(f);
            }
        } catch (IllegalAccessException e) {
            LOGGER.error("{}", error, e);
        }

        return null;
    }

    public static @Nullable MethodHandle findMixinMethodWith(Class<?> klass, String mixin, String match, String error) {
        try {
            for (var m : klass.getDeclaredMethods()) {
                if (!m.getName().contains(match)) continue;

                var annotation = m.getAnnotation(MixinMerged.class);

                if (annotation == null || !annotation.mixin().equals(mixin)) continue;

                m.setAccessible(true);

                return MethodHandles.lookup().unreflect(m);
            }
        } catch (IllegalAccessException e) {
            LOGGER.error("{}", error, e);
        }

        return null;
    }
}
