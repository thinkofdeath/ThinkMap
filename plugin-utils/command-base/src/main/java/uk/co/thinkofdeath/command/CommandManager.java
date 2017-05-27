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

import uk.co.thinkofdeath.parsing.ParserException;
import uk.co.thinkofdeath.parsing.parsers.*;
import uk.co.thinkofdeath.parsing.validators.ArgumentValidator;
import uk.co.thinkofdeath.parsing.validators.TypeHandler;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A CommandManager handles registration and parsing
 * of commands as well as provided methods for completing
 * commands.
 *
 * <p>
 *
 * This works using reflection and a series of annotations
 * to define commands. The {@link uk.co.thinkofdeath.command.Command}
 * annotation is used to define the syntax of command, words
 * are treated as sub-commands while `?` are markers for
 * where user controlled arguments will go. For example:
 *
 * <pre><code>
 * &#064;Command("tp ?")
 * public void teleport(Player sender, Player target) {
 *     // Code to teleport here
 * }</code></pre>
 *
 * For more information on the {@link uk.co.thinkofdeath.command.Command}
 * annotation's handling see: {@link #register(CommandHandler)}
 *
 * <p>
 *
 * The command system supports overloading commands so that
 * different argument/caller types will call different methods
 * for the same command syntax. This also allow for you to
 * require a specific caller for certain commands For example:
 *
 * <pre><code>
 * &#064;Command("tp ?")
 * public void teleport(Player sender, Player target) {
 *     // Only a Player (or sub-class of) can call this
 * }
 *
 * &#064;Command("tp ? ?")
 * public void teleport(ConsoleCommandSender sender, Player a, Player b) {
 *     // If the console calls `/tp a b`
 * }
 *
 * &#064;Command("tp ? ?")
 * public void teleport(Player sender, Player a, Player b) {
 *     // If the player calls `/tp a b`
 * }
 *
 * &#064;Command("tp ? ?")
 * public void teleport(CommandSender sender, Player a, World target) {
 *     // If the CommandSender calls `/tp a world` but with
 *     // a world instead of a player
 * }
 * </code></pre>
 *
 * Argument validator annotations can be used to place limits on
 * certain arguments. {@link uk.co.thinkofdeath.parsing.validators.MaxLength}
 * for example applies to String arguments and will check if
 * the provided argument is smaller or equal to its argument and
 * if its not the command will fail. This can also be used to
 * allow the same argument to go to different methods based on
 * its value. For example:
 *
 * <pre><code>
 * &#064;Command("player set nick ?")
 * public void setNick(Player player, @MaxLength(16) String newNick) {
 *     // If newNick is more than 16 characters this will not
 *     // be called
 * }
 *
 * &#064;Command("do something ?")
 * public void doSomethingSmall(CommandSender sender, @Range(min = 0, max = 5) int something) {
 *     // If something is between (inclusive) 0 and 5
 * }
 *
 * &#064;Command("do something ?")
 * public void doSomethingMid(CommandSender sender, @Range(min = 6, max = 15) int something) {
 *     // If something is between (inclusive) 6 and 15
 * }
 *
 * &#064;Command("do something ?")
 * public void doSomethingLarge(CommandSender sender, @Range(min = 16) int something) {
 *     // If something is greater or equal to 16
 * }
 * </code></pre>
 *
 * Argument validators placed on the method will apply to the first argument
 */
public class CommandManager {

    private static final Object NO_ARG = new Object();
    private final CommandLocaleHandler localeHandler;
    private final CommandNode rootNode = new CommandNode();
    private final HashMap<Class<?>, ArgumentParser> parsers = new HashMap<>();
    private final Pattern splitter = Pattern.compile("(?:`(.*?)`)|(?:(.*?)(\\s|$))");

    /**
     * Creates a CommandManager initialised with parsers
     * for basic types:
     * <ul>
     * <li>String</li>
     * <li>int</li>
     * </ul>
     */
    public CommandManager() {
        this(new DefaultLocaleHandler());
    }

    /**
     * Creates a CommandManager initialised with parsers
     * for basic types:
     * <ul>
     * <li>String</li>
     * <li>int</li>
     * </ul>
     *
     * <p>
     *
     * This will use the passed locale handler to produce
     * error messages from commands. The locale handler
     * can also be used to pre-process commands be the
     * command manager registers them to allow for the
     * order of arguments to be changed
     *
     * @param localeHandler
     *         The locale handler to use
     */
    public CommandManager(CommandLocaleHandler localeHandler) {
        this.localeHandler = localeHandler;
        addParser(String.class, new StringParser());
        addParser(int.class, new IntegerParser());
        addParser(double.class, new DoubleParser());
        addParser(UUID.class, new UUIDParser());
    }

