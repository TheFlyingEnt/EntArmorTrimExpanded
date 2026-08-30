package net.ent.entate.component;

import com.mojang.serialization.Codec;
import net.ent.entate.Constants;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.Identifier;
import net.minecraft.core.component.DataComponentType;

public final class ModComponents {

    public static final Identifier GLOWING_TRIM_ID =
            Identifier.fromNamespaceAndPath(Constants.MOD_ID, "glowing_trim");

    public static final DataComponentType<Boolean> GLOWING_TRIM =
            DataComponentType.<Boolean>builder()
                    .persistent(Codec.BOOL)
                    .networkSynchronized(ByteBufCodecs.BOOL)
                    .build();

    private ModComponents() {}
}
