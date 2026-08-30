package net.ent.entate.mixin;

import net.ent.entate.component.ModComponents;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AnvilMenu.class)
public class MixinAnvilMenu {

    @Shadow @Final private DataSlot cost;
    @Shadow private int repairItemCountCost;

    @Unique
    private boolean entate$isGlowInkCombine(AbstractContainerMenu menu) {
        ItemStack base = menu.getSlot(AnvilMenu.INPUT_SLOT).getItem();
        ItemStack addition = menu.getSlot(AnvilMenu.ADDITIONAL_SLOT).getItem();
        return !base.isEmpty()
                && addition.is(Items.GLOW_INK_SAC)
                && base.has(DataComponents.TRIM)
                && !base.getOrDefault(ModComponents.GLOWING_TRIM, Boolean.FALSE);
    }

    @Inject(method = "createResult", at = @At("RETURN"))
    private void entate$glowInkTrim(CallbackInfo ci) {
        AbstractContainerMenu menu = (AbstractContainerMenu) (Object) this;
        if (!entate$isGlowInkCombine(menu)) {
            return;
        }

        ItemStack result = menu.getSlot(AnvilMenu.INPUT_SLOT).getItem().copy();
        result.set(ModComponents.GLOWING_TRIM, true);
        menu.getSlot(AnvilMenu.RESULT_SLOT).set(result);
        this.repairItemCountCost = 1;
        this.cost.set(0);

        menu.broadcastChanges();
    }

    @Inject(method = "mayPickup", at = @At("HEAD"), cancellable = true)
    private void entate$allowFreeGlowPickup(Player player, boolean hasItem, CallbackInfoReturnable<Boolean> cir) {
        AbstractContainerMenu menu = (AbstractContainerMenu) (Object) this;
        ItemStack result = menu.getSlot(AnvilMenu.RESULT_SLOT).getItem();
        if (!result.isEmpty()
                && result.getOrDefault(ModComponents.GLOWING_TRIM, Boolean.FALSE)
                && entate$isGlowInkCombine(menu)) {
            cir.setReturnValue(true);
        }
    }
}