    /**
     * This registers all the commands contained in the
     * passed command handler class. Commands are methods
     * annotated with a {@link uk.co.thinkofdeath.command.Command}
     * annotation.
     *
     * <p>
     *
     * The value contained in the {@link uk.co.thinkofdeath.command.Command}
     * is used to find the placement of each argument in
     * the command as sub-commands are supported. The
     * format of the command value should be as follows
     *
     * <pre>
     * command sub ? anothersub ?
     * </pre>
     *
     * where `?` is the location of the parameter the
     * user can enter. The type of the parameters will be
     * based on the types of the parameters the Command
     * annotation is attached to excluding the first argument.
     * Only types with registered parsers may be used.
     *
     * <p>
     *
     * After `?` a number can follow to control which parameter
     * of the method this argument will be passed to. E.g. `?2`
     * will cause that argument to be assigned to the second
     * parameter (third including caller) of method. It is
     * recommended that if this is used for one argument
     * then the rest should also be explicitly defined as
     * well to ensure consistent results.
     *
     * <p>
     *
     * The first argument of the method will be the caller
     * and the command will only be passed to the method
     * if the caller is assignable to it.
     *
     * <p>
     *
     * Additional annotations may be added to the parameters
     * to impose limits on them. The annotation must have
     * a type handler annotation on them for the executor
     * use them. For example {@link uk.co.thinkofdeath.parsing.validators.MaxLength}
     *
     * @param commandHandler
     *         The command handler to be added
     */
    public void register(CommandHandler commandHandler) {
        // We search through declared methods so that private
        // ones may be accessed. This allows for pure command
        // handling classes to be produced without exposing
        // the methods publicly (e.g. for a plugin with an
        // API)
        for (Method method : collectAnnotatedMethods(commandHandler.getClass())) {
            Command[] commands;
            Command singleAnnotation = method.getAnnotation(Command.class);
            if (singleAnnotation == null) {
                commands = method.getAnnotation(Commands.class).value();
            } else {
                commands = new Command[]{ singleAnnotation };
            }

            method.setAccessible(true); // It may be private
            if (method.getParameterTypes().length < 1) {
                // No way to actually check if the argument is a caller
                // just that it exists since anything could technically
                // be caller
                throw new CommandRegisterException("You must have a 'caller' argument");
            }

            Class<?>[] methodArgs = method.getParameterTypes();
            Annotation[][] methodArgAnnotations = method.getParameterAnnotations();

            for (Command command : commands) {
                String[] args = localeHandler.getCommand(command.value()).split("\\s");
                int argIndex = 1; // Skip the 'caller' argument
                int[] argumentPositions = new int[methodArgs.length];
                // This starts at the at the root node and
                // searches/creates branches until it reaches
                // its end where it places the method to be
                // called later
                CommandNode currentNode = rootNode;
                for (int i = 0; i < args.length; i++) {
                    String arg = args[i];
                    if (arg.startsWith("?")) { // Dynamic argument
                        int index;
                        if (arg.equals("?")) {
                            index = argIndex;
                        } else {
                            index = Integer.parseInt(arg.substring(1));
                            if (index >= methodArgs.length && index < 1) {
                                throw new CommandRegisterException("Invalid explicit argument position");
                            }
                        }
                        if (argIndex >= methodArgs.length) {
                            throw new CommandRegisterException("Incorrect number of method parameters");
                        }
                        Class<?> argType = methodArgs[index];

                        boolean varargs;
                        if (method.isVarArgs() && methodArgs.length == index + 1) {
                            for (int j = i + 1; j < args.length; j++) {
                                if (!args[j].isEmpty()) {
                                    throw new CommandRegisterException("Varargs needs to be last argument!");
                                }
                            }
                            assert argType.isArray(); // otherwise varargs wouldn't really make sense
                            argType = argType.getComponentType();
                            varargs = true;
                        } else {
                            varargs = false;
                        }

                        ArgumentParser parser = parsers.get(argType);
                        if (parser == null) {
                            throw new CommandRegisterException("No parser for " + argType.getSimpleName());
                        }
                        // Obtain the annotations with argument validators and create
                        // instances of them using the annotation as the arguments
                        Annotation[] annotations = methodArgAnnotations[index];
                        ArgumentValidator[] argCheckers = processCommandAnnotations(argType, annotations);

                        ArgumentNode argumentNode = new ArgumentNode(parser, argCheckers, varargs ? argType : null);
                        currentNode.arguments.add(argumentNode);
                        // Branch into the node
                        currentNode = argumentNode.node;
                        // Save the location of the argument
                        argumentPositions[argIndex] = index;
                        argIndex++;
                    } else { // Constant
                        arg = arg.toLowerCase(); // We don't care about case for sub commands
                        if (!currentNode.subCommands.containsKey(arg)) {
                            // Creates the branch if it doesn't exist
                            currentNode.subCommands.put(arg, new CommandNode());
                        }
                        // Branch into the node
                        currentNode = currentNode.subCommands.get(arg);
                    }
                }

                // Either we have left over '?' or not enough
                if (argIndex != methodArgs.length) {
                    throw new CommandRegisterException("Incorrect number of method parameters");
                }

                // If we followed the route and got to a node with a method already then
                // another command has the same signature
                if (currentNode.methods.containsKey(methodArgs[0])) {
                    throw new CommandRegisterException("Duplicate command");
                }

                ArgumentValidator[] argumentValidators1 = processCommandAnnotations(methodArgs[0],
                        methodArgAnnotations[0]);
                ArgumentValidator[] argumentValidators2 = processCommandAnnotations(methodArgs[0],
                        method.getAnnotations());

                ArgumentValidator[] argumentValidators = new ArgumentValidator[argumentValidators1.length + argumentValidators2.length];
                System.arraycopy(argumentValidators1, 0, argumentValidators, 0, argumentValidators1.length);
                System.arraycopy(argumentValidators2, 0, argumentValidators, argumentValidators1.length, argumentValidators2.length);

                currentNode.methods.put(methodArgs[0],
                        new CommandNode.CommandMethod(
                                method,
                                commandHandler,
                                argumentValidators,
                                argumentPositions));
            }
        }
    }

