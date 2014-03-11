package uk.co.thinkofdeath.thinkmap.bukkit;

import lombok.RequiredArgsConstructor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.WorldLoadEvent;

@RequiredArgsConstructor
public class Events implements Listener {

    private final ThinkMapPlugin plugin;

    @EventHandler
    public void onWorldLoad(WorldLoadEvent event) {
        if (plugin.getTargetWorld() == null) {
            plugin.setTargetWorld(event.getWorld());
        }
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        plugin.getChunkManager(event.getWorld()).activateChunk(event.getChunk());
    }

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event) {
        plugin.getChunkManager(event.getWorld()).deactivateChunk(event.getChunk());
    }
}
