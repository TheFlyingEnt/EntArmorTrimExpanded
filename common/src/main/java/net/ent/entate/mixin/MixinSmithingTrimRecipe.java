package net.ent.entate.mixin;

import net.ent.entate.trim.TrimProviderManager;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.SmithingTrimRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(SmithingTrimRecipe.class)
public class MixinSmithingTrimRecipe {

    @Redirect(
            method = "applyTrim",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;get(Lnet/minecraft/core/component/DataComponentType;)Ljava/lang/Object;",
                    ordinal = 0
            )
    )
    private static Object entate$provideTrimMaterial(ItemStack addition, DataComponentType<?> componentType) {
        Object provided = addition.get(componentType);
        return provided != null ? provided : TrimProviderManager.getHolder(addition.getItem());
    }
}
