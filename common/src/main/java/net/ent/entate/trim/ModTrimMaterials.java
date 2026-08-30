package net.ent.entate.trim;

import net.ent.entate.Constants;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.equipment.trim.TrimMaterial;

public final class ModTrimMaterials {

    public static final ResourceKey<TrimMaterial> SCULK = key("sculk");

    public static final ResourceKey<TrimMaterial> PRISMARINE = key("prismarine");

    //public static final ResourceKey<TrimMaterial> RAW_COPPER = key("raw_copper");
    //Plan for this to be Color Shifting from Brown to green

    //public static final ResourceKey<TrimMaterial> RAW_COPPER = key("glow_squid");
    //Glow Squid Colors. Need an Idea for Animation

    private static ResourceKey<TrimMaterial> key(String path) {
        return ResourceKey.create(Registries.TRIM_MATERIAL,
                Identifier.fromNamespaceAndPath(Constants.MOD_ID, path));
    }

    private ModTrimMaterials() {}
}
