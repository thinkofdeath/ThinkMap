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

import uk.co.thinkofdeath.parsing.parsers.ArgumentParser;
import uk.co.thinkofdeath.parsing.validators.ArgumentValidator;

import java.util.Arrays;

class ArgumentNode {

    final ArgumentParser parser;
    final ArgumentValidator[] type;
    final CommandNode node = new CommandNode();

    final Class<?> varargsType;

    ArgumentNode(ArgumentParser parser, ArgumentValidator[] type, Class<?> varargsType) {
        this.parser = parser;
        this.type = type;
        this.varargsType = varargsType;
    }

    @Override
    public String toString() {
        return "ArgumentNode{" +
                "parser=" + parser +
                ", type=" + Arrays.toString(type) +
                ", node=" + node +
                '}';
    }
}
