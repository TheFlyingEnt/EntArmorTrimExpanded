package net.ent.entate.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.ent.entate.client.EntateRenderTypes;
import net.ent.entate.trim.TrimAnimation;
import net.ent.entate.trim.TrimAnimationManager;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.OverlayTexture;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.armortrim.ArmorTrim;
import net.minecraft.world.item.armortrim.TrimMaterial;
import net.minecraft.world.item.armortrim.TrimPattern;

import net.ent.entate.component.ModComponents;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HumanoidArmorLayer.class)
public abstract class MixinHumanoidArmorLayer {

    @Shadow
    @Final
    private TextureAtlas armorTrimAtlas;

    @Unique
    private static final int ENTATE_FULL_BRIGHT = 0xF000F0;

    @Unique
    private boolean entate$glowing;

    @Inject(
        method =
            "renderArmorPiece(" +
            "Lcom/mojang/blaze3d/vertex/PoseStack;" +
            "Lnet/minecraft/client/renderer/MultiBufferSource;" +
            "Lnet/minecraft/world/entity/LivingEntity;" +
            "Lnet/minecraft/world/entity/EquipmentSlot;" +
            "I" +
            "Lnet/minecraft/client/model/HumanoidModel;" +
            "FFFFFF" +
            ")V",
        at = @At("HEAD")
    )
    private void entate$captureArmor(
        PoseStack poseStack,
        MultiBufferSource bufferSource,
        LivingEntity entity,
        EquipmentSlot slot,
        int packedLight,
        HumanoidModel<?> model,
        float limbSwing,
        float limbSwingAmount,
        float partialTick,
        float ageInTicks,
        float netHeadYaw,
        float headPitch,
        CallbackInfo ci
    ) {
        ItemStack stack = entity.getItemBySlot(slot);

        this.entate$glowing =
                stack.getOrDefault(
                        ModComponents.GLOWING_TRIM,
                        false
                );
    }

    @Inject(
        method =
            "renderTrim(" +
            "Lnet/minecraft/core/Holder;" +
            "Lcom/mojang/blaze3d/vertex/PoseStack;" +
            "Lnet/minecraft/client/renderer/MultiBufferSource;" +
            "I" +
            "Lnet/minecraft/world/item/armortrim/ArmorTrim;" +
            "Lnet/minecraft/client/model/Model;" +
            "Z" +
            ")V",
        at = @At("HEAD"),
        cancellable = true
    )
    private void entate$renderTrim(Holder<ArmorMaterial> armorMaterial, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, ArmorTrim trim, Model model, boolean innerTexture, CallbackInfo ci) {
        TrimAnimation animation =
                entate$getAnimation(trim);

        ResourceLocation baseTexture =
                innerTexture
                        ? trim.innerTexture(armorMaterial)
                        : trim.outerTexture(armorMaterial);

        boolean decal =
                ((TrimPattern) trim.pattern().value()).decal();

        int light =
                this.entate$glowing
                        ? ENTATE_FULL_BRIGHT
                        : packedLight;

        if (animation == null || animation.isEmpty()) {
            TextureAtlasSprite sprite =
                    this.armorTrimAtlas.getSprite(baseTexture);

            if (sprite != null) {
                VertexConsumer consumer =
                        sprite.wrap(
                                bufferSource.getBuffer(
                                        Sheets.armorTrimsSheet(decal)
                                )
                        );

                model.renderToBuffer(
                        poseStack,
                        consumer,
                        light,
                        OverlayTexture.NO_OVERLAY
                );
            }

            ci.cancel();
            return;
        }

        long now =
                System.currentTimeMillis();

        String currentFrame =
                animation.frameAt(now);

        TextureAtlasSprite currentSprite =
                entate$getFrameSprite(
                        baseTexture,
                        animation.baseFrame(),
                        currentFrame
                );

        if (currentSprite == null) {
            currentSprite =
                    this.armorTrimAtlas.getSprite(baseTexture);
        }

        if (currentSprite == null) {
            ci.cancel();
            return;
        }

        VertexConsumer currentConsumer =
                currentSprite.wrap(
                        bufferSource.getBuffer(
                                Sheets.armorTrimsSheet(decal)
                        )
                );

        model.renderToBuffer(
                poseStack,
                currentConsumer,
                light,
                OverlayTexture.NO_OVERLAY
        );

        if (!animation.interpolate()) {
            ci.cancel();
            return;
        }

        String nextFrame =
                animation.nextFrameAt(now);

        if (currentFrame.equals(nextFrame)) {
            ci.cancel();
            return;
        }

        float blend =
                animation.blendFactor(now);

        if (blend <= 0.0F) {
            ci.cancel();
            return;
        }

        TextureAtlasSprite nextSprite =
                entate$getFrameSprite(
                        baseTexture,
                        animation.baseFrame(),
                        nextFrame
                );

        if (nextSprite == null) {
            ci.cancel();
            return;
        }

        RenderType interpolationType =
                EntateRenderTypes.armorTrimTranslucent(
                        nextSprite.atlasLocation(),
                        decal
                );

        VertexConsumer nextConsumer =
                nextSprite.wrap(
                        bufferSource.getBuffer(
                                interpolationType
                        )
                );

        int alpha =
                Math.round(
                        Math.clamp(blend, 0.0F, 1.0F)
                                * 255.0F
                );

        int color =
                (alpha << 24) |
                        0x00FFFFFF;

        model.renderToBuffer(
                poseStack,
                nextConsumer,
                light,
                OverlayTexture.NO_OVERLAY,
                color
        );

        ci.cancel();
    }

    @Unique
    private TrimAnimation entate$getAnimation(
            ArmorTrim trim
    ) {
        if (trim == null) {
            return null;
        }

        ResourceKey<TrimMaterial> materialKey =
                trim.material()
                        .unwrapKey()
                        .orElse(null);

        if (materialKey == null) {
            return null;
        }

        TrimAnimation animation =
                TrimAnimationManager.get(
                        materialKey.location()
                );

        return animation == null || animation.isEmpty()
                ? null
                : animation;
    }

    @Unique
    private TextureAtlasSprite entate$getFrameSprite(
            ResourceLocation baseTexture,
            String baseFrame,
            String targetFrame
    ) {
        String path =
                baseTexture.getPath();

        if (!path.endsWith(baseFrame)) {
            return null;
        }

        ResourceLocation frameId =
                baseTexture.withPath(
                        path.substring(
                                0,
                                path.length() -
                                        baseFrame.length()
                        ) + targetFrame
                );

        TextureAtlasSprite sprite =
                this.armorTrimAtlas.getSprite(frameId);

        if (sprite == null) {
            return null;
        }

        if (!frameId.equals(
                sprite.contents().name()
        )) {
            return null;
        }

        return sprite;
    }
}
