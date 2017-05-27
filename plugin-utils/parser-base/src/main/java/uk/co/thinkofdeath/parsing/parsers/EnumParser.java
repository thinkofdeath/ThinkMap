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

package uk.co.thinkofdeath.parsing.parsers;

import uk.co.thinkofdeath.parsing.ParserException;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Allows for enums to be used as arguments to commands
 *
 * @param <T>
 *         The type of enum
 */
public class EnumParser<T extends Enum<T>> implements ArgumentParser<T> {

    private final Class<T> e;
    private final boolean ignoreUnderscores;

    private final Map<String, T> underscoreless;

    /**
     * Creates an enum parser for the enum
     *
     * @param e
     *         The class of the enum to use
     * @param ignoreUnderscores
     *         Whether to ignore underscores when matching enum names
     */
    public EnumParser(Class<T> e, boolean ignoreUnderscores) {
        this.e = e;
        this.ignoreUnderscores = ignoreUnderscores;
        if (ignoreUnderscores) {
            underscoreless = new HashMap<>();
            for (T v : e.getEnumConstants()) {
                underscoreless.put(v.name().replace("_", "").toLowerCase(), v);
            }
        } else {
            underscoreless = null;
        }
    }

    /**
     * Creates an enum parser for the enum, defaults to acknowledge underscores
     *
     * @param e
     *         The class of the enum to use
     */
    public EnumParser(Class<T> e) {
        this(e, false);
    }

    @Override
    public T parse(String argument) throws ParserException {
        if (ignoreUnderscores) {
            T v = underscoreless.get(argument.replace("_", "").toLowerCase());
            if (v != null) {
                return v;
            }
        } else {
            for (T v : e.getEnumConstants()) {
                if (v.name().equalsIgnoreCase(argument)) {
                    return v;
                }
            }
        }
        throw new ParserException(2, "parser.enum.invalid", argument);
    }

    @Override
    public Set<String> complete(String argument) {
        argument = argument.toLowerCase();
        HashSet<String> ret = new HashSet<>();
        for (T v : e.getEnumConstants()) {
            if (v.name().toLowerCase().startsWith(argument)) {
                ret.add(v.name());
            }
        }
        return ret;
    }
}
