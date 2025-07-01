package fr.perrier.cupcodeapi.menuapi.buttons;


import com.cryptomorin.xseries.XMaterial;
import fr.perrier.cupcodeapi.utils.*;
import fr.perrier.cupcodeapi.menuapi.*;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public class ConfirmationButton extends Button {
    private final boolean confirm;
    private final TypeCallback<Boolean> callback;
    private final boolean closeAfterResponse;

    public ConfirmationButton(final boolean confirm, final TypeCallback<Boolean> callback, final boolean closeAfterResponse) {
        this.confirm = confirm;
        this.callback = callback;
        this.closeAfterResponse = closeAfterResponse;
    }

    @Override
    public ItemStack getButtonItem(final Player player) {
        final ItemBuilder item = new ItemBuilder(XMaterial.WHITE_WOOL.parseMaterial());
        item.setDurability(this.confirm ? 5 : 14);
        item.setName(ChatColor.translateAlternateColorCodes('*', this.confirm ? "&aConfirm" : "&cCancel"));
        return item.toItemStack();
    }

    @Override
    public void clicked(final Player player, final int i, final ClickType clickType, final int hb) {
        if (this.confirm) {
            player.playSound(player.getLocation(), ButtonSound.SUCCESS.getSound(), 20.0f, 0.1f);
        } else {
            player.playSound(player.getLocation(), ButtonSound.FAIL.getSound(), 20.0f, 0.1f);
        }
        if (this.closeAfterResponse) {
            player.closeInventory();
        }
        this.callback.callback(this.confirm);
    }
}
