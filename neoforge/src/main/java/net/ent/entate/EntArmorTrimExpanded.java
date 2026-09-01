package net.ent.entate;

import net.ent.entate.client.EntSculkTrimClient;
import net.ent.entate.component.ModComponents;
import net.ent.entate.data.EntSculkTrimDataGen;
import net.ent.entate.trim.TrimMaterialDefaults;
import net.ent.entate.trim.TrimProviderManager;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
import net.neoforged.neoforge.event.ModifyDefaultComponentsEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

@Mod(Constants.MOD_ID)
public class EntArmorTrimExpanded {

    public EntArmorTrimExpanded(IEventBus eventBus) {

        Constants.LOG.info("[EntATM] - NeoForge Port Detected");
        CommonClass.init();

        eventBus.addListener((RegisterEvent event) ->
                event.register(Registries.DATA_COMPONENT_TYPE, helper ->
                        helper.register(ModComponents.GLOWING_TRIM_ID, ModComponents.GLOWING_TRIM)));

        eventBus.addListener((ModifyDefaultComponentsEvent event) -> {
            for (TrimMaterialDefaults.Mapping mapping : TrimMaterialDefaults.MAPPINGS) {
                event.modify(mapping.item(), (builder, registries, item) ->
                        TrimMaterialDefaults.apply(builder, registries, mapping.material()));
            }
        });

        eventBus.addListener((GatherDataEvent.Client event) -> EntSculkTrimDataGen.generate(event));

        // Load data-driven trim providers so datapack materials apply in the smithing table.
        // AddServerReloadListenersEvent is a game-bus event and re-fires on every datapack reload.
        NeoForge.EVENT_BUS.addListener(EntArmorTrimExpanded::onAddServerReloadListeners);

        if (FMLEnvironment.getDist() == Dist.CLIENT) {
            EntSculkTrimClient.init(eventBus);
        }
    }

    @SuppressWarnings("deprecation") // getRegistryAccess() is the reload-fresh access for this MC version
    private static void onAddServerReloadListeners(AddServerReloadListenersEvent event) {
        var registries = event.getRegistryAccess();
        event.addListener(TrimProviderManager.ID,
                (ResourceManagerReloadListener) resourceManager ->
                        TrimProviderManager.reload(resourceManager, registries));
    }
}
