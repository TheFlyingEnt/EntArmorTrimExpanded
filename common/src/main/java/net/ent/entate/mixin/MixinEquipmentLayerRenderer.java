package net.ent.entate.mixin;

import java.util.function.Function;
import net.ent.entate.component.ModComponents;
import net.ent.entate.trim.EntateTrimRenderTypes;
import net.ent.entate.trim.TrimAnimation;
import net.ent.entate.trim.TrimAnimationManager;
import net.ent.entate.trim.TrimPatternAnimationManager;
import net.ent.entate.trim.TrimRenderState;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.layers.EquipmentLayerRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.EquipmentAssetManager;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.client.resources.palette.PalettedTextureManager;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.trim.ArmorTrim;
import net.minecraft.world.item.equipment.trim.TrimMaterial;
import net.minecraft.world.item.equipment.trim.TrimPattern;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EquipmentLayerRenderer.class)
public class MixinEquipmentLayerRenderer {

    @Unique
    private static final int entate$FULL_BRIGHT = 0xF000F0;

    @Unique
    private static final String entate$RENDER_LAYERS =
            "renderLayers(Lnet/minecraft/client/resources/model/EquipmentClientInfo$LayerType;Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lnet/minecraft/world/item/ItemStack;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/resources/Identifier;II)V";

    @Unique
    private static final String entate$SUBMIT_MODEL =
            "Lnet/minecraft/client/renderer/OrderedSubmitNodeCollector;submitModel(Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/rendertype/RenderType;IIILnet/minecraft/client/renderer/texture/UvMapping;I)V";

    @Unique
    private PalettedTextureManager entate$paletteManager;

    @Unique
    private ItemStack entate$stack;

    @Unique
    private boolean entate$glowing;

    @Unique
    private Identifier entate$baseTexture;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void entate$captureManager(EquipmentAssetManager equipmentAssets, PalettedTextureManager paletteManager, CallbackInfo ci) {
        this.entate$paletteManager = paletteManager;
    }

    @Inject(method = entate$RENDER_LAYERS, at = @At("HEAD"))
    private void entate$captureState(EquipmentClientInfo.LayerType layerType, ResourceKey<EquipmentAsset> equipmentAssetId,
            Model<?> model, Object state, ItemStack itemStack, PoseStack poseStack, SubmitNodeCollector submitNodeCollector,
            int lightCoords, Identifier playerTextureOverride, int outlineColor, int order, CallbackInfo ci) {
        this.entate$stack = itemStack;
        this.entate$glowing = itemStack != null && itemStack.getOrDefault(ModComponents.GLOWING_TRIM, Boolean.FALSE);
        this.entate$baseTexture = null;
    }

    @Redirect(
            method = entate$RENDER_LAYERS,
            at = @At(value = "INVOKE", target = "Ljava/util/function/Function;apply(Ljava/lang/Object;)Ljava/lang/Object;", ordinal = 1)
    )
    private Object entate$animateTrimHandle(Function<Object, Object> trimLookup, Object trimTextureKey) {
        Object handle = trimLookup.apply(trimTextureKey);

        this.entate$baseTexture = TrimRenderState.getBaseTexture(trimTextureKey);

        ItemStack itemStack = this.entate$stack;
        if (itemStack == null || this.entate$paletteManager == null || this.entate$baseTexture == null) {
            return handle;
        }
        ArmorTrim trim = itemStack.get(DataComponents.TRIM);
        if (trim == null) {
            return handle;
        }
        Identifier palette = trim.material().value().paletteId();
        if (palette == null) {
            return handle;
        }

        long now = System.currentTimeMillis();
        Identifier base = this.entate$baseTexture;
        boolean changed = false;

        ResourceKey<TrimMaterial> materialKey = trim.material().unwrapKey().orElse(null);
        if (materialKey != null) {
            Identifier swapped = entate$paletteFrame(
                    TrimAnimationManager.get(materialKey.identifier()), palette, now);
            if (swapped != palette) {
                palette = swapped;
                changed = true;
            }
        }

        ResourceKey<TrimPattern> patternKey = trim.pattern().unwrapKey().orElse(null);
        if (patternKey != null) {
            Identifier swapped = entate$baseFrame(
                    TrimPatternAnimationManager.get(patternKey.identifier()), base, now);
            if (!swapped.equals(base)) {
                base = swapped;
                changed = true;
            }
        }

        if (!changed) {
            return handle;
        }
        return this.entate$paletteManager.getOrPrepare(base, palette);
    }

