package fr.perrier.cupcodeapi.utils.item;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

public class CustomItemListener implements Listener {

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getItem() == null) return;
        CustomItem customItem = CustomItem.getCustomItem(event.getItem());
        if (customItem == null) return;
        if (customItem.getCallable() == null) return;
        boolean rightClick = (event.getAction().name().contains("RIGHT"));
        customItem.getCallable().accept(new CustomItemEvent(event.getPlayer(), event.getItem(), rightClick,event.getClickedBlock()));
    }

}
