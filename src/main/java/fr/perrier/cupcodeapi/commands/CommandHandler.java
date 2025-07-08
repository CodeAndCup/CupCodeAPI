package fr.perrier.cupcodeapi.commands;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import fr.perrier.cupcodeapi.commands.annotations.*;
import fr.perrier.cupcodeapi.commands.annotations.defaults.*;
import fr.perrier.cupcodeapi.utils.*;
import lombok.Getter;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.TabCompleter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

public class CommandHandler implements Listener {

    private final JavaPlugin plugin;

    @Getter
    public static final List<CommandData> commands = new ArrayList<>();
    static final Map<Class<?>, ParameterType<?>> parameterTypes = new HashMap<>();
    static boolean initiated = false;

    // Track registered root commands to avoid duplicates
    private final Map<String, List<CommandData>> registeredRootCommands = new HashMap<>();
    private final Set<String> registeredRootCommandNames = new HashSet<>();

    public CommandHandler(JavaPlugin plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Register a custom parameter adapter.
     */
    public static void registerParameterType(Class<?> transforms, ParameterType<?> parameterType) {
        parameterTypes.put(transforms, parameterType);
    }

    /**
     * Registers a single class with the command handler.
     */
    public void registerCommands(Class<?> registeredClass) {
        for (Method method : registeredClass.getMethods()) {
            if (method.getAnnotation(fr.perrier.cupcodeapi.commands.annotations.Command.class) != null) {
                registerMethod(method);
            }
        }
    }

    /**
     * Registers a single method with the command handler and with Bukkit's command system.
     */
    private void registerMethod(Method method) {
        fr.perrier.cupcodeapi.commands.annotations.Command commandAnnotation =
                method.getAnnotation(fr.perrier.cupcodeapi.commands.annotations.Command.class);
        List<ParameterData> parameterData = new ArrayList<>();

        // Offset of 1 here for the sender parameter.
        for (int parameterIndex = 1; parameterIndex < method.getParameterTypes().length; parameterIndex++) {
            Param paramAnnotation = null;

            for (Annotation annotation : method.getParameterAnnotations()[parameterIndex]) {
                if (annotation instanceof Param) {
                    paramAnnotation = (Param) annotation;
                    break;
                }
            }

            if (paramAnnotation != null) {
                parameterData.add(new ParameterData(paramAnnotation, method.getParameterTypes()[parameterIndex]));
            } else {
                return;
            }
        }

        CommandData commandData = new CommandData(commandAnnotation, parameterData, method,
                method.getParameterTypes()[0].isAssignableFrom(Player.class));

        commands.add(commandData);

        // Register the command dynamically with Bukkit
        registerWithBukkit(commandData);

        // We sort here so to ensure that our commands are matched properly.
        commands.sort((o1, o2) -> (o2.getName().length() - o1.getName().length()));
    }

    /**
     * Dynamically registers a command with Bukkit's command system
     * Handles multi-word commands by registering only the root and treating the rest as sub-commands
     */
    private void registerWithBukkit(CommandData commandData) {
        try {
            SimpleCommandMap commandMap = getCommandMap();

            // Parse all command names to get unique root commands
            for (String commandName : commandData.getNames()) {
                String rootCommand = commandName.split(" ")[0].toLowerCase();

                // Track this command data under the root command
                registeredRootCommands.computeIfAbsent(rootCommand, k -> new ArrayList<>()).add(commandData);

                // Only register with Bukkit if this root command hasn't been registered yet
                if (!registeredRootCommandNames.contains(rootCommand) && !isCommandRegistered(commandMap, rootCommand)) {
                    registerRootCommand(rootCommand, commandMap);
                    registeredRootCommandNames.add(rootCommand);
                }
            }

        } catch (Exception e) {
            plugin.getLogger().severe("Failed to register command: " + commandData.getName());
            e.printStackTrace();
        }
    }

    /**
     * Check if a command is already registered using reflection
     */
    private boolean isCommandRegistered(SimpleCommandMap commandMap, String commandName) {
        try {
            // Try to access the knownCommands field using reflection
            Field knownCommandsField = SimpleCommandMap.class.getDeclaredField("knownCommands");
            knownCommandsField.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<String, Command> knownCommands = (Map<String, Command>) knownCommandsField.get(commandMap);

            return knownCommands.containsKey(commandName.toLowerCase());
        } catch (Exception e) {
            // If reflection fails, try alternative approach
            try {
                // Try to get the command directly
                Command existingCommand = commandMap.getCommand(commandName);
                return existingCommand != null;
            } catch (Exception e2) {
                // If all fails, assume not registered to avoid blocking registration
                plugin.getLogger().warning("Could not check if command '" + commandName + "' is registered. Proceeding with registration.");
                return false;
            }
        }
    }

    /**
     * Register a root command with Bukkit
     */
    private void registerRootCommand(String rootCommand, SimpleCommandMap commandMap) throws Exception {
        // Create a new PluginCommand instance for the root command only
        Constructor<org.bukkit.command.PluginCommand> constructor =
                org.bukkit.command.PluginCommand.class.getDeclaredConstructor(String.class, org.bukkit.plugin.Plugin.class);
        constructor.setAccessible(true);

        org.bukkit.command.PluginCommand pluginCommand = constructor.newInstance(rootCommand, plugin);

        // Set description and usage
        pluginCommand.setDescription("Command handler for " + rootCommand);
        pluginCommand.setUsage("/" + rootCommand + " <subcommand>");

        // Set the executor and tab completer
        MultiCommandExecutor executor = new MultiCommandExecutor(rootCommand, this);
        pluginCommand.setExecutor(executor);
        pluginCommand.setTabCompleter(executor);

        // Register the root command
        commandMap.register(plugin.getName().toLowerCase(), pluginCommand);

        plugin.getLogger().info("Registered root command: " + rootCommand);
    }

    /**
     * Multi-command executor that handles root commands with sub-commands
     */
    private class MultiCommandExecutor implements CommandExecutor, TabCompleter {
        private final String rootCommand;
        private final CommandHandler handler;

        public MultiCommandExecutor(String rootCommand, CommandHandler handler) {
            this.rootCommand = rootCommand;
            this.handler = handler;
        }

        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            // Build the full command string including sub-commands
            String fullCommand = label;
            if (args.length > 0) {
                fullCommand += " " + String.join(" ", args);
            }

            // Try to find and execute a matching command
            CommandData executed = handler.evalCommand(sender, fullCommand);

            if (executed == null) {
                // No matching command found, show available sub-commands
                List<CommandData> availableCommands = registeredRootCommands.get(rootCommand.toLowerCase());
                if (availableCommands != null && !availableCommands.isEmpty()) {
                    sender.sendMessage(ChatColor.RED + "Unknown sub-command. Available commands:");
                    for (CommandData cmd : availableCommands) {
                        if (cmd.canAccess(sender)) {
                            for (String cmdName : cmd.getNames()) {
                                if (cmdName.toLowerCase().startsWith(rootCommand.toLowerCase())) {
                                    String usage = "/" + cmdName;
                                    if (cmd.getParameters().size() > 0) {
                                        usage += " " + getParameterUsage(cmd);
                                    }
                                    sender.sendMessage(ChatColor.GRAY + "- " + usage);
                                    break; // Only show first matching name
                                }
                            }
                        }
                    }
                }
                return true;
            }

            return true;
        }

        @Override
        public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
            if (!(sender instanceof Player)) {
                return new ArrayList<>();
            }

            Player player = (Player) sender;
            List<String> completions = new ArrayList<>();

            // Get available commands for this root
            List<CommandData> availableCommands = registeredRootCommands.get(rootCommand.toLowerCase());
            if (availableCommands == null) {
                return completions;
            }

            // Build the current partial command (what the user has typed so far)
            String currentPartialCommand = alias.toLowerCase();
            if (args.length > 0) {
                currentPartialCommand += " " + String.join(" ", args).toLowerCase();
            }

            // Check if we have an exact command match first
            CommandData exactMatch = null;
            String exactCommandName = null;

            for (CommandData cmd : availableCommands) {
                if (!cmd.canAccess(sender)) continue;

                for (String cmdName : cmd.getNames()) {
                    String cmdLower = cmdName.toLowerCase();
                    if (cmdLower.equals(currentPartialCommand.trim())) {
                        exactMatch = cmd;
                        exactCommandName = cmdName;
                        break;
                    }
                }
                if (exactMatch != null) break;
            }

            // If we have an exact match, tab complete the parameters
            if (exactMatch != null && exactCommandName != null) {
                String[] cmdParts = exactCommandName.split(" ");
                int parameterIndex = args.length - (cmdParts.length - 1);

                if (parameterIndex > 0 && parameterIndex <= exactMatch.getParameters().size()) {
                    ParameterData paramData = exactMatch.getParameters().get(parameterIndex - 1);
                    String currentParam = args.length > 0 ? args[args.length - 1] : "";

                    return tabCompleteParameter(player, currentParam,
                            paramData.getParameterClass(), paramData.getTabCompleteFlags());
                }
                return completions; // No more parameters to complete
            }

            // Otherwise, complete sub-commands
            String partialLastWord = args.length > 0 ? args[args.length - 1].toLowerCase() : "";
            String commandSoFar = alias.toLowerCase();
            if (args.length > 1) {
                commandSoFar += " " + String.join(" ", Arrays.copyOf(args, args.length - 1)).toLowerCase();
            }

            for (CommandData cmd : availableCommands) {
                if (!cmd.canAccess(sender)) continue;

                for (String cmdName : cmd.getNames()) {
                    String cmdLower = cmdName.toLowerCase();

                    // Check if this command could be a completion
                    if (cmdLower.startsWith(commandSoFar)) {
                        // Get the remaining part after what's already typed
                        String remaining = cmdLower.substring(commandSoFar.length());
                        if (remaining.startsWith(" ")) {
                            remaining = remaining.substring(1); // Remove leading space
                        }

                        if (!remaining.isEmpty()) {
                            String nextWord = remaining.split(" ")[0];

                            // If we're typing a partial word, check if it matches
                            if (partialLastWord.isEmpty() || nextWord.startsWith(partialLastWord)) {
                                if (!completions.contains(nextWord)) {
                                    completions.add(nextWord);
                                }
                            }
                        }
                    }
                }
            }

            return completions;
        }

