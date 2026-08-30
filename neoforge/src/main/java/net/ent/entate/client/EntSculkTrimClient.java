package net.ent.entate.client;

import net.ent.entate.trim.TrimAnimationManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;

public final class EntSculkTrimClient {

    public static void init(IEventBus modEventBus) {

        modEventBus.addListener((AddClientReloadListenersEvent event) -> event.addListener(TrimAnimationManager.ID,
            (ResourceManagerReloadListener) TrimAnimationManager::reload)
        );
    }

    private EntSculkTrimClient() {}
}
