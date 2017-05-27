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

package uk.co.thinkofdeath.parsing.bukkit.validators;

import org.bukkit.entity.Player;
import uk.co.thinkofdeath.parsing.ParserException;
import uk.co.thinkofdeath.parsing.validators.ArgumentValidator;
import uk.co.thinkofdeath.parsing.validators.TypeHandler;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@TypeHandler(
        value = StrictHandler.class,
        clazz = Player.class
)
@Target({ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
/**
 * Requires the player to be an exact match instead of
 * a partial one
 */
public @interface Strict {
}


class StrictHandler implements ArgumentValidator<Player> {

    StrictHandler(Strict strict) {
    }

    @Override
    public void validate(String argStr, Player argument) throws ParserException {
        if (argStr == null) return;
        if (argument.getName().equalsIgnoreCase(argStr)) {
            return;
        }
        throw new ParserException(3, "bukkit.no-player", argStr);
    }
}