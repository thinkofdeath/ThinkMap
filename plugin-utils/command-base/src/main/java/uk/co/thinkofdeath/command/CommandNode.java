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

import uk.co.thinkofdeath.parsing.validators.ArgumentValidator;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;

class CommandNode {

    final HashMap<String, CommandNode> subCommands = new HashMap<>();
    final ArrayList<ArgumentNode> arguments = new ArrayList<>();

    final HashMap<Class<?>, CommandMethod> methods = new HashMap<>();

    CommandNode() {
    }

    @Override
    public String toString() {
        return "CommandNode{" +
                "subCommands=" + subCommands +
                ", arguments=" + arguments +
                ", methods=" + methods +
                '}';
    }

    static class CommandMethod {
        final Method method;
        final CommandHandler owner;
        final ArgumentValidator[] argumentValidators;
        final int[] argumentPositions;

        CommandMethod(Method method, CommandHandler owner, ArgumentValidator[] argumentValidators, int[] argumentPositions) {
            this.method = method;
            this.owner = owner;
            this.argumentValidators = argumentValidators;
            this.argumentPositions = argumentPositions;
        }
    }
}
