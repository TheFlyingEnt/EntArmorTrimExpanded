package net.ent.entate.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.ent.entate.component.ModComponents;
import net.ent.entate.trim.TrimAnimation;
import net.ent.entate.trim.TrimAnimationManager;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.armortrim.ArmorTrim;
import net.minecraft.world.item.armortrim.TrimMaterial;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HumanoidArmorLayer.class)
public class MixinHumanoidArmorLayer {

    @Unique
    private static final int entate$FULL_BRIGHT = 0xF000F0;

    @Shadow
    @Final
    private TextureAtlas armorTrimAtlas;

    @Unique
    private boolean entate$glowing;

    @Unique
    private ArmorTrim entate$currentTrim;

    @Unique
    private ResourceLocation entate$currentTexture;

    @Unique
    private PoseStack entate$pose;

    @Unique
    private MultiBufferSource entate$buffer;

    @Unique
    private int entate$light;

    @Unique
    private HumanoidModel<?> entate$model;

    @Unique
    private boolean entate$innerTexture;

    @Inject(
            method = "renderArmorPiece",
            at = @At("HEAD")
    )
    private void entate$capture(
            CallbackInfo ci,
            @Local(argsOnly = true) LivingEntity entity,
            @Local(argsOnly = true) EquipmentSlot slot,
            @Local(argsOnly = true) PoseStack pose,
            @Local(argsOnly = true) MultiBufferSource buffer,
            @Local(argsOnly = true) int light,
            @Local(argsOnly = true) HumanoidModel<?> model
    ) {
        this.entate$pose = pose;
        this.entate$buffer = buffer;
        this.entate$light = light;
        this.entate$model = model;

        ItemStack stack = entity.getItemBySlot(slot);

        if (stack.isEmpty()) {
            this.entate$glowing = false;
            this.entate$currentTrim = null;
            return;
        }

        this.entate$glowing =
                stack.getOrDefault(ModComponents.GLOWING_TRIM, Boolean.FALSE);

        this.entate$currentTrim =
                stack.get(DataComponents.TRIM);
    }


    @Redirect(
            method = "renderTrim(Lnet/minecraft/core/Holder;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/item/armortrim/ArmorTrim;Lnet/minecraft/client/model/HumanoidModel;Z)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/texture/TextureAtlas;getSprite(Lnet/minecraft/resources/ResourceLocation;)Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;"
            )
    )
    private TextureAtlasSprite entate$animateTrimSprite(
            TextureAtlas atlas,
            ResourceLocation texture
    ) {
        this.entate$currentTexture = texture;

        TextureAtlasSprite currentSprite = atlas.getSprite(texture);

        TrimAnimation animation =
                this.entate$animationFor(this.entate$currentTrim);

        if (animation == null) {
            return currentSprite;
        }

        long now = System.currentTimeMillis();

        String currentFrame =
                animation.frameAt(now);

        if (currentFrame.equals(animation.baseFrame())) {
            return currentSprite;
        }

        TextureAtlasSprite animatedSprite =
                this.entate$frameSprite(
                        atlas,
                        texture,
                        animation.baseFrame(),
                        currentFrame
                );

        return animatedSprite != null
                ? animatedSprite
                : currentSprite;
    }

    @ModifyVariable(
            method = "renderTrim(Lnet/minecraft/core/Holder;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/item/armortrim/ArmorTrim;Lnet/minecraft/client/model/HumanoidModel;Z)V",
            at = @At("HEAD"),
            argsOnly = true,
            ordinal = 0
    )
    private int entate$glowTrimLight(int light) {
        return this.entate$glowing
                ? entate$FULL_BRIGHT
                : light;
    }

    @Inject(
            method = "renderTrim(Lnet/minecraft/core/Holder;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/item/armortrim/ArmorTrim;Lnet/minecraft/client/model/HumanoidModel;Z)V",
            at = @At("TAIL")
    )
    private void entate$interpolateTrim(
            Holder<?> armorMaterial,
            PoseStack pose,
            MultiBufferSource buffer,
            int light,
            ArmorTrim trim,
            HumanoidModel<?> model,
            boolean innerTexture,
            CallbackInfo ci
    ) {
        ResourceLocation baseTexture = this.entate$currentTexture;
        this.entate$currentTexture = null;

        if (baseTexture == null) {
            return;
        }

        if (trim == null) {
            return;
        }

        TrimAnimation animation =
                this.entate$animationFor(trim);

        if (animation == null || !animation.interpolate()) {
            return;
        }

        long now = System.currentTimeMillis();

        String currentFrame =
                animation.frameAt(now);

        String nextFrame =
                animation.nextFrameAt(now);

        if (currentFrame.equals(nextFrame)) {
            return;
        }

        float blend =
                animation.blendFactor(now);

        if (blend <= 0.0F) {
            return;
        }

        TextureAtlasSprite nextSprite =
                this.entate$frameSprite(
                        this.armorTrimAtlas,
                        baseTexture,
                        animation.baseFrame(),
                        nextFrame
                );

        if (nextSprite == null) {
            return;
        }

        boolean decal =
                ((net.minecraft.world.item.armortrim.TrimPattern)
                        trim.pattern().value())
                        .decal();

        RenderType trimRenderType =
                Sheets.armorTrimsSheet(decal);

        VertexConsumer consumer =
                nextSprite.wrap(
                        buffer.getBuffer(trimRenderType)
                );

        int renderLight =
                this.entate$glowing
                        ? entate$FULL_BRIGHT
                        : light;

        int alpha =
                Math.round(blend * 255.0F);

        int color =
                (alpha << 24) | 0x00FFFFFF;

        model.renderToBuffer(
                pose,
                consumer,
                renderLight,
                OverlayTexture.NO_OVERLAY,
                color
        );
    }

    @Unique
    private TrimAnimation entate$animationFor(
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
    private TextureAtlasSprite entate$frameSprite(
            TextureAtlas atlas,
            ResourceLocation baseTexture,
            String baseFrame,
            String targetFrame
    ) {
        String path = baseTexture.getPath();

        if (!path.endsWith(baseFrame)) {
            return null;
        }

        ResourceLocation frameId =
                baseTexture.withPath(
                        path.substring(
                                0,
                                path.length() - baseFrame.length()
                        ) + targetFrame
                );

        TextureAtlasSprite frameSprite =
                atlas.getSprite(frameId);

        if (frameSprite == null) {
            return null;
        }

        return frameId.equals(
                frameSprite.contents().name()
        )
                ? frameSprite
                : null;
    }
}
