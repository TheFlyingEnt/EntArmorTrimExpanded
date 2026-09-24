package net.ent.entate;

import java.util.ArrayList;
import java.util.List;
import net.ent.entate.component.ModComponents;
import net.ent.entate.item.ModItems;
import net.ent.entate.trim.CustomTemplate;
import net.ent.entate.trim.CustomTemplateManager;
import net.ent.entate.trim.TrimMaterialDefaults;
import net.ent.entate.trim.TrimProviderManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.item.v1.DefaultItemComponentEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class EntArmorTrimExpanded implements ModInitializer {

    @Override
    public void onInitialize() {
        Constants.LOG.info("[EntATM] - Fabric Port Detected");
        CommonClass.init();

        Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE,
                ModComponents.GLOWING_TRIM_ID, ModComponents.GLOWING_TRIM);
        Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE,
                ModComponents.TRIM_PATTERN_ID, ModComponents.TRIM_PATTERN);

        Item soulTemplate = ModItems.createSoulArmorTrimTemplate(
                new Item.Properties().setId(ModItems.SOUL_ARMOR_TRIM_SMITHING_TEMPLATE));
        Registry.register(BuiltInRegistries.ITEM,
                ModItems.SOUL_ARMOR_TRIM_SMITHING_TEMPLATE, soulTemplate);
        Item customTemplate = ModItems.createCustomSmithingTemplate(
                new Item.Properties().setId(ModItems.CUSTOM_SMITHING_TEMPLATE));
        Registry.register(BuiltInRegistries.ITEM,
                ModItems.CUSTOM_SMITHING_TEMPLATE, customTemplate);
        CreativeModeTabEvents.modifyOutputEvent(ModItems.INGREDIENTS_TAB).register(output -> {
            List<ItemStack> templates = new ArrayList<>();
            templates.add(new ItemStack(soulTemplate));
            for (CustomTemplate template : CustomTemplateManager.sorted()) {
                if (ModItems.DEDICATED_PATTERNS.contains(template.pattern())) {
                    continue;
                }
                ItemStack stack = new ItemStack(customTemplate);
                stack.set(ModComponents.TRIM_PATTERN, template.pattern());
                templates.add(stack);
            }
            output.insertBefore(Items.EXPERIENCE_BOTTLE, templates);
        });

        DefaultItemComponentEvents.MODIFY.register(context -> {
            for (TrimMaterialDefaults.Mapping mapping : TrimMaterialDefaults.MAPPINGS) {
                context.modify(mapping.item(), (builder, registries, item) ->
                        TrimMaterialDefaults.apply(builder, registries, mapping.material()));
            }
        });

        ServerLifecycleEvents.SERVER_STARTING.register(server ->
                TrimProviderManager.reload(server.getResourceManager(), server.registryAccess()));
        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, resourceManager, success) ->
                TrimProviderManager.reload(resourceManager, server.registryAccess()));
    }
}
