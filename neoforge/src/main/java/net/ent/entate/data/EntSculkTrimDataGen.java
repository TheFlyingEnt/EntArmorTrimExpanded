package net.ent.entate.data;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import net.ent.entate.Constants;
import net.ent.entate.trim.ModTrimMaterials;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.equipment.trim.TrimMaterial;
import net.neoforged.neoforge.common.data.ItemTagsProvider;
import net.neoforged.neoforge.common.data.LanguageProvider;
import net.neoforged.neoforge.data.event.GatherDataEvent;

public final class EntSculkTrimDataGen {

    public static void generate(GatherDataEvent.Client event) {
        RegistrySetBuilder registries = new RegistrySetBuilder()
                .add(Registries.TRIM_MATERIAL, ctx -> {
                    ctx.register(ModTrimMaterials.SCULK, new TrimMaterial(
                            Identifier.fromNamespaceAndPath(Constants.MOD_ID, "trim/sculk"),
                            Component.translatable("trim_material.entate.sculk").withColor(0x1CE0C8)));
                    ctx.register(ModTrimMaterials.PRISMARINE, new TrimMaterial(
                            Identifier.fromNamespaceAndPath(Constants.MOD_ID, "trim/prismarine"),
                            Component.translatable("trim_material.entate.prismarine").withColor(0x4FB89C)));
                });
        event.createReloadableRegistryObjects(registries, Set.of(Constants.MOD_ID));

        event.createProvider((PackOutput output, CompletableFuture<HolderLookup.Provider> lookup) ->
                new ItemTagsProvider(output, lookup, Constants.MOD_ID) {
                    @Override
                    protected void addTags(HolderLookup.Provider provider) {
                        tag(ItemTags.TRIM_MATERIALS)
                                .add(Items.ECHO_SHARD.builtInRegistryHolder().key())
                                .add(Items.PRISMARINE_SHARD.builtInRegistryHolder().key());
                    }
                });

        event.createProvider((PackOutput output) ->
                new LanguageProvider(output, Constants.MOD_ID, "en_us") {
                    @Override
                    protected void addTranslations() {
                        add("trim_material.entate.sculk", "Sculk Material");
                        add("trim_material.entate.prismarine", "Prismarine Material");
                    }
                });
    }

    private EntSculkTrimDataGen() {}
}
