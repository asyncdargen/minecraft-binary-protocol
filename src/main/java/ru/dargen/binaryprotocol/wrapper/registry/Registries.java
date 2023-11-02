package ru.dargen.binaryprotocol.wrapper.registry;

import com.google.common.base.Predicates;
import lombok.experimental.UtilityClass;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;

@UtilityClass
public class Registries {

    public final Registry<Material> MATERIAL = Registry.fromKeyedEnum("i", Material.class,
            Predicates.not(Material::isLegacy));
    public final BlockDataRegistry BLOCK_DATA = new BlockDataRegistry();
    public final Registry<EntityType> ENTITY_TYPE = Registry.fromKeyedEnum("h", EntityType.class,
            type -> type != EntityType.UNKNOWN);


}
