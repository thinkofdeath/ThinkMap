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

import com.google.common.collect.ImmutableMap;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffectType;
import uk.co.thinkofdeath.parsing.ParserException;
import uk.co.thinkofdeath.parsing.parsers.ArgumentParser;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Parses an enchantment name into an potiontype object. This
 * class supplies mappings by default, but can be given custom mappings.
 * The keys in the map MUST be completely lowercase, otherwise behavior
 * is undefined.
 */
public class PotionParser implements ArgumentParser<PotionEffectType> {
    private final Plugin plugin;

    private final Map<String, PotionEffectType> map;
    private static final Map<String, PotionEffectType> DEFAULT;

    static {
        ImmutableMap.Builder<String, PotionEffectType> map = new ImmutableMap.Builder<>();
        for (Field f : PotionEffectType.class.getFields()) {
            if (Modifier.isStatic(f.getModifiers()) && Modifier.isFinal(f.getModifiers()) && PotionEffectType.class.isAssignableFrom(f.getType())) {
                try {
                    map.put(f.getName().toLowerCase(), (PotionEffectType) f.get(null));
                } catch (IllegalArgumentException | IllegalAccessException e) {
                    Bukkit.getLogger().severe("Exception building default map for " + PotionParser.class.getName() + " at element " + f.getName());
                    e.printStackTrace();
                }
            }
        }
        DEFAULT = map.build();
    }

    public PotionParser(Plugin plugin, Map<String, PotionEffectType> mappings) {
        this.plugin = plugin;
        this.map = mappings;
    }

    public PotionParser(Plugin plugin) {
        this(plugin, DEFAULT);
    }

    @Override
    public PotionEffectType parse(String argument) throws ParserException {
        PotionEffectType potion = map.get(argument.toLowerCase());
        if (potion == null) {
            throw new ParserException(2, "bukkit.no-potion", argument);
        }
        return potion;
    }

    @Override
    public Set<String> complete(String argument) {
        HashSet<String> completions = new HashSet<>();
        for (String name : map.keySet()) {
            if (name.startsWith(argument.toLowerCase())) {
                completions.add(name);
            }
        }
        return completions;
    }
}
