/*
 * Copyright 2014 Matthew Collins
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
        if (plugin.getTargetWorld() == null) plugin.setTargetWorld(event.getWorld());
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