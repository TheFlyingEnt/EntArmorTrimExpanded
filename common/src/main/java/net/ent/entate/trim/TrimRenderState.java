package net.ent.entate.trim;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.resources.Identifier;

public final class TrimRenderState {

    private static final Map<Object, Identifier> BASE_TEXTURES = new ConcurrentHashMap<>();

    public static void putBaseTexture(Object trimTextureKey, Identifier baseTexture) {
        BASE_TEXTURES.put(trimTextureKey, baseTexture);
    }

    public static Identifier getBaseTexture(Object trimTextureKey) {
        return BASE_TEXTURES.get(trimTextureKey);
    }

    public static Identifier framePalette(Identifier paletteId, String baseFrame, String targetFrame) {
        return frameTexture(paletteId, baseFrame, targetFrame);
    }

    public static Identifier frameTexture(Identifier textureId, String baseFrame, String targetFrame) {
        String path = textureId.getPath();
        if (!path.endsWith(baseFrame)) {
            return null;
        }
        String swapped = path.substring(0, path.length() - baseFrame.length()) + targetFrame;
        return textureId.withPath(swapped);
    }

    public static Identifier frameBaseTexture(Identifier baseTexture, String frame) {
        String path = baseTexture.getPath();
        int slash = path.lastIndexOf('/');
        String dir = slash >= 0 ? path.substring(0, slash + 1) : "";
        return baseTexture.withPath(dir + frame);
    }

    private TrimRenderState() {}
}