    @Unique
    private static Identifier entate$paletteFrame(TrimAnimation animation, Identifier original, long now) {
        if (animation == null || animation.isEmpty()) {
            return original;
        }
        String frame = animation.frameAt(now);
        if (frame.equals(animation.baseFrame())) {
            return original;
        }
        Identifier swapped = TrimRenderState.frameTexture(original, animation.baseFrame(), frame);
        return swapped != null ? swapped : original;
    }

    @Unique
    private static Identifier entate$nextPaletteFrame(TrimAnimation animation, Identifier original, long now) {
        if (animation == null || animation.isEmpty() || !animation.interpolate()) {
            return null;
        }
        String nextFrame = animation.nextFrameAt(now);
        if (nextFrame.equals(animation.frameAt(now))) {
            return null;
        }
        return TrimRenderState.frameTexture(original, animation.baseFrame(), nextFrame);
    }

    @Unique
    private static Identifier entate$baseFrame(TrimAnimation animation, Identifier original, long now) {
        if (animation == null || animation.isEmpty()) {
            return original;
        }
        return TrimRenderState.frameBaseTexture(original, animation.frameAt(now));
    }

    @Unique
    private static Identifier entate$nextBaseFrame(TrimAnimation animation, Identifier original, long now) {
        if (animation == null || animation.isEmpty() || !animation.interpolate()) {
            return null;
        }
        String nextFrame = animation.nextFrameAt(now);
        if (nextFrame.equals(animation.frameAt(now))) {
            return null;
        }
        return TrimRenderState.frameBaseTexture(original, nextFrame);
    }

    @ModifyArg(
            method = entate$RENDER_LAYERS,
            at = @At(value = "INVOKE", target = entate$SUBMIT_MODEL, ordinal = 1),
            index = 4
    )
    private int entate$glowTrimLight(int lightCoords) {
        return this.entate$glowing ? entate$FULL_BRIGHT : lightCoords;
    }

    @Inject(
            method = entate$RENDER_LAYERS,
            at = @At(value = "INVOKE", target = entate$SUBMIT_MODEL, ordinal = 1, shift = At.Shift.AFTER)
    )
    @SuppressWarnings({"unchecked", "rawtypes"})
    private void entate$interpolateTrim(EquipmentClientInfo.LayerType layerType, ResourceKey<EquipmentAsset> equipmentAssetId,
            Model model, Object state, ItemStack itemStack, PoseStack poseStack, SubmitNodeCollector submitNodeCollector,
            int lightCoords, Identifier playerTextureOverride, int outlineColor, int order, CallbackInfo ci) {
        if (itemStack == null || this.entate$paletteManager == null || this.entate$baseTexture == null) {
            return;
        }
        ArmorTrim trim = itemStack.get(DataComponents.TRIM);
        if (trim == null) {
            return;
        }
        Identifier origPalette = trim.material().value().paletteId();
        if (origPalette == null) {
            return;
        }
        Identifier origBase = this.entate$baseTexture;

        ResourceKey<TrimMaterial> materialKey = trim.material().unwrapKey().orElse(null);
        ResourceKey<TrimPattern> patternKey = trim.pattern().unwrapKey().orElse(null);
        TrimAnimation materialAnim = materialKey != null ? TrimAnimationManager.get(materialKey.identifier()) : null;
        TrimAnimation patternAnim = patternKey != null ? TrimPatternAnimationManager.get(patternKey.identifier()) : null;

        long now = System.currentTimeMillis();

        Identifier toPalette = entate$paletteFrame(materialAnim, origPalette, now);
        Identifier toBase = entate$baseFrame(patternAnim, origBase, now);
        float blend = 0.0F;
        boolean active = false;

        Identifier nextPalette = entate$nextPaletteFrame(materialAnim, origPalette, now);
        if (nextPalette != null) {
            toPalette = nextPalette;
            blend = Math.max(blend, materialAnim.blendFactor(now));
            active = true;
        }
        Identifier nextBase = entate$nextBaseFrame(patternAnim, origBase, now);
        if (nextBase != null) {
            toBase = nextBase;
            blend = Math.max(blend, patternAnim.blendFactor(now));
            active = true;
        }
        if (!active || blend <= 0.0F) {
            return;
        }

        PalettedTextureManager.Handle nextHandle = this.entate$paletteManager.getOrPrepare(toBase, toPalette);
        int light = this.entate$glowing ? entate$FULL_BRIGHT : lightCoords;
        int fadeColor = (Math.round(blend * 255.0F) << 24) | 0x00FFFFFF;

        RenderType renderType = EntateTrimRenderTypes.interpolate(nextHandle.textureLocation());
        submitNodeCollector.order(order + 4096).submitModel(model, state, poseStack, renderType, light,
                OverlayTexture.NO_OVERLAY, fadeColor, nextHandle, outlineColor);
    }
}
