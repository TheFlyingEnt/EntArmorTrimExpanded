package net.ent.entate.trim;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import net.ent.entate.Constants;
import net.ent.entate.trim.TrimAnimation;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

public final class TrimAnimationManager {

    public static final Identifier ID = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "trim_animations");
    private static final String DIRECTORY = "trim_animations";
    private static final String SUFFIX = ".json";

    private static volatile Map<Identifier, TrimAnimation> animations = Map.of();

    public static TrimAnimation get(Identifier material) {
        return animations.get(material);
    }

    public static void reload(ResourceManager resourceManager) {
        Map<Identifier, TrimAnimation> loaded = new HashMap<>();
        Map<Identifier, Resource> files =
                resourceManager.listResources(DIRECTORY, id -> id.getPath().endsWith(SUFFIX));

        for (Map.Entry<Identifier, Resource> entry : files.entrySet()) {
            Identifier file = entry.getKey();
            Identifier material = materialIdFromFile(file);
            try (Reader reader = entry.getValue().openAsReader()) {
                JsonElement json = JsonParser.parseReader(reader);
                var parsed = TrimAnimation.CODEC.parse(JsonOps.INSTANCE, json);
                if (parsed.result().isPresent()) {
                    loaded.put(material, parsed.result().get());
                } else {
                    Constants.LOG.error("Invalid trim animation {}: {}", file,
                            parsed.error().map(Object::toString).orElse("unknown error"));
                }
            } catch (Exception e) {
                Constants.LOG.error("Failed to read trim animation {}", file, e);
            }
        }

        animations = Map.copyOf(loaded);
        Constants.LOG.info("Loaded {} animated trim material(s)", loaded.size());
    }

    private static Identifier materialIdFromFile(Identifier file) {
        String path = file.getPath();
        String name = path.substring(DIRECTORY.length() + 1, path.length() - SUFFIX.length());
        return Identifier.fromNamespaceAndPath(file.getNamespace(), name);
    }

    private TrimAnimationManager() {}
}
