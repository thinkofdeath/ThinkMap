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

/**
 * Allows for any valid double, except NaN and infinities
 */
public class DoubleParser implements ArgumentParser<Double> {
    @Override
    public Double parse(String argument) throws ParserException {
        try {
            double v = Double.valueOf(argument);

            // most command won't expect to handle special values
            if (Double.isNaN(v) || Double.isInfinite(v)) {
                throw new NumberFormatException();
            }

            return v;
        } catch (NumberFormatException e) {
            throw new ParserException(2, "parser.double.invalid", argument);
        }
    }

    @Override
    public Set<String> complete(String argument) {
        return new HashSet<>();
    }
}
