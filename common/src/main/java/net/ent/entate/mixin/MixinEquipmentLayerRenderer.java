package net.ent.entate.mixin;

import java.util.function.Function;
import net.ent.entate.component.ModComponents;
import net.ent.entate.trim.TrimAnimation;
import net.ent.entate.trim.TrimAnimationManager;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.layers.EquipmentLayerRenderer;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.EquipmentAssetManager;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.trim.ArmorTrim;
import net.minecraft.world.item.equipment.trim.TrimMaterial;
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
            "Lnet/minecraft/client/renderer/OrderedSubmitNodeCollector;submitModel(Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/rendertype/RenderType;IIILnet/minecraft/client/renderer/texture/TextureAtlasSprite;ILnet/minecraft/client/renderer/feature/ModelFeatureRenderer$CrumblingOverlay;)V";

    @Unique
    private TextureAtlas entate$trimAtlas;

    @Unique
    private boolean entate$glowing;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void entate$captureAtlas(EquipmentAssetManager equipmentAssets, TextureAtlas armorTrimAtlas, CallbackInfo ci) {
        this.entate$trimAtlas = armorTrimAtlas;
    }

    @Unique
    private TextureAtlasSprite entate$frameSprite(Identifier baseName, String baseFrame, String targetFrame) {
        String basePath = baseName.getPath();
        if (!basePath.endsWith(baseFrame)) {
            return null;
        }
        Identifier frameName = baseName.withPath(
                basePath.substring(0, basePath.length() - baseFrame.length()) + targetFrame);
        TextureAtlasSprite frameSprite = this.entate$trimAtlas.getSprite(frameName);
        return (frameSprite != null && frameName.equals(frameSprite.contents().name())) ? frameSprite : null;
    }

    @Redirect(
            method = entate$RENDER_LAYERS,
            at = @At(value = "INVOKE", target = "Ljava/util/function/Function;apply(Ljava/lang/Object;)Ljava/lang/Object;", ordinal = 1)
    )
    private Object entate$animateTrimSprite(Function<Object, Object> trimSpriteLookup, Object key, EquipmentClientInfo.LayerType layerType, ResourceKey<EquipmentAsset> equipmentAssetId, Model<?> model, Object state, ItemStack itemStack) {
        TextureAtlasSprite sprite = (TextureAtlasSprite) trimSpriteLookup.apply(key);

        this.entate$glowing = itemStack != null
                && itemStack.getOrDefault(ModComponents.GLOWING_TRIM, Boolean.FALSE);

        if (itemStack == null || this.entate$trimAtlas == null) {
            return sprite;
        }
        ArmorTrim trim = itemStack.get(DataComponents.TRIM);
        if (trim == null) {
            return sprite;
        }
        ResourceKey<TrimMaterial> materialKey = trim.material().unwrapKey().orElse(null);
        if (materialKey == null) {
            return sprite;
        }
        TrimAnimation animation = TrimAnimationManager.get(materialKey.identifier());
        if (animation == null || animation.isEmpty()) {
            return sprite;
        }

        String targetFrame = animation.frameAt(System.currentTimeMillis());
        if (targetFrame.equals(animation.baseFrame())) {
            return sprite;
        }
        TextureAtlasSprite frameSprite = entate$frameSprite(sprite.contents().name(), animation.baseFrame(), targetFrame);
        return frameSprite != null ? frameSprite : sprite;
    }

    @ModifyArg(
            method = entate$RENDER_LAYERS,
            at = @At(value = "INVOKE", target = entate$SUBMIT_MODEL, ordinal = 2),
            index = 4
    )
    private int entate$glowTrimLight(int lightCoords) {
        return this.entate$glowing ? entate$FULL_BRIGHT : lightCoords;
    }

    @Inject(
            method = entate$RENDER_LAYERS,
            at = @At(value = "INVOKE", target = entate$SUBMIT_MODEL, ordinal = 2, shift = At.Shift.AFTER)
    )
    @SuppressWarnings({"unchecked", "rawtypes"})
    private void entate$interpolateTrim(EquipmentClientInfo.LayerType layerType, ResourceKey<EquipmentAsset> equipmentAssetId, Model model, Object state, ItemStack itemStack, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, Identifier playerTextureOverride, int outlineColor, int order, CallbackInfo ci) {
        if (itemStack == null || this.entate$trimAtlas == null) {
            return;
        }
        ArmorTrim trim = itemStack.get(DataComponents.TRIM);
        if (trim == null) {
            return;
        }
        ResourceKey<TrimMaterial> materialKey = trim.material().unwrapKey().orElse(null);
        if (materialKey == null) {
            return;
        }
        TrimAnimation animation = TrimAnimationManager.get(materialKey.identifier());
        if (animation == null || animation.isEmpty() || !animation.interpolate()) {
            return;
        }

        long now = System.currentTimeMillis();
        String nextFrame = animation.nextFrameAt(now);
        if (nextFrame.equals(animation.frameAt(now))) {
            return;
        }
        float blend = animation.blendFactor(now);
        if (blend <= 0.0F) {
            return;
        }

        Identifier baseSpriteId = trim.layerAssetId(layerType.trimAssetPrefix(), equipmentAssetId);
        TextureAtlasSprite nextSprite = entate$frameSprite(baseSpriteId, animation.baseFrame(), nextFrame);
        if (nextSprite == null) {
            return;
        }

        int light = this.entate$glowing ? entate$FULL_BRIGHT : lightCoords;
        int fadeColor = (Math.round(blend * 255.0F) << 24) | 0x00FFFFFF;
        RenderType renderType = RenderTypes.armorTranslucent(nextSprite.atlasLocation());
        submitNodeCollector.order(order + 4096).submitModel(model, state, poseStack, renderType, light,
                OverlayTexture.NO_OVERLAY, fadeColor, nextSprite, outlineColor, (ModelFeatureRenderer.CrumblingOverlay) null);
    }
}
