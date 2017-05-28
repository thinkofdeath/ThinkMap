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
import uk.co.thinkofdeath.parsing.ParserException;
import uk.co.thinkofdeath.parsing.validators.*;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static org.junit.Assert.*;

@SuppressWarnings("unused")
public class TestLimiter {
    @Test(expected = CommandException.class)
    public void limitMaxLength() throws CommandException {
        CommandManager commandManager = new CommandManager();
        commandManager.register(new CommandHandler() {
            @Command("world ? time set ?")
            public void test(String sender, @MaxLength(5) String name, int newTime) {
                fail("Shouldn't be called");
            }
        });
        commandManager.execute("hello", "world testing time set 55");
    }

    @Test
    public void limitRange() throws CommandException {
        CommandManager commandManager = new CommandManager();
        commandManager.register(new CommandHandler() {
            int call = 0;

            @Command("do ?")
            public void test(String sender, @Range(min = 0, max = 10) int val) {
                assertEquals(0, call++);
            }

            @Command("do ?")
            public void test2(String sender, @Range(min = 11, max = 20) int val) {
                assertEquals(1, call++);
            }

            @Command("do ?")
            public void test3(String sender, @Range(min = 21, max = 30) int val) {
                assertEquals(2, call++);
            }
        });
        commandManager.execute("hello", "do 4");
        commandManager.execute("hello", "do 18");
        commandManager.execute("hello", "do 25");
    }

    @Test(expected = CommandException.class)
    public void limitCaller() throws CommandException {
        CommandManager commandManager = new CommandManager();
        commandManager.register(new CommandHandler() {
            @Command("world ? time set ?")
            public void test(@MaxLength(2) String sender, String name, int newTime) {
                fail("Shouldn't be called");
            }
        });
        commandManager.execute("hello", "world testing time set 55");
    }

    @Test(expected = CommandException.class)
    public void limitRegex() throws CommandException {
        CommandManager commandManager = new CommandManager();
        commandManager.register(new CommandHandler() {
            @Command("test ?")
            public void test(String sender, @Regex("[a-zA-Z_][0-9a-zA-Z_]*") String name) {
                fail("Shouldn't be called");
            }
        });
        commandManager.execute("hello", "test 0hello_world123_");
    }

    @Test
    public void limitCustom() throws CommandException {
        CommandManager commandManager = new CommandManager();
        commandManager.register(new CommandHandler() {
            private int call = 0;

            @Command("testing ?")
            public void test(String sender, @NoK String test) {
                assertEquals(sender, "bob");
                assertEquals("Incorrect call", call++, 0);
                assertFalse(test.toLowerCase().contains("k"));
            }
        });
        commandManager.execute("bob", "testing test");
    }

    @Test(expected = CommandException.class)
    public void limitCustomFail() throws CommandException {
        CommandManager commandManager = new CommandManager();
        commandManager.register(new CommandHandler() {
            @Command("testing ?")
            public void test(String sender, @NoK String test) {
                fail("Shouldn't be called");
            }
        });
        commandManager.execute("bob", "testing cake");
    }

    @TypeHandler(
            value = NoK.NoKHandler.class,
            clazz = String.class
    )
    @Target(ElementType.PARAMETER)
    @Retention(RetentionPolicy.RUNTIME)
    private @interface NoK {

        class NoKHandler implements ArgumentValidator<String> {

            NoKHandler(NoK noK) {
            }

            @Override
            public void validate(String argString, String argument) throws ParserException{
                if (argument.toLowerCase().contains("k")) {
                    throw new ParserException(3, "No K's allowed");
                }
            }
        }
    }
}
