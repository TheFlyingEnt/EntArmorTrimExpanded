package net.ent.entate.trim;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import net.ent.entate.Constants;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.trim.TrimMaterial;

public final class TrimProviderManager {

    public static final Identifier ID = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "trim_providers");
    private static final String DIRECTORY = "entate/trim_providers";
    private static final String SUFFIX = ".json";

    private static volatile Map<Item, Holder<TrimMaterial>> providers = Map.of();

    private TrimProviderManager() {}

    public static Holder<TrimMaterial> getHolder(Item item) {
        return providers.get(item);
    }

    public static void reload(ResourceManager resourceManager, HolderLookup.Provider registries) {
        HolderLookup.RegistryLookup<TrimMaterial> materials =
                registries.lookupOrThrow(Registries.TRIM_MATERIAL);

        Map<Item, Holder<TrimMaterial>> loaded = new HashMap<>();
        Map<Identifier, Resource> files =
                resourceManager.listResources(DIRECTORY, id -> id.getPath().endsWith(SUFFIX));

        for (Map.Entry<Identifier, Resource> entry : files.entrySet()) {
            Identifier file = entry.getKey();
            try (Reader reader = entry.getValue().openAsReader()) {
                JsonElement json = JsonParser.parseReader(reader);
                if (!json.isJsonObject()) {
                    Constants.LOG.error("Trim provider {} is not a JSON object", file);
                    continue;
                }
                JsonObject object = json.getAsJsonObject();
                Identifier itemId = readId(object, "item", file);
                Identifier materialId = readId(object, "material", file);
                if (itemId == null || materialId == null) {
                    continue;
                }

                Item item = BuiltInRegistries.ITEM.getOptional(itemId).orElse(null);
                if (item == null) {
                    Constants.LOG.error("Trim provider {} references unknown item '{}'", file, itemId);
                    continue;
                }

                Holder<TrimMaterial> holder = materials
                        .get(ResourceKey.create(Registries.TRIM_MATERIAL, materialId))
                        .orElse(null);
                if (holder == null) {
                    Constants.LOG.error("Trim provider {} references unknown trim material '{}'", file, materialId);
                    continue;
                }

                loaded.put(item, holder);
            } catch (Exception e) {
                Constants.LOG.error("Failed to read trim provider {}", file, e);
            }
        }

        providers = Map.copyOf(loaded);
        Constants.LOG.info("Loaded {} data-driven trim provider(s)", loaded.size());
    }

    private static Identifier readId(JsonObject object, String key, Identifier file) {
        if (!object.has(key) || !object.get(key).isJsonPrimitive()) {
            Constants.LOG.error("Trim provider {} is missing string field '{}'", file, key);
            return null;
        }
        Identifier id = Identifier.tryParse(object.get(key).getAsString());
        if (id == null) {
            Constants.LOG.error("Trim provider {} has invalid identifier for '{}'", file, key);
        }
        return id;
    }
}
