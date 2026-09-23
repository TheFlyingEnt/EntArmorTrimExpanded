package net.ent.entate.trim;

import net.ent.entate.Constants;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.equipment.trim.TrimPattern;

public final class ModTrimPatterns {

    public static final ResourceKey<TrimPattern> SOUL = key("soul");

    private static ResourceKey<TrimPattern> key(String path) {
        return ResourceKey.create(Registries.TRIM_PATTERN,
                Identifier.fromNamespaceAndPath(Constants.MOD_ID, path));
    }

    private ModTrimPatterns() {}
}
