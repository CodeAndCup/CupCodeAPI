package fr.perrier.cupcodeapi.commands;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class CommandHandlerTest {

    JavaPlugin plugin;
    CommandHandler handler;

    @BeforeEach
    public void setUp() throws Exception {
        plugin = mock(JavaPlugin.class);
        Server server = mock(Server.class);
        PluginManager pluginManager = mock(PluginManager.class);
        when(plugin.getServer()).thenReturn(server);
        when(server.getPluginManager()).thenReturn(pluginManager);
        // registerEvents does nothing
        doNothing().when(pluginManager).registerEvents(any(), any());

        handler = new CommandHandler(plugin);
        handler.hook();

        // Clear any existing commands in the static list to avoid interference
        CommandHandler.commands.clear();
    }

    // On crée une commande factice via reflection pour tester la complétion
    public static class FakeCommands {
        @fr.perrier.cupcodeapi.commands.annotations.Command(names = {"root sub"}, permission = "", async = false)
        public static void testCommand(Player sender, @fr.perrier.cupcodeapi.commands.annotations.Param(name = "mode", tabCompleteFlags = {""}) String mode) {
            // no-op
        }

        // Simple root-only command (no parameters) to test basic sub-command completion
        @fr.perrier.cupcodeapi.commands.annotations.Command(names = {"root"}, permission = "", async = false)
        public static void rootOnly(Player sender) {
            // no-op
        }
    }

    @Test
    public void testParameterTabCompleteAfterFirstLetter() throws Exception {
        // Build CommandData manually (avoid calling registerCommands which interacts with Bukkit)
        // Register a test ParameterType for String that returns suggestions
        fr.perrier.cupcodeapi.commands.annotations.ParameterType<String> testStringType = new fr.perrier.cupcodeapi.commands.annotations.ParameterType<String>() {
            @Override
            public String transform(org.bukkit.command.CommandSender sender, String source) {
                return source;
            }

            @Override
            public java.util.List<String> tabComplete(org.bukkit.entity.Player sender, java.util.Set<String> flags, String source) {
                java.util.List<String> all = java.util.Arrays.asList("pear", "peach", "apple");
                java.util.List<String> out = new java.util.ArrayList<>();
                for (String s : all) {
                    if (source == null || source.isEmpty() || s.toLowerCase().startsWith(source.toLowerCase())) out.add(s);
                }
                return out;
            }
        };
        CommandHandler.registerParameterType(String.class, testStringType);

        // Create CommandData for 'root sub' command (with a String parameter)
        Method method = FakeCommands.class.getMethod("testCommand", Player.class, String.class);
        fr.perrier.cupcodeapi.commands.annotations.Command cmdAnn = method.getAnnotation(fr.perrier.cupcodeapi.commands.annotations.Command.class);
        java.lang.annotation.Annotation[] paramAnnos = method.getParameterAnnotations()[1];
        fr.perrier.cupcodeapi.commands.annotations.Param paramAnn = null;
        for (java.lang.annotation.Annotation a : paramAnnos) {
            if (a instanceof fr.perrier.cupcodeapi.commands.annotations.Param) {
                paramAnn = (fr.perrier.cupcodeapi.commands.annotations.Param) a;
                break;
            }
        }
        assertNotNull(paramAnn);

        List<fr.perrier.cupcodeapi.commands.annotations.ParameterData> params = new ArrayList<>();
        params.add(new fr.perrier.cupcodeapi.commands.annotations.ParameterData(paramAnn, String.class));

        CommandData cmdData = new CommandData(cmdAnn, params, method, method.getParameterTypes()[0].isAssignableFrom(Player.class));
        CommandHandler.commands.add(cmdData);

        // Create CommandData for simple 'root' command (no params)
        Method rootMethod = FakeCommands.class.getMethod("rootOnly", Player.class);
        fr.perrier.cupcodeapi.commands.annotations.Command rootAnn = rootMethod.getAnnotation(fr.perrier.cupcodeapi.commands.annotations.Command.class);
        CommandData rootCmdData = new CommandData(rootAnn, new ArrayList<>(), rootMethod, rootMethod.getParameterTypes()[0].isAssignableFrom(Player.class));
        CommandHandler.commands.add(rootCmdData);

        // Also add both to handler.registeredRootCommands via reflection so onTabComplete can find them
        java.lang.reflect.Field rrField = CommandHandler.class.getDeclaredField("registeredRootCommands");
        rrField.setAccessible(true);
        @SuppressWarnings("unchecked")
        java.util.Map<String, java.util.List<CommandData>> rrMap = (java.util.Map<String, java.util.List<CommandData>>) rrField.get(handler);
        rrMap.computeIfAbsent("root", k -> new java.util.ArrayList<>()).add(cmdData);
        rrMap.computeIfAbsent("root", k -> new java.util.ArrayList<>()).add(rootCmdData);

        // Create a MultiCommandExecutor for the root via reflection
        Class<?> execClass = Class.forName("fr.perrier.cupcodeapi.commands.CommandHandler$MultiCommandExecutor");
        Constructor<?>[] ctors = execClass.getDeclaredConstructors();
        Constructor<?> found = null;
        for (Constructor<?> c : ctors) {
            Class<?>[] pts = c.getParameterTypes();
            // look for constructor with signature (CommandHandler, String, CommandHandler) -> param types[1] == String
            if (pts.length >= 2 && pts[1].equals(String.class)) {
                found = c;
                break;
            }
        }
        if (found == null) {
            // fallback: pick any non-synthetic constructor
            for (Constructor<?> c : ctors) {
                if (!c.isSynthetic()) { found = c; break; }
            }
        }
        assertNotNull(found, "Could not find MultiCommandExecutor constructor");
        found.setAccessible(true);

        Object exec;
        Class<?>[] ctorParams = found.getParameterTypes();
        if (ctorParams.length >= 2 && ctorParams[1].equals(String.class)) {
            // signature: (outer, String, CommandHandler)
            exec = found.newInstance(handler, "root", handler);
        } else if (ctorParams.length >= 1 && ctorParams[0].equals(String.class)) {
            // weird signature: (String, CommandHandler)
            exec = found.newInstance("root", handler);
        } else {
            // last resort: try (handler, handler)
            exec = found.newInstance(handler, handler);
        }
        assertNotNull(exec);

        Player sender = mock(Player.class);
        when(sender.getName()).thenReturn("tester");
        when(sender.canSee(any())).thenReturn(true);

        // First: simulate typing `/root ` then pressing tab (empty partial) -> should suggest the sub-command 'sub'
        String alias = "root";
        String[] argsEmptyPartial = new String[]{""};
        Method onTabComplete = execClass.getMethod("onTabComplete", CommandSender.class, Command.class, String.class, String[].class);
        @SuppressWarnings("unchecked")
        List<String> completionsForSub = (List<String>) onTabComplete.invoke(exec, sender, mock(Command.class), alias, argsEmptyPartial);
        System.out.println("DEBUG completionsForSub=" + completionsForSub);
        assertNotNull(completionsForSub);
        assertTrue(completionsForSub.stream().anyMatch(s -> s.equalsIgnoreCase("sub")), "Expected 'sub' in completions when typing '/root '");

        // Then: user typed 'root sub p' and presses tab -> should return completions for String param (pear/peach)
        String[] args = new String[]{"sub", "p"};
        @SuppressWarnings("unchecked")
        List<String> completions = (List<String>) onTabComplete.invoke(exec, sender, mock(Command.class), alias, args);

        assertNotNull(completions);
        // Ensure we got suggestions from the test ParameterType
        assertFalse(completions.isEmpty(), "Expected completions to be non-empty");
        assertTrue(completions.stream().anyMatch(s -> s.equalsIgnoreCase("pear") || s.equalsIgnoreCase("peach")), "Expected pear or peach in completions");
    }
}
