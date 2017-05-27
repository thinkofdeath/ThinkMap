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

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Allows for any valid UUID, both standard style and Mojang style
 */
public class UUIDParser implements ArgumentParser<UUID> {

    private final Pattern altPattern = Pattern.compile("([0-9a-fA-F]{8})([0-9a-fA-F]{4})([0-9a-fA-F]{4})([0-9a-fA-F]{4})([0-9a-fA-F]{12})");

    @Override
    public UUID parse(String argument) throws ParserException {
        try {
            return UUID.fromString(argument);
        } catch (IllegalArgumentException e) {
            Matcher m = altPattern.matcher(argument);
            if (!m.matches()) {
                throw new ParserException(2, "parser.uuid.invalid", argument);
            }
            String reformatted = String.format("%s-%s-%s-%s-%s", m.group(1), m.group(2), m.group(3), m.group(4), m.group(5));
            return parse(reformatted);
        }
    }

    @Override
    public Set<String> complete(String argument) {
        return new HashSet<>();
    }
}
