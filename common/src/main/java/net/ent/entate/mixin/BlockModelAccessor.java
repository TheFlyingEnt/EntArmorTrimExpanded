package net.ent.entate.mixin;

import java.util.List;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemOverride;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BlockModel.class)
public interface BlockModelAccessor {

    @Accessor("overrides")
    List<ItemOverride> entate$getOverrides();

    @Accessor("overrides")
    @Mutable
    void entate$setOverrides(List<ItemOverride> overrides);
}
