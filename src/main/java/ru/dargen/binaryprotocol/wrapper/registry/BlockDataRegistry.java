package ru.dargen.binaryprotocol.wrapper.registry;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.data.BlockData;

import java.util.concurrent.atomic.AtomicInteger;

public class BlockDataRegistry extends Registry<Material> {

    public BlockDataRegistry() {
        var wrapper = RegistryWrapper.wrap("f");

        var index = new AtomicInteger();
        wrapper.keySet().forEach(key -> {
            var type = Registries.MATERIAL.getValue(key);

            key2ValueMap.put(key, type);
            value2KeyMap.put(type, key);

            key2IndexMap.put(key, index.get());
            index2KeyMap.put(index.getAndIncrement(), key);
        });
    }

    public BlockData getBlockData(NamespacedKey key) {
        return getValue(key).createBlockData();
    }

    public BlockData getBlockData(int index) {
        return getValue(index).createBlockData();
    }

    public int indexOf(BlockData data) {
        return indexOf(data.getMaterial());
    }

    public NamespacedKey getKey(BlockData data) {
        return getKey(data.getMaterial());
    }

}
