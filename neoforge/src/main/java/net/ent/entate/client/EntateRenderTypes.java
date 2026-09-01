package net.ent.entate.client;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public final class EntateRenderTypes {

    private EntateRenderTypes() {
    }

    public static RenderType armorTrimTranslucent(
            ResourceLocation texture,
            boolean equalDepthTest
    ) {
        RenderType.CompositeState state =
                RenderType.CompositeState.builder()
                        .setShaderState(
                                RenderStateShard.RENDERTYPE_ARMOR_CUTOUT_NO_CULL_SHADER
                        )
                        .setTextureState(
                                new RenderStateShard.TextureStateShard(
                                        texture,
                                        false,
                                        false
                                )
                        )
                        .setTransparencyState(
                                RenderStateShard.TRANSLUCENT_TRANSPARENCY
                        )
                        .setCullState(
                                RenderStateShard.NO_CULL
                        )
                        .setLightmapState(
                                RenderStateShard.LIGHTMAP
                        )
                        .setOverlayState(
                                RenderStateShard.OVERLAY
                        )
                        .setLayeringState(
                                RenderStateShard.VIEW_OFFSET_Z_LAYERING
                        )
                        .setDepthTestState(
                                equalDepthTest
                                        ? RenderStateShard.EQUAL_DEPTH_TEST
                                        : RenderStateShard.LEQUAL_DEPTH_TEST
                        )
                        .createCompositeState(false);

        return RenderType.create(
                "entate_armor_trim_translucent",
                DefaultVertexFormat.NEW_ENTITY,
                VertexFormat.Mode.QUADS,
                1536,
                true,
                true,
                state
        );
    }
}
