package fr.perrier.cupcodeapi.utils.item;

import lombok.RequiredArgsConstructor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.Plugin;

@RequiredArgsConstructor
public class CustomItemListener implements Listener {

    private final Plugin plugin;

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
