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

import com.carrotsearch.junitbenchmarks.BenchmarkOptions;
import com.carrotsearch.junitbenchmarks.BenchmarkRule;
import org.junit.Rule;
import org.junit.Test;

public class BenchmarkCommand implements CommandHandler {

    @Rule
    public final BenchmarkRule benchmarkRule = new BenchmarkRule();

    private final CommandManager commandManager = new CommandManager();

    {
        commandManager.register(this);
    }

    @Test
    @BenchmarkOptions(benchmarkRounds = 20000, warmupRounds = 100)
    public void execute() throws CommandException {
        commandManager.execute("tester", "hello world test");
    }

    @Test
    @BenchmarkOptions(benchmarkRounds = 20000, warmupRounds = 100)
    public void execute2() throws CommandException {
        commandManager.execute("tester", "hello 46 number");
    }

    @Command("hello world test")
    public void testCommand(String sender) {
    }

    @Command("hello world testing")
    public void testCommand2(String sender) {
    }

    @Command("hello world cake")
    public void testCommand3(String sender) {
    }

    @Command("hello testing world")
    public void testCommand4(String sender) {
    }

    @Command("hello ? testing")
    public void testCommand5(String sender, String arg) {
    }

    @Command("hello ? number")
    public void testCommand6(String sender, int arg) {
    }
}
