package fr.perrier.cupcodeapi;

import fr.perrier.cupcodeapi.textdisplay.TextDisplayInstance;
import fr.perrier.cupcodeapi.textdisplay.TextDisplayManager;
import fr.perrier.cupcodeapi.textdisplay.events.TextDisplayClickListener;
import fr.perrier.cupcodeapi.utils.item.CustomItemListener;
import org.bukkit.plugin.java.JavaPlugin;

public class CupCodeAPI {
    private static JavaPlugin pluginInstance;

    /**
     * Initialize the CupCodeAPI with the given plugin instance.
     *
     * @param plugin The main plugin instance.
     */
    public static void enable(JavaPlugin plugin) {
        if (pluginInstance == null) {
            pluginInstance = plugin;
        }
        pluginInstance.getServer().getPluginManager().registerEvents(new TextDisplayClickListener(), pluginInstance);
        pluginInstance.getServer().getPluginManager().registerEvents(new CustomItemListener(), pluginInstance);
        TextDisplayInstance.registerGlobalListener();
    }

    /**
     * Disable the CupCodeAPI and clean up resources.
     */
    public static void disable() {
        TextDisplayManager.getInstance().shutdown();
    }

    /**
     * Get the plugin instance associated with the CupCodeAPI.
     *
     * @return The JavaPlugin instance.
     * @throws IllegalStateException if the API has not been initialized.
     */
    public static JavaPlugin getPlugin() {
        if (pluginInstance == null) {
            throw new IllegalStateException("CupCodeAPI has not been initialized. Please call CupCodeAPI.setPlugin() in your plugin's onEnable().");
        }
        return pluginInstance;
    }
}
