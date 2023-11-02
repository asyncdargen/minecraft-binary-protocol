package ru.dargen.binaryprotocol.wrapper.registry;

import org.bukkit.NamespacedKey;
import ru.dargen.binaryprotocol.wrapper.MinecraftKeyPool;
import ru.dargen.crowbar.proxy.ProxyWrappers;
import ru.dargen.crowbar.proxy.wrapper.WrapperProxy;
import ru.dargen.crowbar.proxy.wrapper.annotation.MethodAccessor;
import ru.dargen.crowbar.proxy.wrapper.annotation.ProxiedClass;
import ru.dargen.crowbar.util.Reflection;

import java.util.Set;
import java.util.stream.Collectors;

@ProxiedClass(Object.class)
public interface RegistryWrapper {

    Class<?> BUILTIN_REGISTRIES_CLASS = Reflection.getClass("net.minecraft.core.registries.BuiltInRegistries");
    WrapperProxy<RegistryWrapper, ?> PROXY = ProxyWrappers.builtIn().create(RegistryWrapper.class);

    static RegistryWrapper wrap(String fieldName) {
        return PROXY.wrap(Reflection.getStaticFieldValue(BUILTIN_REGISTRIES_CLASS, fieldName));
    }

    @MethodAccessor(value = "a", inlinedOwner = true,
            owner = @ProxiedClass(className = "net.minecraft.core.Registry"))
    int getIndex(Object registryValue);

    @MethodAccessor(value = "a", inlinedOwner = true,
            owner = @ProxiedClass(className = "net.minecraft.core.Registry"))
    Object getValue(int index);

    @MethodAccessor(value = "a", inlinedOwner = true,
            owner = @ProxiedClass(className = "net.minecraft.core.IRegistry"),
            parameterTypes = @ProxiedClass(className = "net.minecraft.resources.MinecraftKey"))
    Object getValue(Object key);

    @MethodAccessor(value = "b", inlinedOwner = true,
            owner = @ProxiedClass(className = "net.minecraft.core.IRegistry"),
            returnType = @ProxiedClass(className = "net.minecraft.resources.MinecraftKey"))
    Object getKey(Object value);

    @MethodAccessor(value = "e", inlinedOwner = true,
            owner = @ProxiedClass(className = "net.minecraft.core.IRegistry"))
    Set<Object> minecraftKeySet();

    default Set<NamespacedKey> keySet() {
        return minecraftKeySet().stream().map(MinecraftKeyPool::asNamespacedKey).collect(Collectors.toSet());
    }

    default int indexOf(NamespacedKey key) {
        return getIndex(getValue(MinecraftKeyPool.asMinecraftKey(key)));
    }

    default NamespacedKey keyOf(int index) {
        return MinecraftKeyPool.asNamespacedKey(getKey(getValue(index)));
    }

}
