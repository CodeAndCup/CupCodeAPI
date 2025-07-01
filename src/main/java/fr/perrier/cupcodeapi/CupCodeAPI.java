package fr.perrier.cupcodeapi;

import fr.perrier.cupcodeapi.textdisplay.TextDisplayInstance;
import fr.perrier.cupcodeapi.textdisplay.TextDisplayManager;
import fr.perrier.cupcodeapi.textdisplay.events.TextDisplayClickListener;
import org.bukkit.plugin.java.JavaPlugin;

public class CupCodeAPI {
    private static JavaPlugin pluginInstance;

    public static void enable(JavaPlugin plugin) {
        if (pluginInstance == null) {
            pluginInstance = plugin;
        }
        pluginInstance.getServer().getPluginManager().registerEvents(new TextDisplayClickListener(), pluginInstance);
        TextDisplayInstance.registerGlobalListener();
    }

    public static void disable() {
        TextDisplayManager.getInstance().shutdown();
    }

    public static JavaPlugin getPlugin() {
        if (pluginInstance == null) {
            throw new IllegalStateException("CupCodeAPI has not been initialized. Please call CupCodeAPI.setPlugin() in your plugin's onEnable().");
        }
        return pluginInstance;
    }
}
