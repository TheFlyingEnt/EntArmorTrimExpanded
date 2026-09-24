package net.ent.entate.trim;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.ent.entate.Constants;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

public final class CustomTemplateManager {

    public static final Identifier ID = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "custom_templates");
    private static final String DIRECTORY = "custom_templates";
    private static final String SUFFIX = ".json";

    private static volatile Map<Identifier, CustomTemplate> byPattern = Map.of();

    public static CustomTemplate get(Identifier pattern) {
        return byPattern.get(pattern);
    }

    public static List<CustomTemplate> sorted() {
        List<CustomTemplate> list = new ArrayList<>(byPattern.values());
        list.sort(Comparator.comparing(template -> template.pattern().toString()));
        return list;
    }

    public static void reload(ResourceManager resourceManager) {
        byPattern = load(resourceManager);
        Constants.LOG.info("Loaded {} custom smithing template(s)", byPattern.size());
    }

    public static Map<Identifier, CustomTemplate> load(ResourceManager resourceManager) {
        Map<Identifier, CustomTemplate> loaded = new HashMap<>();
        Map<Identifier, Resource> files =
                resourceManager.listResources(DIRECTORY, id -> id.getPath().endsWith(SUFFIX));

        for (Map.Entry<Identifier, Resource> entry : files.entrySet()) {
            Identifier file = entry.getKey();
            try (Reader reader = entry.getValue().openAsReader()) {
                JsonElement json = JsonParser.parseReader(reader);
                var parsed = CustomTemplate.CODEC.parse(JsonOps.INSTANCE, json);
                if (parsed.result().isPresent()) {
                    CustomTemplate template = parsed.result().get();
                    loaded.put(template.pattern(), template);
                } else {
                    Constants.LOG.error("Invalid custom template {}: {}", file,
                            parsed.error().map(Object::toString).orElse("unknown error"));
                }
            } catch (Exception e) {
                Constants.LOG.error("Failed to read custom template {}", file, e);
            }
        }
        return Map.copyOf(loaded);
    }

    private CustomTemplateManager() {}
}
