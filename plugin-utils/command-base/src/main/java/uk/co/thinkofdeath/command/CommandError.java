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

import uk.co.thinkofdeath.common.locale.LocaleHandler;
import uk.co.thinkofdeath.common.locale.LocaleKey;

/**
 * Contains the information required to display a
 * error message
 */
public class CommandError extends LocaleKey {

    private final int priority;
    private final Object[] args;

    public CommandError(int priority, String key, Object... args) {
        super(key);
        this.priority = priority;
        this.args = args;
    }

    /**
     * The priority of error, used in selecting which
     * error message to display
     *
     * @return The priority
     */
    public int getPriority() {
        return priority;
    }

    /**
     * Returns the number of additional arguments
     * to the message this error has
     *
     * @return The number of arguments
     */
    public int getArgumentCount() {
        return args.length;
    }

    /**
     * Returns the argument at the index.
     *
     * @param i
     *         The index of the argument
     * @return The argument
     * @throws java.lang.ArrayIndexOutOfBoundsException
     *         if the index is less than zero or greater
     *         than or equal {@link #getArgumentCount()}
     */
    public Object getArgument(int i) {
        return args[i];
    }

    @Override
    public String localise(LocaleHandler localeHandler) {
        Object[] args = new Object[this.args.length];
        for (int i = 0; i < args.length; i++) {
            Object arg = this.args[i];
            if (arg instanceof LocaleKey) {
                args[i] = ((LocaleKey) arg).localise(localeHandler);
            } else {
                args[i] = arg;
            }
        }
        return String.format(super.localise(localeHandler), args);
    }
}
