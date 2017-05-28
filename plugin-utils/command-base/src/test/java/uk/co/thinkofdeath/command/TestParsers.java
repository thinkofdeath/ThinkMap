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
import uk.co.thinkofdeath.parsing.parsers.ArgumentParser;
import uk.co.thinkofdeath.parsing.parsers.EnumParser;
import uk.co.thinkofdeath.parsing.ParserException;

import java.util.*;

import static org.junit.Assert.*;

@SuppressWarnings("unused")
public class TestParsers {

    @Test
    public void stringParser() throws CommandException {
        CommandManager commandManager = new CommandManager();
        commandManager.register(new CommandHandler() {
            private String[] expected = {"a", "hello", "TesTing", "123", "a2C"};
            private int current = 0;

            @Command("test ?")
            public void stringTest(String sender, String arg) {
                assertEquals(arg, expected[current++]);
            }
        });
        commandManager.execute("tester", "test a");
        commandManager.execute("tester", "test hello");
        commandManager.execute("tester", "test TesTing");
        commandManager.execute("tester", "test 123");
        commandManager.execute("tester", "test a2C");
    }

    @Test
    public void intParser() throws CommandException {
        CommandManager commandManager = new CommandManager();
        commandManager.register(new CommandHandler() {
            private int[] expected = {55, 1204, 36589, -3590, -123456};
            private int current = 0;

            @Command("test ?")
            public void intTest(String sender, int arg) {
                assertEquals(arg, expected[current++]);
            }
        });
        commandManager.execute("tester", "test 55");
        commandManager.execute("tester", "test 1204");
        commandManager.execute("tester", "test 36589");
        commandManager.execute("tester", "test -3590");
        commandManager.execute("tester", "test -123456");
    }

    @Test(expected = CommandException.class)
    public void intParserFail() throws CommandException {
        CommandManager commandManager = new CommandManager();
        commandManager.register(new CommandHandler() {
            @Command("test ?")
            public void intTest(String sender, int arg) {
                fail();
            }
        });
        commandManager.execute("tester", "test 55e3");
    }

    @Test(expected = CommandException.class)
    public void intWrongType() throws CommandException {
        CommandManager commandManager = new CommandManager();
        commandManager.register(new CommandHandler() {
            @Command("world ? time set ?")
            public void test(String sender, String name, int newTime) {
                fail("Shouldn't be called");
            }
        });
        commandManager.execute("hello", "world testing time set cake");
    }

    @Test
    public void doubleParser() throws CommandException {
        CommandManager commandManager = new CommandManager();
        commandManager.register(new CommandHandler() {
            private double[] expected = {123.0, 0.0, -0.061, 100, 43E10, 65E-7};
            private int current = 0;

            @Command("test ?")
            public void doubleTest(String sender, double arg) {
                assertEquals(arg, expected[current++], 0.0);
            }
        });
        commandManager.execute("tester", "test 123");
        commandManager.execute("tester", "test 0");
        commandManager.execute("tester", "test -0.061");
        commandManager.execute("tester", "test 100D");
        commandManager.execute("tester", "test 43E10");
        commandManager.execute("tester", "test 65e-7f");
    }

    @Test(expected = CommandException.class)
    public void doubleParserFail() throws CommandException {
        CommandManager commandManager = new CommandManager();
        commandManager.register(new CommandHandler() {
            @Command("test ?")
            public void doubleTest(String sender, double arg) {
                fail();
            }
        });
        commandManager.execute("tester", "test 334L");
    }

    @Test(expected = CommandException.class)
    public void doubleParserFailNaN() throws CommandException {
        CommandManager commandManager = new CommandManager();
        commandManager.register(new CommandHandler() {
            @Command("test ?")
            public void doubleTest(String sender, double arg) {
                fail();
            }
        });
        commandManager.execute("tester", "test NaN");
    }

    @Test(expected = CommandException.class)
    public void doubleParserFailInfinity() throws CommandException {
        CommandManager commandManager = new CommandManager();
        commandManager.register(new CommandHandler() {
            @Command("test ?")
            public void doubleTest(String sender, double arg) {
                fail();
            }
        });
        commandManager.execute("tester", "test Infinity");
    }

    @Test(expected = CommandException.class)
    public void doubleWrongType() throws CommandException {
        CommandManager commandManager = new CommandManager();
        commandManager.register(new CommandHandler() {
            @Command("world ? time set ?")
            public void test(String sender, String name, double newTime) {
                fail("Shouldn't be called");
            }
        });
        commandManager.execute("hello", "world testing time set cake");
    }

    static enum TestEnum {
        HELLO,
        TESTING,
        CAKE,
        COLD,
        ABC,
        UNDER_SCORE
    }

