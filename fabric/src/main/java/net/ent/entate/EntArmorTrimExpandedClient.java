package net.ent.entate;

import net.ent.entate.trim.TrimAnimationManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;

public class EntArmorTrimExpandedClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ResourceLoader.get(PackType.CLIENT_RESOURCES).registerReloadListener(TrimAnimationManager.ID, (ResourceManagerReloadListener) TrimAnimationManager::reload);
    }
}