        /**
         * Generate usage string for command parameters
         */
        private String getParameterUsage(CommandData cmd) {
            StringBuilder usage = new StringBuilder();
            for (ParameterData param : cmd.getParameters()) {
                if (usage.length() > 0) usage.append(" ");
                usage.append("<").append(param.getName() != null ? param.getName() : "param").append(">");
            }
            return usage.toString();
        }
    }

    /**
     * @return the full command line input of a player before running or tab completing a Core command
     */
    public static String[] getParameters(Player player) {
        return CommandMap.parameters.get(player.getUniqueId());
    }

    /**
     * Process a command (permission checks, argument validation, etc.)
     */
    public CommandData evalCommand(final CommandSender sender, String command) {
        String[] args = new String[]{};
        CommandData found = null;

        CommandLoop:
        for (CommandData commandData : commands) {
            for (String alias : commandData.getNames()) {
                String messageString = command.toLowerCase() + " ";
                String aliasString = alias.toLowerCase() + " ";

                if (messageString.startsWith(aliasString)) {
                    found = commandData;

                    if (messageString.length() > aliasString.length()) {
                        if (found.getParameters().size() == 0) {
                            continue;
                        }
                    }

                    if (command.length() > alias.length() + 1) {
                        args = (command.substring(alias.length() + 1)).split(" ");
                    }

                    break CommandLoop;
                }
            }
        }

        if (found == null) {
            return (null);
        }

        if (!(sender instanceof Player) && !found.isConsoleAllowed()) {
            sender.sendMessage(ChatColor.RED + "This command does not support execution from the console.");
            return (found);
        }

        if (!found.canAccess(sender)) {
            sender.sendMessage(ChatUtil.translate("&cVous n'avez pas la permission d'executer cette commande."));
            return (found);
        }

        if (found.isAsync()) {
            final CommandData foundClone = found;
            final String[] argsClone = args;

            new BukkitRunnable() {
                public void run() {
                    foundClone.execute(sender, argsClone);
                }
            }.runTaskAsynchronously(plugin);
        } else {
            found.execute(sender, args);
        }

        return (found);
    }

    /**
     * Transforms a parameter.
     */
    static Object transformParameter(CommandSender sender, String parameter, Class<?> transformTo) {
        if (transformTo.equals(String.class)) {
            return (parameter);
        }
        return (parameterTypes.get(transformTo).transform(sender, parameter));
    }

    /**
     * Tab completes a parameter.
     */
    static List<String> tabCompleteParameter(Player sender, String parameter, Class<?> transformTo, String[] tabCompleteFlags) {
        if (!parameterTypes.containsKey(transformTo)) {
            return (new ArrayList<>());
        }
        return (parameterTypes.get(transformTo).tabComplete(sender, ImmutableSet.copyOf(tabCompleteFlags), parameter));
    }

    /**
     * Initiates the command handler.
     */
    public void hook() {
        Preconditions.checkState(!initiated);
        initiated = true;

        registerParameterType(boolean.class, new BooleanParameterType());
        registerParameterType(float.class, new FloatParameterType());
        registerParameterType(double.class, new DoubleParameterType());
        registerParameterType(int.class, new IntegerParameterType());
        registerParameterType(OfflinePlayer.class, new OfflinePlayerParameterType());
        registerParameterType(Player.class, new PlayerParameterType());
        registerParameterType(String.class, new StringParameterType());
    }

    // Remove the event handlers since we're using direct registration now
    // Keep them commented out in case you need fallback behavior
    /*
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCommandPreProcess(PlayerCommandPreprocessEvent event) {
        String command = event.getMessage().substring(1);
        CommandMap.parameters.put(event.getPlayer().getUniqueId(), command.split(" "));

        if (evalCommand(event.getPlayer(), command) != null) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onConsoleCommand(ServerCommandEvent event) {
        if (evalCommand(event.getSender(), event.getCommand()) != null) {
            event.setCancelled(true);
        }
    }
    */

    // Erreur sous paper
    /*@SneakyThrows
    public static SimpleCommandMap getCommandMap() {
        final Class<?> craftServerClass = Reflection.getOBCClass("CraftServer");
        assert craftServerClass != null;
        final Method getCommandMapMethod = craftServerClass.getMethod("getCommandMap");
        return (SimpleCommandMap) getCommandMapMethod.invoke(craftServerClass.cast(Bukkit.getServer()), new Object[0]);
    }*/

    @SneakyThrows
    public static SimpleCommandMap getCommandMap() {
        Object server = Bukkit.getServer();
        Field commandMapField = server.getClass().getDeclaredField("commandMap");
        commandMapField.setAccessible(true);
        return (SimpleCommandMap) commandMapField.get(server);
    }
}