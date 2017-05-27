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

package uk.co.thinkofdeath.parsing.bukkit.parsing;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import uk.co.thinkofdeath.parsing.parsers.ArgumentParser;
import uk.co.thinkofdeath.parsing.ParserException;

import java.util.HashSet;
import java.util.Set;

/**
 * Parses player name's into Player objects. By default
 * this parsing doesn't require an exact match to work.
 * This may be changed by using {@link uk.co.thinkofdeath.parsing.bukkit.validators.Strict}
 */
public class PlayerParser implements ArgumentParser<Player> {
    private final Plugin plugin;

    public PlayerParser(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public Player parse(String argument) throws ParserException {
        Player player = plugin.getServer().getPlayer(argument);
        if (player == null) {
            throw new ParserException(2, "bukkit.no-player", argument);
        }
        return player;
    }

    @Override
    public Set<String> complete(String argument) {
        HashSet<String> completions = new HashSet<>();
        for (Player player : plugin.getServer().matchPlayer(argument)) {
            completions.add(player.getName());
        }
        return completions;
    }
}
