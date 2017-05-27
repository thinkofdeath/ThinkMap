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

package uk.co.thinkofdeath.parsing.validators;

import uk.co.thinkofdeath.parsing.ParserException;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@TypeHandler(
        value = MaxLengthHandler.class,
        clazz = String.class
)
@Target({ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
/**
 * Requires the passed string to have a length less than
 * or equal to the value
 */
public @interface MaxLength {
    int value();
}

class MaxLengthHandler implements ArgumentValidator<String> {

    private final int max;

    MaxLengthHandler(MaxLength maxLength) {
        max = maxLength.value();
    }

    @Override
    public void validate(String argStr, String argument) throws ParserException {
        if (argument.length() > max) {
            throw new ParserException(3, "validator.maxlength", argument, max);
        }
    }
}
