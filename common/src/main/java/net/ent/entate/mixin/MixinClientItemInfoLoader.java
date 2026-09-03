package net.ent.entate.mixin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.ent.entate.Constants;
import net.minecraft.client.renderer.item.ClientItem;
import net.minecraft.client.renderer.item.CuboidItemModelWrapper;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.SelectItemModel;
import net.minecraft.client.renderer.item.properties.select.TrimMaterialProperty;
import net.minecraft.client.resources.model.ClientItemInfoLoader;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.equipment.trim.TrimMaterial;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientItemInfoLoader.class)
public class MixinClientItemInfoLoader {

    @Inject(method = "scheduleLoad", at = @At("RETURN"), cancellable = true)
    private static void entate$injectTrimIcons(ResourceManager resourceManager, Executor executor,
            CallbackInfoReturnable<CompletableFuture<ClientItemInfoLoader.LoadedClientInfos>> cir) {
        CompletableFuture<ClientItemInfoLoader.LoadedClientInfos> future = cir.getReturnValue();
        cir.setReturnValue(future.thenApply(infos -> entate$augment(infos, resourceManager)));
    }

    @Unique
    private static ClientItemInfoLoader.LoadedClientInfos entate$augment(
            ClientItemInfoLoader.LoadedClientInfos infos, ResourceManager resourceManager) {
        try {
            List<Identifier> materials = entate$discoverMaterials(resourceManager);
            if (materials.isEmpty()) {
                return infos;
            }
            Map<Identifier, ClientItem> out = new HashMap<>(infos.contents());
            int added = 0;
            for (Map.Entry<Identifier, ClientItem> entry : infos.contents().entrySet()) {
                ClientItem augmented = entate$augmentItem(entry.getKey(), entry.getValue(), materials, resourceManager);
                if (augmented != null) {
                    out.put(entry.getKey(), augmented);
                    added++;
                }
            }
            if (added > 0) {
                Constants.LOG.info("Injected trim-material icon cases into {} armor item(s)", added);
                return new ClientItemInfoLoader.LoadedClientInfos(out);
            }
            return infos;
        } catch (Exception e) {
            Constants.LOG.error("Failed to inject trim-material icon cases", e);
            return infos;
        }
    }

    @Unique
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static ClientItem entate$augmentItem(Identifier itemId, ClientItem item,
            List<Identifier> materials, ResourceManager resourceManager) {
        ItemModel.Unbaked model = item.model();
        if (!(model instanceof SelectItemModel.Unbaked select)) {
            return null;
        }
        SelectItemModel.UnbakedSwitch<?, ?> unbakedSwitch = select.unbakedSwitch();
        if (!(unbakedSwitch.property() instanceof TrimMaterialProperty)) {
            return null;
        }

        String armor = itemId.getPath();
        Set<ResourceKey<TrimMaterial>> present = new HashSet<>();
        for (SelectItemModel.SwitchCase<?> switchCase : unbakedSwitch.cases()) {
            for (Object value : switchCase.values()) {
                present.add((ResourceKey<TrimMaterial>) value);
            }
        }

        List cases = new ArrayList(unbakedSwitch.cases());
        boolean changed = false;
        for (Identifier material : materials) {
            ResourceKey<TrimMaterial> key = ResourceKey.create(Registries.TRIM_MATERIAL, material);
            if (present.contains(key)) {
                continue;
            }
            Identifier overlayModel = Identifier.fromNamespaceAndPath(
                    material.getNamespace(), "item/" + armor + "_" + material.getPath() + "_trim");
            Identifier overlayResource = Identifier.fromNamespaceAndPath(
                    material.getNamespace(), "models/item/" + armor + "_" + material.getPath() + "_trim.json");
            if (resourceManager.getResourceStack(overlayResource).isEmpty()) {
                continue;
            }
            ItemModel.Unbaked overlay = new CuboidItemModelWrapper.Unbaked(overlayModel, Optional.empty(), List.of());
            cases.add(new SelectItemModel.SwitchCase(List.of(key), overlay));
            changed = true;
        }
        if (!changed) {
            return null;
        }

        SelectItemModel.UnbakedSwitch newSwitch =
                new SelectItemModel.UnbakedSwitch(unbakedSwitch.property(), cases);
        SelectItemModel.Unbaked newModel =
                new SelectItemModel.Unbaked(select.transformation(), newSwitch, select.fallback());
        return new ClientItem(newModel, item.properties(), item.registrySwapper());
    }

    @Unique
    private static List<Identifier> entate$discoverMaterials(ResourceManager resourceManager) {
        String dir = "trim_animations";
        String suffix = ".json";
        List<Identifier> materials = new ArrayList<>();
        for (Identifier file : resourceManager.listResources(dir, id -> id.getPath().endsWith(suffix)).keySet()) {
            String path = file.getPath();
            String name = path.substring(dir.length() + 1, path.length() - suffix.length());
            materials.add(Identifier.fromNamespaceAndPath(file.getNamespace(), name));
        }
        return materials;
    }
}
