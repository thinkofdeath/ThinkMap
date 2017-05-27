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

import java.util.Set;

/**
 * An argument parser takes a argument passed into a
 * command returns object/type is represents. If the
 * argument cannot be parsed by the parser then this
 * should throw an {@link uk.co.thinkofdeath.parsing.ParserException}
 * with the message set to the reason that should be
 * passed to the executor
 */
public interface ArgumentParser<T> {

    /**
     * Attempts to convert the argument into the type
     * handled by this parser
     *
     * @param argument
     *         The argument to parse
     * @return The result of parsing
     * @throws uk.co.thinkofdeath.parsing.ParserException
     *         If the parser is unable to
     *         parse the argument
     */
    T parse(String argument) throws ParserException;

    /**
     * Returns a set containing the possible completions
     * to the passed argument
     *
     * @param argument
     *         The partial argument
     * @return The set of possible completions
     */
    Set<String> complete(String argument);
}
