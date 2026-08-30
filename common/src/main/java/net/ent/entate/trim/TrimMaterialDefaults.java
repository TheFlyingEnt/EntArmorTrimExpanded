package net.ent.entate.trim;

import java.util.List;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.equipment.trim.TrimMaterial;
import net.minecraft.resources.ResourceKey;

public final class TrimMaterialDefaults {

    public record Mapping(Item item, ResourceKey<TrimMaterial> material) {}

    public static final List<Mapping> MAPPINGS = List.of(
        new Mapping(Items.ECHO_SHARD, ModTrimMaterials.SCULK),
        new Mapping(Items.PRISMARINE_SHARD, ModTrimMaterials.PRISMARINE)
    );

    public static void apply(DataComponentMap.Builder builder, HolderLookup.Provider registries, ResourceKey<TrimMaterial> material) {
        registries.lookupOrThrow(Registries.TRIM_MATERIAL)
            .get(material)
            .ifPresent(holder -> builder.set(DataComponents.PROVIDES_TRIM_MATERIAL, holder)
        );
    }

    private TrimMaterialDefaults() {}
}
