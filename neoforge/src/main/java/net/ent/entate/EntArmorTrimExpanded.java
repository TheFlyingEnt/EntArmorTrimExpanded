package net.ent.entate;

import net.ent.entate.client.EntSculkTrimClient;
import net.ent.entate.component.ModComponents;
import net.ent.entate.data.EntSculkTrimDataGen;
import net.ent.entate.item.ModItems;
import net.ent.entate.trim.CustomTemplate;
import net.ent.entate.trim.CustomTemplateManager;
import net.ent.entate.trim.TrimMaterialDefaults;
import net.ent.entate.trim.TrimProviderManager;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.ModifyDefaultComponentsEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

@Mod(Constants.MOD_ID)
public class EntArmorTrimExpanded {

    private static Item soulTemplate;
    private static Item customTemplate;

    public EntArmorTrimExpanded(IEventBus eventBus) {

        Constants.LOG.info("[EntATM] - NeoForge Port Detected");
        CommonClass.init();

        eventBus.addListener((RegisterEvent event) ->
                event.register(Registries.DATA_COMPONENT_TYPE, helper -> {
                    helper.register(ModComponents.GLOWING_TRIM_ID, ModComponents.GLOWING_TRIM);
                    helper.register(ModComponents.TRIM_PATTERN_ID, ModComponents.TRIM_PATTERN);
                }));

        eventBus.addListener((RegisterEvent event) ->
                event.register(Registries.ITEM, helper -> {
                    soulTemplate = ModItems.createSoulArmorTrimTemplate(
                            new Item.Properties().setId(ModItems.SOUL_ARMOR_TRIM_SMITHING_TEMPLATE));
                    helper.register(ModItems.SOUL_ARMOR_TRIM_SMITHING_TEMPLATE, soulTemplate);
                    customTemplate = ModItems.createCustomSmithingTemplate(
                            new Item.Properties().setId(ModItems.CUSTOM_SMITHING_TEMPLATE));
                    helper.register(ModItems.CUSTOM_SMITHING_TEMPLATE, customTemplate);
                }));

        eventBus.addListener((BuildCreativeModeTabContentsEvent event) -> {
            if (event.getTabKey() != ModItems.INGREDIENTS_TAB) {
                return;
            }
            ItemStack before = new ItemStack(Items.EXPERIENCE_BOTTLE);
            if (soulTemplate != null) {
                event.insertBefore(before, new ItemStack(soulTemplate),
                        CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            }
            if (customTemplate != null) {
                for (CustomTemplate template : CustomTemplateManager.sorted()) {
                    if (ModItems.DEDICATED_PATTERNS.contains(template.pattern())) {
                        continue;
                    }
                    ItemStack stack = new ItemStack(customTemplate);
                    stack.set(ModComponents.TRIM_PATTERN, template.pattern());
                    event.insertBefore(before, stack, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
                }
            }
        });

        eventBus.addListener((ModifyDefaultComponentsEvent event) -> {
            for (TrimMaterialDefaults.Mapping mapping : TrimMaterialDefaults.MAPPINGS) {
                event.modify(mapping.item(), (builder, registries, item) ->
                        TrimMaterialDefaults.apply(builder, registries, mapping.material()));
            }
        });

        eventBus.addListener((GatherDataEvent.Client event) -> EntSculkTrimDataGen.generate(event));

        NeoForge.EVENT_BUS.addListener(EntArmorTrimExpanded::onAddServerReloadListeners);

        if (FMLEnvironment.getDist() == Dist.CLIENT) {
            EntSculkTrimClient.init(eventBus);
        }
    }

    @SuppressWarnings("deprecation")
    private static void onAddServerReloadListeners(AddServerReloadListenersEvent event) {
        var registries = event.getRegistryAccess();
        event.addListener(TrimProviderManager.ID,
                (ResourceManagerReloadListener) resourceManager ->
                        TrimProviderManager.reload(resourceManager, registries));
    }
}
