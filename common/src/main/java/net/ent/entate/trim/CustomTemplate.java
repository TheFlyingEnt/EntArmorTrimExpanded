package net.ent.entate.trim;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;

public record CustomTemplate(Identifier pattern, String name, Identifier model) {

    public static final Codec<CustomTemplate> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Identifier.CODEC.fieldOf("pattern").forGetter(CustomTemplate::pattern),
            Codec.STRING.fieldOf("name").forGetter(CustomTemplate::name),
            Identifier.CODEC.fieldOf("model").forGetter(CustomTemplate::model)
    ).apply(instance, CustomTemplate::new));
}
