package net.ent.entate;

import net.ent.entate.client.EntSculkTrimClient;
import net.ent.entate.component.ModComponents;
import net.minecraft.core.registries.Registries;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.registries.RegisterEvent;

@Mod(Constants.MOD_ID)
public class EntArmorTrimExpanded {

    public EntArmorTrimExpanded(IEventBus eventBus) {

        Constants.LOG.info("[EntATM] - NeoForge Port Detected");
        CommonClass.init();

        eventBus.addListener((RegisterEvent event) ->
                event.register(Registries.DATA_COMPONENT_TYPE, helper ->
                        helper.register(ModComponents.GLOWING_TRIM_ID, ModComponents.GLOWING_TRIM)));

        if (FMLEnvironment.dist == Dist.CLIENT) {
            EntSculkTrimClient.init(eventBus);
        }
    }
}