    /**
     * Collects all methods annotated with Command (or Commands) in a list and returns it. If a method is overridden
     * and both the overridden and the overriding method are annotated, only the topmost (most specific) is returned.
     * Classes always take priority over interfaces when deciding which method and thus which Command annotation
     * should be used.
     *
     * To compare methods, method name and parameter types are returned; return type is assumed to be irrelevant.
     * Additionally, private methods are not overridable: if a method of the same signature exists in a subclass both
     * are returned.
     */
    private static List<Method> collectAnnotatedMethods(Class<?> of) {
        List<Method> collected = new ArrayList<>();
        collectAnnotatedMethods(collected, of);
        return collected;
    }

    private static void collectAnnotatedMethods(List<Method> target, Class<?> of) {
        if (of == null) { // top of tree (parent of Object or parent of an interface)
            return;
        }

        outer:
        for (Method method : of.getDeclaredMethods()) {
            if (method.getAnnotation(Command.class) == null && method.getAnnotation(Commands.class) == null) {
                continue;
            }
            if (!Modifier.isPrivate(method.getModifiers())) {
                // check if the method is already defined
                for (Method other : target) {
                    if (other.getName().equals(method.getName()) &&
                        Arrays.equals(other.getParameterTypes(), method.getParameterTypes())) {
                        continue outer;
                    }
                }
            }
            target.add(method);
        }

        // recurse into parents
        collectAnnotatedMethods(target, of.getSuperclass());
        for (Class<?> interf : of.getInterfaces()) {
            collectAnnotatedMethods(target, interf);
        }
    }

