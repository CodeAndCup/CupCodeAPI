package fr.perrier.cupcodeapi.menuapi.buttons;


import fr.perrier.cupcodeapi.*;
import fr.perrier.cupcodeapi.utils.*;
import fr.perrier.cupcodeapi.menuapi.*;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.conversations.*;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.function.BiConsumer;

public class ConversationButton<T> extends Button {
    final ItemStack item;
    final String message;
    final T target;
    final BiConsumer<T, Pair<ConversationContext, String>> action;

    public ConversationButton(ItemStack item, T target, String message, BiConsumer<T, Pair<ConversationContext, String>> action) {
        this.item = item;
        this.message = message;
        this.target = target;
        this.action = action;
    }

    @Override
    public ItemStack getButtonItem(Player p0) {
        return item;
    }

    @Override
    public void clicked(Player player, int slot, ClickType clickType, int hotbarButton) {
        player.closeInventory();
        ConversationFactory factory = new ConversationFactory(CupCodeAPI.getPlugin()).withModality(true).withPrefix(new NullConversationPrefix()).withFirstPrompt(new StringPrompt() {
            @Override
            public String getPromptText(ConversationContext cc) {
                return ChatUtil.translate(message + " &7&oEnter &c&ocancel &7&oto cancel.");
            }

            @Override
            public Prompt acceptInput(ConversationContext cc, String s) {
                if (s.equalsIgnoreCase("cancel")) {
                    cc.getForWhom().sendRawMessage(ChatUtil.translate("&cCanceled."));
                    return Prompt.END_OF_CONVERSATION;
                }
                action.accept(target, Pair.of(cc, s));
                return Prompt.END_OF_CONVERSATION;
            }
        }).withLocalEcho(false).withEscapeSequence("/no").withTimeout(60).thatExcludesNonPlayersWithMessage("How did u get there???");
        player.beginConversation(factory.buildConversation(player));
    }
}
