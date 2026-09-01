package net.ent.entate;

import net.ent.entate.component.ModComponents;
import net.fabricmc.api.ModInitializer;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;

public class EntArmorTrimExpanded implements ModInitializer {

    @Override
    public void onInitialize() {
        Constants.LOG.info("[EntATM] - Fabric Port Detected");
        CommonClass.init();

        Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE,
                ModComponents.GLOWING_TRIM_ID, ModComponents.GLOWING_TRIM);
    }
}
