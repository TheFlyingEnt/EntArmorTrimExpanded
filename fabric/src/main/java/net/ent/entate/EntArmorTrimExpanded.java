package net.ent.entate;

import net.ent.entate.component.ModComponents;
import net.ent.entate.trim.TrimMaterialDefaults;
import net.ent.entate.trim.TrimProviderManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.item.v1.DefaultItemComponentEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;

public class EntArmorTrimExpanded implements ModInitializer {

    @Override
    public void onInitialize() {
        Constants.LOG.info("[EntATM] - Fabric Port Detected");
        CommonClass.init();

        Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE,
                ModComponents.GLOWING_TRIM_ID, ModComponents.GLOWING_TRIM);

        DefaultItemComponentEvents.MODIFY.register(context -> {
            for (TrimMaterialDefaults.Mapping mapping : TrimMaterialDefaults.MAPPINGS) {
                context.modify(mapping.item(), (builder, registries, item) ->
                        TrimMaterialDefaults.apply(builder, registries, mapping.material()));
            }
        });

        // Load data-driven trim providers so datapack materials apply in the smithing table.
        ServerLifecycleEvents.SERVER_STARTING.register(server ->
                TrimProviderManager.reload(server.getResourceManager(), server.registryAccess()));
        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, resourceManager, success) ->
                TrimProviderManager.reload(resourceManager, server.registryAccess()));
    }
}
