package net.ent.entate.trim;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;

public record TrimAnimation(List<String> frames, int frametime, boolean interpolate) {

    private static final long MS_PER_TICK = 50L;

    public static final Codec<TrimAnimation> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.listOf().fieldOf("frames").forGetter(TrimAnimation::frames),
            Codec.INT.optionalFieldOf("frametime", 1).forGetter(TrimAnimation::frametime),
            Codec.BOOL.optionalFieldOf("interpolate", false).forGetter(TrimAnimation::interpolate)
    ).apply(instance, TrimAnimation::new));

    public boolean isEmpty() {
        return this.frames.isEmpty();
    }

    public String baseFrame() {
        return this.frames.get(0);
    }

    private long frameTimeMs() {
        return Math.max(1L, (long) this.frametime * MS_PER_TICK);
    }

    private int indexAt(long timeMs) {
        return (int) ((timeMs / frameTimeMs()) % this.frames.size());
    }

    public String frameAt(long timeMs) {
        return this.frames.get(indexAt(timeMs));
    }

    public String nextFrameAt(long timeMs) {
        int index = (int) (((timeMs / frameTimeMs()) + 1) % this.frames.size());
        return this.frames.get(index);
    }

    public float blendFactor(long timeMs) {
        long step = frameTimeMs();
        return (timeMs % step) / (float) step;
    }
}
