package net.ent.entate.component;

import com.mojang.serialization.Codec;
import net.ent.entate.Constants;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.component.DataComponentType;

public final class ModComponents {

    public static final ResourceLocation GLOWING_TRIM_ID =
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "glowing_trim");

    public static final DataComponentType<Boolean> GLOWING_TRIM =
            DataComponentType.<Boolean>builder()
                    .persistent(Codec.BOOL)
                    .networkSynchronized(ByteBufCodecs.BOOL)
                    .build();

    private ModComponents() {}
}
