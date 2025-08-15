package fr.perrier.cupcodeapi.textdisplay;

import fr.perrier.cupcodeapi.CupCodeAPI;
import fr.perrier.cupcodeapi.textdisplay.events.TextDisplayClickEvent;
import fr.perrier.cupcodeapi.textdisplay.hover.HoverBehavior;
import fr.perrier.cupcodeapi.textdisplay.hover.ButtonTextDisplay;
import lombok.Getter;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInputEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.util.*;
import java.util.function.Consumer;

/**
 * Represents a TextDisplay instance with its properties and buttons.
 */
@Getter
public class TextDisplayInstance {
    private final UUID id;
    private final TextDisplay textDisplay;
    private final Player targetPlayer;
    private final Location location;
    private final int expirationTime;

    private final Map<UUID,String> buttonNames;
    private final Set<ButtonTextDisplay> buttons;

    private final Set<TextDisplay> displayButtons;

    private final Map<String, Consumer<TextDisplayClickEvent>> clickHandlers = new HashMap<>();

    // Propriétés pour le hover (null si pas hoverable)
    private final HoverBehavior hoverBehavior;

    private static final Set<TextDisplayInstance> instances = new HashSet<>();

    /**
     * Create a new TextDisplayInstance.
     *
     * @param id The unique ID.
     * @param textDisplay The TextDisplay entity.
     * @param targetPlayer The target player.
     * @param location The location.
     * @param expirationTime Expiration time in seconds.
     */
    public TextDisplayInstance(UUID id, TextDisplay textDisplay, Player targetPlayer,
                               Location location, int expirationTime) {
        this(id, textDisplay, targetPlayer, location, expirationTime, null);
    }

    /**
     * Create a new TextDisplayInstance with hover behavior.
     *
     * @param id The unique ID.
     * @param textDisplay The TextDisplay entity.
     * @param targetPlayer The target player.
     * @param location The location.
     * @param expirationTime Expiration time in seconds.
     * @param hoverBehavior The hover behavior.
     */
    public TextDisplayInstance(UUID id, TextDisplay textDisplay, Player targetPlayer,
                               Location location, int expirationTime, HoverBehavior hoverBehavior) {
        this.id = id;
        this.textDisplay = textDisplay;
        this.targetPlayer = targetPlayer;
        this.location = location;
        this.expirationTime = expirationTime;
        this.hoverBehavior = hoverBehavior;
        this.buttonNames = new HashMap<>();
        this.buttons = new HashSet<>();
        this.displayButtons = new HashSet<>();
        
        instances.add(this);
    }

    /**
     * Check if this display is hoverable.
     *
     * @return true if hoverable, false otherwise.
     */
    public boolean isHoverable() {
        return hoverBehavior != null;
    }

    /**
     * Update the hover state for this display and its hoverable buttons.
     */
    public void updateHoverState() {
        if (hoverBehavior != null) {
            hoverBehavior.updateHoverState();
        }

        // Mettre à jour aussi les boutons survolables
        buttons.forEach(ButtonTextDisplay::updateHoverState);
    }

    /**
     * Add a button to this display.
     *
     * @param buttonId The button ID.
     * @param button The button instance.
     */
    public void addButton(String buttonId, ButtonTextDisplay button) {
        buttonNames.put(button.getId(), buttonId);
        buttons.add(button);
    }

    /**
     * Add a display button to this display.
     *
     * @param button The display button.
     */
    public void addDisplayButton(TextDisplay button) {
        displayButtons.add(button);
    }

    /**
     * Get a button by its ID.
     *
     * @param buttonId The button ID.
     * @return An Optional containing the button if found.
     */
    public Optional<ButtonTextDisplay> getButton(String buttonId) {
        return Optional.ofNullable(buttons.stream().filter(button -> buttonNames.get(button.getId()).equals(buttonId)).findFirst().orElse(null));
    }

    /**
     * Destroy this display and all its buttons.
     */
    public void destroy() {
        instances.remove(this);

        // Supprimer tous les boutons
        buttons.forEach(ButtonTextDisplay::destroy);
        displayButtons.forEach(TextDisplay::remove);

        // Supprimer le TextDisplay
        if (textDisplay != null && !textDisplay.isDead()) {
            textDisplay.remove();
        }
    }

    /**
     * Update the text of the display.
     *
     * @param newText The new text.
     */
    public void updateText(String newText) {
        if (textDisplay != null && !textDisplay.isDead()) {
            textDisplay.setText(newText);
        }
    }

    /**
     * Update the background color of the display.
     *
     * @param color The new color.
     */
    public void updateBackgroundColor(Color color) {
        if (textDisplay != null && !textDisplay.isDead()) {
            textDisplay.setBackgroundColor(color);
        }
    }

    /**
     * Register a click handler for a button.
     *
     * @param buttonId The ID of the button to handle clicks for.
     * @param clickHandler The handler to be called when the button is clicked.
     * @return This TextDisplayInstance for chaining.
     */
    public TextDisplayInstance onClick(String buttonId, Consumer<TextDisplayClickEvent> clickHandler) {
        if (buttonNames.containsValue(buttonId)) {
            clickHandlers.put(buttonId, clickHandler);
        } else {
            throw new IllegalArgumentException("No button found with ID: " + buttonId);
        }
        return this;
    }

    /**
     * Handle a click event on this display's buttons.
     *
     * @param event The event.
     * @param buttonId The ID of the button that was clicked.
     */
    public void handleClick(TextDisplayClickEvent event, String buttonId) {
        if (clickHandlers.containsKey(buttonId)) {
            clickHandlers.get(buttonId).accept(event);
        }
    }

    /**
     * Register the click listener for this display.
     */
    public static void registerGlobalListener() {

        Bukkit.getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onPlayerInput(PlayerInteractEvent event) {
                Player player = event.getPlayer();
                if(event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK ) {
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
                                    displayInstance.handleClick(clickEvent, buttonId);
                                });
                    }
                }
            }
        }, CupCodeAPI.getPlugin());
    }

    private static String getMetadata(Interaction interaction, String key) {
        return interaction.hasMetadata(key)
                ? interaction.getMetadata(key).get(0).asString()
                : null;
    }

    private static TextDisplay findNearbyTextDisplay(TextDisplay textDisplay) {
        return textDisplay.getLocation().getWorld()
                .getNearbyEntities(textDisplay.getLocation(), 5, 5, 5)
                .stream()
                .filter(entity -> entity instanceof TextDisplay)
                .map(entity -> (TextDisplay) entity)
                .findFirst()
                .orElse(null);
    }
}