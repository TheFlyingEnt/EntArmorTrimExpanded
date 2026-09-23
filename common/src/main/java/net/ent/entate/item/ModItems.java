package net.ent.entate.item;

import net.ent.entate.Constants;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SmithingTemplateItem;

public final class ModItems {

    public static final ResourceKey<Item> SOUL_ARMOR_TRIM_SMITHING_TEMPLATE =
            key("soul_armor_trim_smithing_template");

    public static final ResourceKey<CreativeModeTab> INGREDIENTS_TAB = ResourceKey.create(
            Registries.CREATIVE_MODE_TAB, Identifier.fromNamespaceAndPath("minecraft", "ingredients"));

    public static Item createSoulArmorTrimTemplate(Item.Properties properties) {
        return SmithingTemplateItem.createArmorTrimTemplate(properties.rarity(Rarity.UNCOMMON));
    }

    private static ResourceKey<Item> key(String path) {
        return ResourceKey.create(Registries.ITEM,
                Identifier.fromNamespaceAndPath(Constants.MOD_ID, path));
    }

    private ModItems() {}
}
