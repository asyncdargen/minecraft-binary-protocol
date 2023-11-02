package ru.dargen.binaryprotocol.util;

import lombok.experimental.UtilityClass;
import ru.dargen.crowbar.util.Reflection;

import java.util.Queue;

@UtilityClass
public class MinecraftUtil {

    public final Class<?> MINECRAFT_SERVER_CLASS = Reflection.getClass("net.minecraft.server.MinecraftServer");
    public final Object MINECRAFT_SERVER = Reflection.invokeStaticMethod(Reflection.getMethod(MINECRAFT_SERVER_CLASS, "getServer"));
    public final Queue<Runnable> MINECRAFT_TASKS = Reflection.getFieldValue(
            MINECRAFT_SERVER_CLASS, "processQueue", MINECRAFT_SERVER);

    public void postToMainThread(Runnable runnable) {
        MINECRAFT_TASKS.offer(runnable);
    }

}
