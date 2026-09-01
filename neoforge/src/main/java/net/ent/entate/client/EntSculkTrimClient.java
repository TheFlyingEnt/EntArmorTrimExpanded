package net.ent.entate.client;

import net.ent.entate.trim.TrimAnimationManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

public final class EntSculkTrimClient {

    public static void init(IEventBus modEventBus) {

        modEventBus.addListener((RegisterClientReloadListenersEvent event) ->
                event.registerReloadListener((ResourceManagerReloadListener) TrimAnimationManager::reload));

        NeoForge.EVENT_BUS.addListener((ItemTooltipEvent event) ->
                GlowingTrimTooltip.append(event.getItemStack(), event.getToolTip()));
    }

    private EntSculkTrimClient() {}
}
