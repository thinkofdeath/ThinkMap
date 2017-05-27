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

package uk.co.thinkofdeath.parsing.bukkit;

import java.util.Map;

public class DefaultBukkitParserLocales {

    public static void insert(Map<String, String> strings) {
        strings.put("bukkit.no-permission", "You do not have the permission to do this");
        strings.put("bukkit.no-player", "Could not find player '%s'");
        strings.put("bukkit.no-world", "Could not find world '%s'");
        strings.put("bukkit.no-enchantment", "Could not find enchantment '%s'");
        strings.put("bukkit.no-potion", "Could not find potion type '%s'");
    }
}
