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

package uk.co.thinkofdeath.command;

import java.lang.annotation.*;

/**
 * Used to annotate methods as commands where the value of this annotation should be the syntax of command required to execute.
 *
 * @see uk.co.thinkofdeath.command.CommandManager#register(CommandHandler)
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
// Note that @Repeatable is a java 8 feature and class. The JVM will ignore this annotation in that case so it is still
// backwards compatible.
@Repeatable(Commands.class)
public @interface Command {
    /**
     * The syntax of the command required
     *
     * @return The command syntax
     * @see uk.co.thinkofdeath.command.CommandManager#register(CommandHandler)
     */
    String value();
}
