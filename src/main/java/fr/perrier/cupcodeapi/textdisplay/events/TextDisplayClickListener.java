package fr.perrier.cupcodeapi.textdisplay.events;

import fr.perrier.cupcodeapi.textdisplay.TextDisplayManager;
import fr.perrier.cupcodeapi.textdisplay.hover.ButtonTextDisplay;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

/**
 * Listener to handle clicks on TextDisplay buttons.
 */
public class TextDisplayClickListener implements Listener {

    @EventHandler
    public void onPlayerInput(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if(event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK) {
            if(ButtonTextDisplay.getHoveredDisplays().containsKey(player.getUniqueId())) {

                ButtonTextDisplay hoveredDisplay = ButtonTextDisplay.getHoveredDisplays().get(player.getUniqueId());

                TextDisplayManager.getInstance().getInstanceOfButton(hoveredDisplay.getId())
                        .ifPresent(displayInstance -> {
                            String buttonId = displayInstance.getButtonNames().get(hoveredDisplay.getId());

                            TextDisplayClickEvent clickEvent = new TextDisplayClickEvent(
                                    player,
                                    displayInstance,
                                    buttonId
                            );
                            Bukkit.getPluginManager().callEvent(clickEvent);
                        });
            }
        }
    }
}