package net.ent.entate;

import net.ent.entate.client.GlowingTrimTooltip;
import net.ent.entate.trim.TrimAnimationManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;

public class EntArmorTrimExpandedClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES)
                .registerReloadListener(new SimpleSynchronousResourceReloadListener() {
                    @Override
                    public ResourceLocation getFabricId() {
                        return TrimAnimationManager.ID;
                    }

                    @Override
                    public void onResourceManagerReload(ResourceManager resourceManager) {
                        TrimAnimationManager.reload(resourceManager);
                    }
                });

        ItemTooltipCallback.EVENT.register(
                (stack, tooltipContext, tooltipType, lines) -> GlowingTrimTooltip.append(stack, lines));
    }
}