    // Obtains the annotations with argument validators and create
    // instances of them using the annotation as the arguments
    private ArgumentValidator[] processCommandAnnotations(Class<?> argType, Annotation[] annotations) {
        try {
            ArrayList<ArgumentValidator> argumentValidators = new ArrayList<>();
            for (Annotation annotation : annotations) {
                TypeHandler handler = annotation.annotationType().getAnnotation(TypeHandler.class);
                if (handler == null) {
                    continue;
                }
                if (!handler.clazz().isAssignableFrom(argType)) {
                    throw new RuntimeException(argType.getSimpleName() + " requires " + handler.clazz().getSimpleName());
                }
                Constructor<? extends ArgumentValidator> constructor = handler.value().getDeclaredConstructor(annotation.annotationType());
                constructor.setAccessible(true);
                ArgumentValidator type = constructor.newInstance(annotation);
                argumentValidators.add(type);

            }
            return argumentValidators.toArray(new ArgumentValidator[argumentValidators.size()]);
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Executes the command with the passed name as the passed caller
     * with the passed arguments (if any). This is a helper for APIs
     * that provide arguments as an array instead of the full string
     *
     * @param caller
     *         The caller to call as
     * @param name
     *         The name of the command
     * @param args
     *         The arguments of the command (if any)
     * @throws CommandException
     *         Thrown if the command failed to execute
     */
    public void execute(Object caller, String name, String... args) throws CommandException {
        execute(caller, join(name, args));
    }

    /**
     * Executes the command as the passed caller. Arguments
     * will be split by spaces unless the argument is wrapped
     * in '`' quotes
     *
     * @param caller
     *         The caller to call as
     * @param command
     *         The command
     * @throws CommandException
     *         Thrown if the command failed to execute
     */
    public void execute(Object caller, String command) throws CommandException {
        // lastError encountered whilst executing.
        CommandError lastError = null;
        String[] args = split(command);
        // Stores the states we can return to if the current route fails
        Stack<CommandState> toTry = new Stack<>();
        toTry.add(new CommandState(rootNode, caller, 0));
        // Try every possible route until we match a command or
        // run out of options
        while (!toTry.isEmpty()) {
            CommandState state = toTry.pop();
            CommandNode currentNode = state.node;
            int offset = state.offset;
            // We have enough arguments try executing the command
            if (offset == args.length) {
                if (currentNode.methods.size() == 0) {
                    // No command here
                    if (lastError == null || lastError.getPriority() < 1) {
                        lastError = new CommandError(1, "command.unknown");
                    }
                    continue;
                }
                // Check the caller
                callCheck:
                for (CommandNode.CommandMethod method : currentNode.methods.values()) {
                    Class<?> type = method.method.getParameterTypes()[0];
                    if (type.isAssignableFrom(caller.getClass())) {

                        for (ArgumentValidator t : method.argumentValidators) {
                            try {
                                t.validate(null, caller);
                            } catch (ParserException e) {
                                if (lastError == null || lastError.getPriority() < e.getPriority()) {
                                    lastError = new CommandError(e.getPriority(), e.getKey(), e.getArguments());
                                }
                                continue callCheck;
                            }
                        }

                        ArrayList<Object> arguments = new ArrayList<>();

                        CommandState currentState = state;
                        while (currentState != null) {
                            if (currentState.argument != NO_ARG) {
                                arguments.add(currentState.argument);
                            }
                            currentState = currentState.parent;
                        }


                        Object[] processedArguments = new Object[arguments.size()];
                        for (int i = 0; i < processedArguments.length; i++) {
                            processedArguments[method.argumentPositions[i]] = arguments.get(arguments.size() - i - 1);
                        }

                        try {
                            method.method.invoke(method.owner, processedArguments);
                        } catch (IllegalAccessException | InvocationTargetException e) {
                            // Propagate errors
                            if (e.getCause() != null && e.getCause() instanceof Error) {
                                throw (Error) e.getCause();
                            } else {
                                throw new RuntimeException(e);
                            }
                        }
                        return;
                    } else {
                        // Incorrect caller
                        if (lastError == null || lastError.getPriority() < 1) {
                            lastError = new CommandError(1, "command.incorrect.caller");
                        }
                    }
                }
                continue;
            }
            String arg = args[offset];
            String argLower = arg.toLowerCase(); // For checking sub commands
            // Try matching against all the argument types
            argTypes:
            for (ArgumentNode argumentNode : currentNode.arguments) {
                Object out;
                Object[] outArray;
                try {
                    if (argumentNode.varargsType != null) {
                        out = Array.newInstance(argumentNode.varargsType, args.length - offset);
                        outArray = (Object[]) out;
                        for (int i = offset; i < args.length; i++) {
                            Object parsed = argumentNode.parser.parse(args[i]);
                            if (parsed == null) { // parser error?
                                continue argTypes;
                            }
                            outArray[i - offset] = parsed;
                        }
                        offset = args.length - 1;
                    } else {
                        out = argumentNode.parser.parse(arg);
                        if (out == null) { // parser error?
                            continue;
                        }
                        outArray = null;
                    }
                } catch (ParserException e) {
                    if (lastError == null || lastError.getPriority() < e.getPriority()) {
                        lastError = new CommandError(e.getPriority(), e.getKey(), e.getArguments());
                    }
                    continue;
                }
                for (ArgumentValidator type : argumentNode.type) {
                    try {
                        if (outArray != null) {
                            for (Object o : outArray) {
                                type.validate(arg, o);
                            }
                        } else {
                            type.validate(arg, out);
                        }
                    } catch (ParserException e) {
                        if (lastError == null || lastError.getPriority() < e.getPriority()) {
                            lastError = new CommandError(e.getPriority(), e.getKey(), e.getArguments());
                        }
                        continue argTypes;
                    }
                }
                CommandState newState = new CommandState(argumentNode.node, out, offset + 1);
                newState.parent = state;
                toTry.add(newState);
            }
            // Check sub-commands
            if (currentNode.subCommands.containsKey(argLower)) {
                CommandNode nextNode = currentNode.subCommands.get(argLower);
                CommandState newState = new CommandState(nextNode, NO_ARG, offset + 1);
                newState.parent = state;
                toTry.add(newState);
            }
        }
        if (lastError == null || lastError.getPriority() < 1) {
            lastError = new CommandError(1, "command.unknown");
        }
        throw new CommandException(lastError, lastError.localise(localeHandler));
    }


    /**
     * Provides a list of possible completions for the
     * command. Follows the same rules a {@link #execute(Object, String)}
     * This is a helper for APIs that provide arguments
     * as an array instead of the full string
     *
     * @param name
     *         The name of the command
     * @param args
     *         The arguments of the command (if any)
     * @return A list of possible completions
     */
    public List<String> complete(String name, String... args) {
        return complete(join(name, args));
    }

    /**
     * Provides a list of possible completions for the
     * command. Follows the same rules a {@link #execute(Object, String)}
     *
     * @param command
     *         The command to complete
     * @return A list of possible completions
     */
    public List<String> complete(String command) {
        Set<String> completions = new HashSet<>();
        String[] args = split(command);
        // Stores the states we can return to if the current route fails
        Stack<CommandState> toTry = new Stack<>();
        toTry.add(new CommandState(rootNode, null, 0));
        // Try every possible route until we match a command or
        // run out of options
        while (!toTry.isEmpty()) {
            CommandState state = toTry.pop();
            CommandNode currentNode = state.node;
            int offset = state.offset;

            String arg = args[offset];
            String argLower = arg.toLowerCase(); // For checking sub commands
            // We have enough arguments try completing the command
            if (offset == args.length - 1) {
                for (String sub : currentNode.subCommands.keySet()) {
                    if (sub.startsWith(argLower)) {
                        completions.add(sub);
                    }
                }

                for (ArgumentNode argumentNode : currentNode.arguments) {
                    completions.addAll(argumentNode.parser.complete(arg));
                }
                continue;
            }
            // Try matching against all the argument types
            argTypes:
            for (ArgumentNode argumentNode : currentNode.arguments) {
                Object out;
                try {
                    out = argumentNode.parser.parse(arg);
                } catch (ParserException e) {
                    continue;
                }
                if (out == null) {
                    continue;
                }
                for (ArgumentValidator type : argumentNode.type) {
                    try {
                        type.validate(arg, out);
                    } catch (ParserException e) {
                        continue argTypes;
                    }
                }
                toTry.add(new CommandState(argumentNode.node, null, offset + 1));
            }
            // Check sub-commands
            if (currentNode.subCommands.containsKey(argLower)) {
                CommandNode newNode = currentNode.subCommands.get(argLower);
                toTry.add(new CommandState(newNode, null, offset + 1));
            }
        }
        return new ArrayList<>(completions);
    }

    /**
     * Defines a parser for the type class. The parser will be called
     * when the class is encountered as a parameter to a method.
     *
     * @param clazz
     *         The class to match this parser to
     * @param parser
     *         The parser to use for this type
     */
    public <T> void addParser(Class<T> clazz, ArgumentParser<T> parser) {
        parsers.put(clazz, parser);
    }

    // Split by spaces unless the argument is quoted with `
    private String[] split(String command) {
        Matcher matcher = splitter.matcher(command);
        ArrayList<String> args = new ArrayList<>();
        while (matcher.find()) {
            String arg = matcher.group().trim();
            if (arg.startsWith("`")) {
                arg = arg.substring(1, arg.length() - 1);
            }
            args.add(arg);
        }
        args.remove(args.size() - 1);
        return args.toArray(new String[args.size()]);
    }

    // Helper for converting between split arguments and joined arguments
    // format
    private String join(String name, String... args) {
        StringBuilder out = new StringBuilder(name);
        for (String arg : args) {
            out.append(' ').append(arg);
        }
        return out.toString();
    }

    private static class CommandState {
        private CommandNode node;
        private Object argument;
        private int offset;
        private CommandState parent;

        private CommandState(CommandNode node, Object argument, int offset) {
            this.node = node;
            this.argument = argument;
            this.offset = offset;
        }
    }
}
