package net.ent.entate.trim;

import java.util.function.Function;
import net.ent.entate.mixin.RenderTypeAccessor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.LayeringTransform;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;

public final class EntateTrimRenderTypes {

    private static final Function<Identifier, RenderType> TRIM_INTERPOLATE = Util.memoize(texture -> {
        RenderSetup setup = RenderSetup.builder(RenderPipelines.ENTITY_TRANSLUCENT)
                .setOitPipelines(RenderPipelines.OIT_ENTITY)
                .withTexture("Sampler0", texture)
                .useLightmap()
                .useOverlay()
                .setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
                .affectsCrumbling()
                .sortOnUpload()
                .setOutline(RenderSetup.OutlineProperty.NONE)
                .createRenderSetup();
        return RenderTypeAccessor.entate$create("entate_trim_interpolate", setup);
    });

    public static RenderType interpolate(Identifier atlasTexture) {
        return TRIM_INTERPOLATE.apply(atlasTexture);
    }

    private EntateTrimRenderTypes() {}
}
