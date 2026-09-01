package net.ent.entate.mixin;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.armortrim.TrimMaterials;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.SmithingRecipeInput;
import net.minecraft.world.item.crafting.SmithingTrimRecipe;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(SmithingTrimRecipe.class)
public class MixinSmithingTrimRecipe {

    @Redirect(
            method = "matches(Lnet/minecraft/world/item/crafting/SmithingRecipeInput;Lnet/minecraft/world/level/Level;)Z",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/crafting/Ingredient;test(Lnet/minecraft/world/item/ItemStack;)Z",
                    ordinal = 2
            )
    )
    private boolean entate$acceptAnyTrimIngredient(Ingredient addition, ItemStack stack, SmithingRecipeInput input, Level level) {
        if (addition.test(stack)) {
            return true;
        }
        return !stack.isEmpty()
                && TrimMaterials.getFromIngredient(level.registryAccess(), stack).isPresent();
    }
}
