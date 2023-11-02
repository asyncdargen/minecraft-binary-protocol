package ru.dargen.binaryprotocol.wrapper.registry;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.Getter;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;

import java.util.Set;
import java.util.function.Function;

@Getter
public class Registry<T> {

    protected final Object2ObjectMap<NamespacedKey, T> key2ValueMap = new Object2ObjectOpenHashMap<>();
    protected final Object2ObjectMap<T, NamespacedKey> value2KeyMap = new Object2ObjectOpenHashMap<>();

    protected final Int2ObjectMap<NamespacedKey> index2KeyMap = new Int2ObjectOpenHashMap<>();
    protected final Object2IntMap<NamespacedKey> key2IndexMap = new Object2IntOpenHashMap<>();

    public NamespacedKey getKey(T value) {
        return value2KeyMap.get(value);
    }

    public NamespacedKey getKey(int index) {
        return index2KeyMap.get(index);
    }

    public T getValue(NamespacedKey key) {
        return key2ValueMap.get(key);
    }

    public T getValue(int index) {
        return getValue(getKey(index));
    }

    public int indexOf(NamespacedKey key) {
        return key2IndexMap.get(key);
    }

    public int indexOf(T value) {
        return indexOf(getKey(value));
    }

    public Set<T> valueSet() {
        return value2KeyMap.keySet();
    }

    public Set<NamespacedKey> keySet() {
        return key2ValueMap.keySet();
    }

    public Set<Integer> indexSet() {
        return index2KeyMap.keySet();
    }

    public static <T extends Enum<T> & Keyed> Registry<T> fromKeyedEnum(String fieldName, Class<T> enumClass) {
        return fromKeyedEnum(fieldName, enumClass, Predicates.alwaysTrue());
    }

    public static <T extends Enum<T> & Keyed> Registry<T> fromKeyedEnum(String fieldName, Class<T> enumClass, Predicate<T> filter) {
        return fromEnum(fieldName, enumClass, T::getKey, filter);
    }

    public static <T extends Enum<T>> Registry<T> fromEnum(String fieldName, Class<T> enumClass,
                                                           Function<T, NamespacedKey> keyMapper) {
        return fromEnum(fieldName, enumClass, keyMapper, Predicates.alwaysTrue());
    }

    public static <T extends Enum<T>> Registry<T> fromEnum(String fieldName, Class<T> enumClass,
                                                           Function<T, NamespacedKey> keyMapper, Predicate<T> filter) {
        return from(fieldName, Set.of(enumClass.getEnumConstants()), keyMapper, filter);
    }

    public static <T> Registry<T> from(String fieldName, Set<T> values,
                                       Function<T, NamespacedKey> keyMapper, Predicate<T> filter) {
        var wrapper = RegistryWrapper.wrap(fieldName);
        var registry = new Registry<T>();

        values.forEach(value -> {
            if (!filter.test(value)) return;

            var key = keyMapper.apply(value);
            var index = wrapper.indexOf(key);

            registry.key2ValueMap.put(key, value);
            registry.value2KeyMap.put(value, key);

            registry.index2KeyMap.put(index, key);
            registry.key2IndexMap.put(key, index);
        });

        return registry;
    }

}
