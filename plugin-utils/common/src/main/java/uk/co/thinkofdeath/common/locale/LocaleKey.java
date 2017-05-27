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

package uk.co.thinkofdeath.common.locale;

/**
 * For use in CommandError to provide localised arguments
 */
public class LocaleKey {

    private final String key;

    /**
     * Creates a LocaleKey with the key set
     * to the passed argument
     *
     * @param key
     *         The locale key
     */
    public LocaleKey(String key) {
        this.key = key;
    }

    /**
     * Returns the key that this LocaleKey points to
     *
     * @return The locale key
     */
    public String getKey() {
        return key;
    }

    /**
     * Returns the localised version of this LocaleKey
     * @return The localised version
     */
    public String localise(LocaleHandler localeHandler) {
        return localeHandler.getLocalisedString(key);
    }
}
