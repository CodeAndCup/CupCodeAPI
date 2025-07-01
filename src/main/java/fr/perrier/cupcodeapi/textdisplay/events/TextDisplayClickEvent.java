package fr.perrier.cupcodeapi.textdisplay.events;

import fr.perrier.cupcodeapi.textdisplay.TextDisplayInstance;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Event triggered when a TextDisplay button is clicked.
 */
@Getter
public class TextDisplayClickEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private final TextDisplayInstance displayInstance;
    private final String buttonId;

    /**
     * Create a new TextDisplayClickEvent.
     *
     * @param player The player who clicked.
     * @param displayInstance The display instance.
     * @param buttonId The button ID.
     */
    public TextDisplayClickEvent(Player player, TextDisplayInstance displayInstance, String buttonId) {
        this.player = player;
        this.displayInstance = displayInstance;
        this.buttonId = buttonId;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    /**
     * Get the HandlerList for this event.
     *
     * @return The HandlerList.
     */
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}