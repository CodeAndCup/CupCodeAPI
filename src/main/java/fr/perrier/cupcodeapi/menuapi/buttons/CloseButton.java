package fr.perrier.cupcodeapi.menuapi.buttons;


import com.cryptomorin.xseries.XMaterial;
import fr.perrier.cupcodeapi.utils.*;
import fr.perrier.cupcodeapi.menuapi.*;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public class CloseButton extends Button {
    @Override
    public ItemStack getButtonItem(final Player player) {
        final ItemBuilder item = new ItemBuilder(XMaterial.INK_SAC.parseMaterial()).setDurability(1).setName(ChatColor.translateAlternateColorCodes('&', "&cClose"));
        return item.toItemStack();
    }

    @Override
    public void clicked(final Player player, final int i, final ClickType clickType, final int hb) {
        Button.playNeutral(player);
        player.closeInventory();
    }
}
