package fr.perrier.cupcodeapi.textdisplay.events;

import fr.perrier.cupcodeapi.textdisplay.TextDisplayManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.TextDisplay;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;

/**
 * Listener to handle clicks on TextDisplay buttons.
 */
public class TextDisplayClickListener implements Listener {

    /**
     * Handle player interaction with an entity for TextDisplay buttons.
     *
     * @param event The player interact entity event.
     */
    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (!(event.getRightClicked() instanceof Interaction interaction)) return;

        // Récupérer les métadonnées
        String buttonId = getMetadata(interaction, "BUTTON_ID");
        String targetPlayer = getMetadata(interaction, "TARGET_PLAYER");

        if (buttonId == null) return;

        // Vérifier si le joueur est autorisé à interagir
        if (!"ALL".equals(targetPlayer) && !event.getPlayer().getUniqueId().toString().equals(targetPlayer)) {
            return;
        }

        // Trouver le TextDisplay parent
        TextDisplay parentDisplay = findNearbyTextDisplay(interaction);
        if (parentDisplay == null) return;

        // Trouver l'instance de TextDisplay
        TextDisplayManager.getInstance().getDisplayByEntity(parentDisplay)
                .ifPresent(displayInstance -> {
                    TextDisplayClickEvent clickEvent = new TextDisplayClickEvent(
                            event.getPlayer(),
                            displayInstance,
                            buttonId
                    );
                    Bukkit.getPluginManager().callEvent(clickEvent);
                });
    }

    private String getMetadata(Interaction interaction, String key) {
        return interaction.hasMetadata(key)
                ? interaction.getMetadata(key).get(0).asString()
                : null;
    }

    private TextDisplay findNearbyTextDisplay(Interaction interaction) {
        return interaction.getLocation().getWorld()
                .getNearbyEntities(interaction.getLocation(), 5, 5, 5)
                .stream()
                .filter(entity -> entity instanceof TextDisplay)
                .map(entity -> (TextDisplay) entity)
                .findFirst()
                .orElse(null);
    }
}