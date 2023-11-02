package ru.dargen.binaryprotocol.wrapper;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import lombok.experimental.UtilityClass;
import org.bukkit.NamespacedKey;
import ru.dargen.crowbar.Accessors;
import ru.dargen.crowbar.accessor.ConstructorAccessor;
import ru.dargen.crowbar.accessor.FieldAccessor;
import ru.dargen.crowbar.util.Reflection;

@UtilityClass
public class MinecraftKeyPool {

    public final Class<?> MINECRAFT_KEY_CLASS = Reflection.getClass("net.minecraft.resources.MinecraftKey");

    public final ConstructorAccessor<Object> MINECRAFT_KEY_CONSTRUCTOR = Accessors.invoke()
            .openConstructor(MINECRAFT_KEY_CLASS, String.class);
    public final FieldAccessor<String> MINECRAFT_KEY_NAMESPACE_FIELD = Accessors.unsafe()
            .openField(MINECRAFT_KEY_CLASS, "f", String.class);
    public final FieldAccessor<String> MINECRAFT_KEY_KEY_FIELD = Accessors.unsafe()
            .openField(MINECRAFT_KEY_CLASS, "g", String.class);

    public final BiMap<NamespacedKey, Object> MINECRAFT_KEY_CACHE = HashBiMap.create();
    public final BiMap<Object, NamespacedKey> NAMESPACED_KEY_CACHE = MINECRAFT_KEY_CACHE.inverse();

    public Object asMinecraftKey(NamespacedKey key) {
        return MINECRAFT_KEY_CACHE.computeIfAbsent(key, k ->
                MINECRAFT_KEY_CONSTRUCTOR.newInstance(k.asString()));
    }

    public NamespacedKey asNamespacedKey(Object key) {
        return NAMESPACED_KEY_CACHE.computeIfAbsent(key, k ->
                new NamespacedKey(MINECRAFT_KEY_NAMESPACE_FIELD.getValue(key), MINECRAFT_KEY_KEY_FIELD.getValue(key)));
    }

}
