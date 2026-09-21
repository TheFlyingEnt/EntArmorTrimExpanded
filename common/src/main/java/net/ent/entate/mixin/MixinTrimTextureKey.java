package net.ent.entate.mixin;

import net.ent.entate.trim.TrimRenderState;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(targets = "net.minecraft.client.renderer.entity.layers.EquipmentLayerRenderer$TrimTextureKey")
public class MixinTrimTextureKey {

    @ModifyArg(
            method = "getOrPrepareTexture",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/resources/palette/PalettedTextureManager;getOrPrepare(Lnet/minecraft/resources/Identifier;Lnet/minecraft/resources/Identifier;)Lnet/minecraft/client/resources/palette/PalettedTextureManager$Handle;"
            ),
            index = 0
    )
    private Identifier entate$captureBaseTexture(Identifier baseTexture) {
        TrimRenderState.putBaseTexture(this, baseTexture);
        return baseTexture;
    }
}
