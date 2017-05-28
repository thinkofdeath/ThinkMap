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

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestLocale {

    @Test
    public void testCustomLocale() throws CommandException {
        CommandHandler handler = new CommandHandler() {

            @Command("my.command")
            public void myCommand(String caller, String arg1, int arg2) {
                assertEquals("tester", caller);
                assertEquals("hello", arg1);
                assertEquals(55, arg2);
            }

        };

        CommandManager cm1 = new CommandManager(new CommandLocaleHandler() {
            @Override
            public String getCommand(String command) {
                return "mycommand give ? ?";
            }

            @Override
            public String getLocalisedString(String key) {
                return key;
            }
        });
        CommandManager cm2 = new CommandManager(new CommandLocaleHandler() {
            @Override
            public String getCommand(String command) {
                return "testing ?2 give ?1";
            }

            @Override
            public String getLocalisedString(String key) {
                return key;
            }
        });

        cm1.register(handler);
        cm2.register(handler);

        cm1.execute("tester", "mycommand give hello 55");
        cm2.execute("tester", "testing 55 give hello");
    }
}
