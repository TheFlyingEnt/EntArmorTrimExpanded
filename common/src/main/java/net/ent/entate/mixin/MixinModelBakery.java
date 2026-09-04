package net.ent.entate.mixin;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.llamalad7.mixinextras.sugar.Local;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.ent.entate.Constants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemOverride;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ModelBakery.class)
public class MixinModelBakery {

    @Unique
    private static final ResourceLocation entate$TRIM_TYPE = ResourceLocation.withDefaultNamespace("trim_type");

    @Unique
    private static final String entate$ANIM_DIR = "trim_animations";

    @Unique
    private static final String entate$JSON = ".json";

    @Unique
    private Map<ResourceLocation, Float> entate$materialIndices;

    @Inject(
            method = "loadItemModelAndDependencies",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/resources/model/ModelBakery;registerModelAndLoadDependencies(Lnet/minecraft/client/resources/model/ModelResourceLocation;Lnet/minecraft/client/resources/model/UnbakedModel;)V"
            )
    )
    private void entate$injectTrimOverrides(ResourceLocation modelLocation, CallbackInfo ci, @Local UnbakedModel unbakedModel) {
        if (!(unbakedModel instanceof BlockModel blockModel)) {
            return;
        }
        List<ItemOverride> existing = blockModel.getOverrides();
        if (existing.isEmpty() || !entate$isTrimmable(existing)) {
            return;
        }

        Map<ResourceLocation, Float> materials = entate$materials();
        if (materials.isEmpty()) {
            return;
        }

        ResourceManager resourceManager = Minecraft.getInstance().getResourceManager();
        String armor = modelLocation.getPath();
        List<ItemOverride> combined = new ArrayList<>(existing);
        boolean changed = false;

        for (Map.Entry<ResourceLocation, Float> entry : materials.entrySet()) {
            ResourceLocation material = entry.getKey();
            ResourceLocation overlayModel = ResourceLocation.fromNamespaceAndPath(
                    material.getNamespace(), "item/" + armor + "_" + material.getPath() + "_trim");
            if (entate$hasOverrideFor(existing, overlayModel)) {
                continue;
            }
            ResourceLocation overlayFile = ResourceLocation.fromNamespaceAndPath(
                    material.getNamespace(), "models/item/" + armor + "_" + material.getPath() + "_trim" + entate$JSON);
            if (resourceManager.getResource(overlayFile).isEmpty()) {
                continue;
            }
            combined.add(new ItemOverride(overlayModel,
                    List.of(new ItemOverride.Predicate(entate$TRIM_TYPE, entry.getValue()))));
            changed = true;
        }

        if (changed) {
            combined.sort(Comparator.comparingDouble(MixinModelBakery::entate$trimValue));
            ((BlockModelAccessor) blockModel).entate$setOverrides(List.copyOf(combined));
        }
    }

    @Unique
    private static boolean entate$isTrimmable(List<ItemOverride> overrides) {
        for (ItemOverride override : overrides) {
            if (override.getPredicates().anyMatch(p -> p.getProperty().equals(entate$TRIM_TYPE))) {
                return true;
            }
        }
        return false;
    }

    @Unique
    private static boolean entate$hasOverrideFor(List<ItemOverride> overrides, ResourceLocation model) {
        for (ItemOverride override : overrides) {
            if (override.getModel().equals(model)) {
                return true;
            }
        }
        return false;
    }

    @Unique
    private static float entate$trimValue(ItemOverride override) {
        return override.getPredicates()
                .filter(p -> p.getProperty().equals(entate$TRIM_TYPE))
                .map(ItemOverride.Predicate::getValue)
                .findFirst()
                .orElse(Float.NEGATIVE_INFINITY);
    }

    @Unique
    private Map<ResourceLocation, Float> entate$materials() {
        if (this.entate$materialIndices != null) {
            return this.entate$materialIndices;
        }
        Map<ResourceLocation, Float> map = new HashMap<>();
        ResourceManager resourceManager = Minecraft.getInstance().getResourceManager();
        Map<ResourceLocation, Resource> files =
                resourceManager.listResources(entate$ANIM_DIR, id -> id.getPath().endsWith(entate$JSON));
        for (Map.Entry<ResourceLocation, Resource> entry : files.entrySet()) {
            ResourceLocation file = entry.getKey();
            String path = file.getPath();
            String name = path.substring(entate$ANIM_DIR.length() + 1, path.length() - entate$JSON.length());
            try (Reader reader = entry.getValue().openAsReader()) {
                JsonElement json = JsonParser.parseReader(reader);
                if (json.isJsonObject()) {
                    JsonObject obj = json.getAsJsonObject();
                    if (obj.has("item_model_index")) {
                        float index = obj.get("item_model_index").getAsFloat();
                        map.put(ResourceLocation.fromNamespaceAndPath(file.getNamespace(), name), index);
                    }
                }
            } catch (Exception e) {
                Constants.LOG.error("Failed to read trim icon index from {}", file, e);
            }
        }
        this.entate$materialIndices = map;
        return map;
    }
}