    @Test
    public void enumParser() throws CommandException {
        CommandManager commandManager = new CommandManager();
        commandManager.addParser(TestEnum.class, new EnumParser<>(TestEnum.class));
        commandManager.register(new CommandHandler() {
            private int count = 0;

            @Command("test ?")
            public void enumTest(String sender, TestEnum testEnum) {
                assertEquals(TestEnum.values()[count++], testEnum);
            }
        });

        commandManager.execute("test", "test hello");
        commandManager.execute("test", "test TESTING");
        commandManager.execute("test", "test cAkE");
        commandManager.execute("test", "test cold");
        commandManager.execute("test", "test aBc");
        try {
            commandManager.execute("test", "test world");
            fail();
        } catch (CommandException e) {
            // All ok
        }
    }

    @Test
    public void enumParserUnderscore() throws CommandException {
        CommandManager commandManager = new CommandManager();
        commandManager.addParser(TestEnum.class, new EnumParser<>(TestEnum.class, true));
        commandManager.register(new CommandHandler() {
            private int count = 0;

            @Command("test ?")
            public void enumTest(String sender, TestEnum testEnum) {
                assertEquals(TestEnum.UNDER_SCORE, testEnum);
            }
        });

        commandManager.execute("test", "test uNdErScOrE");
        commandManager.execute("test", "test under_SCORE");
    }

    @Test
    public void enumParserComplete() throws CommandException {
        CommandManager commandManager = new CommandManager();
        commandManager.addParser(TestEnum.class, new EnumParser<>(TestEnum.class));
        commandManager.register(new CommandHandler() {

            @Command("test ?")
            public void enumTest(String sender, TestEnum testEnum) {
                fail();
            }

            @Command("test abcd")
            public void test(String sender) {
                fail();
            }
        });
        Util.same(Arrays.asList("HELLO"), commandManager.complete("test hel"));
        Util.same(new ArrayList<String>(), commandManager.complete("test wor"));
        Util.same(Arrays.asList("CAKE", "COLD"), commandManager.complete("test c"));
        Util.same(Arrays.asList("TESTING"), commandManager.complete("test test"));
        Util.same(Arrays.asList("ABC", "abcd"), commandManager.complete("test ab"));
    }

    @Test
    public void uuidParser() throws CommandException {
        CommandManager commandManager = new CommandManager();
        commandManager.register(new CommandHandler() {
            private UUID[] expected =
                    {
                            UUID.fromString("f34d4287-316d-4e43-b381-2dffc7f05b82"),
                            UUID.fromString("a122d0c7-2c6d-4709-a2c1-f452212af850"),
                            UUID.fromString("efa4eba3-04ab-4fe9-a726-97de00d92ea0"),
                            UUID.fromString("f2f9bd2d-6af9-4d83-b25d-9804a22a1483"),
                            UUID.fromString("0a65a943-7a2e-4abd-9b27-b53b9595140b")
                    };
            private int current = 0;

            @Command("test ?")
            public void uuidTest(String sender, UUID arg) {
                assertEquals(arg, expected[current++]);
            }
        });
        commandManager.execute("tester", "test f34d4287-316d-4e43-b381-2dffc7f05b82");
        commandManager.execute("tester", "test a122d0c7-2c6d-4709-a2c1-f452212af850");
        commandManager.execute("tester", "test efa4eba3-04ab-4fe9-a726-97de00d92ea0");
        commandManager.execute("tester", "test f2f9bd2d6af94d83b25d9804a22a1483");
        commandManager.execute("tester", "test 0a65a9437a2e4abd9b27b53b9595140b");
    }

    @Test(expected = CommandException.class)
    public void uuidParserFail() throws CommandException {
        CommandManager commandManager = new CommandManager();
        commandManager.register(new CommandHandler() {
            @Command("test ?")
            public void uuidTest(String sender, UUID arg) {
                fail();
            }
        });
        // first character is g
        commandManager.execute("tester", "test g34d4287-316d-4e43-b381-2dffc7f05b82");
    }

    @Test(expected = CommandException.class)
    public void uuidParserFail2() throws CommandException {
        CommandManager commandManager = new CommandManager();
        commandManager.register(new CommandHandler() {
            @Command("test ?")
            public void uuidTest(String sender, UUID arg) {
                fail();
            }
        });
        // first character is g, now mojang-style
        commandManager.execute("tester", "test g34d4287316d4e43b3812dffc7f05b82");
    }

    @Test
    public void customParser() throws CommandException {
        CommandManager commandManager = new CommandManager();
        commandManager.addParser(int[].class, new ArgumentParser<int[]>() {
            @Override
            public int[] parse(String argument) throws ParserException {
                String[] args = argument.split("\\s*,\\s*");
                int[] out = new int[args.length];
                for (int i = 0; i < args.length; i++) {
                    try {
                        out[i] = Integer.parseInt(args[i]);
                    } catch (NumberFormatException e) {
                        throw new ParserException(2, e.getMessage());
                    }
                }
                return out;
            }

            @Override
            public Set<String> complete(String argument) {
                return new HashSet<>();
            }
        });
        commandManager.register(new CommandHandler() {
            @Command("numbers ?")
            public void giveNumbers(String caller, int[] numbers) {
                assertEquals(caller, "tester");
                assertArrayEquals(numbers, new int[]{1, 2, 3, 4, 10});
            }
        });
        commandManager.execute("tester", "numbers `1, 2, 3, 4, 10`");
    }
}
