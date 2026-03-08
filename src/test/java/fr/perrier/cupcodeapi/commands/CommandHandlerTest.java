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

        // Also add to handler.registeredRootCommands via reflection so onTabComplete can find it
        java.lang.reflect.Field rrField = CommandHandler.class.getDeclaredField("registeredRootCommands");
        rrField.setAccessible(true);
        @SuppressWarnings("unchecked")
        java.util.Map<String, java.util.List<CommandData>> rrMap = (java.util.Map<String, java.util.List<CommandData>>) rrField.get(handler);
        rrMap.computeIfAbsent("root", k -> new java.util.ArrayList<>()).add(cmdData);

        // Create a MultiCommandExecutor for the root via reflection
        Class<?> execClass = Class.forName("fr.perrier.cupcodeapi.commands.CommandHandler$MultiCommandExecutor");
        Constructor<?>[] ctors = execClass.getDeclaredConstructors();
        Constructor<?> found = null;
        for (Constructor<?> c : ctors) {
            if (c.getParameterCount() >= 2) {
                found = c;
                break;
            }
        }
        assertNotNull(found, "Could not find MultiCommandExecutor constructor");
        found.setAccessible(true);

        Object exec;
        if (found.getParameterCount() == 2) {
            exec = found.newInstance(handler, handler);
        } else {
            exec = found.newInstance(handler, "root", handler);
        }
        assertNotNull(exec);

        CommandSender sender = mock(Player.class);
        when(((Player) sender).getName()).thenReturn("tester");
        when(((Player) sender).canSee(any())).thenReturn(true);

        // Case: user typed 'root sub p' and presses tab
        String alias = "root";
        String[] args = new String[]{"sub", "p"};

        // Invoke onTabComplete via reflection
        Method onTabComplete = execClass.getMethod("onTabComplete", CommandSender.class, Command.class, String.class, String[].class);
        @SuppressWarnings("unchecked")
        List<String> completions = (List<String>) onTabComplete.invoke(exec, sender, mock(Command.class), alias, args);

        assertNotNull(completions);
        // Ensure we got suggestions from the test ParameterType
        assertFalse(completions.isEmpty(), "Expected completions to be non-empty");
        assertTrue(completions.stream().anyMatch(s -> s.equalsIgnoreCase("pear") || s.equalsIgnoreCase("peach")), "Expected pear or peach in completions");
    }
}
