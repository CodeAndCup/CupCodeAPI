package fr.perrier.cupcodeapi.textdisplay;

import fr.perrier.cupcodeapi.CupCodeAPI;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Central manager for all TextDisplays.
 */
public class TextDisplayManager {
    private static TextDisplayManager instance;
    private final Map<UUID, TextDisplayInstance> displays = new ConcurrentHashMap<>();
    private final Map<Player, Set<UUID>> playerDisplays = new ConcurrentHashMap<>();
    private BukkitTask hoverTask;

    private TextDisplayManager() {
        startHoverDetection();
    }

    /**
     * Get the singleton instance of the TextDisplayManager.
     *
     * @return The TextDisplayManager instance.
     */
    public static TextDisplayManager getInstance() {
        if (instance == null) {
            instance = new TextDisplayManager();
        }
        return instance;
    }

    /**
     * Register a new TextDisplayInstance.
     *
     * @param display The display instance to register.
     */
    public void registerDisplay(TextDisplayInstance display) {
        displays.put(display.getId(), display);

        if (display.getTargetPlayer() != null) {
            playerDisplays.computeIfAbsent(display.getTargetPlayer(), k -> new HashSet<>())
                    .add(display.getId());
        }

        // Gestion de l'expiration
        if (display.getExpirationTime() > 0) {
            CupCodeAPI.getPlugin().getServer().getScheduler().runTaskLater(
                    CupCodeAPI.getPlugin(),
                    () -> removeDisplay(display.getId()),
                    display.getExpirationTime() * 20L
            );
        }
    }

    /**
     * Remove a TextDisplayInstance by its UUID.
     *
     * @param displayId The UUID of the display to remove.
     */
    public void removeDisplay(UUID displayId) {
        TextDisplayInstance display = displays.remove(displayId);
        if (display != null) {
            display.destroy();

            // Nettoyer les références par joueur
            playerDisplays.values().forEach(set -> set.remove(displayId));
        }
    }

    /**
     * Get a display instance by its UUID.
     *
     * @param displayId The UUID of the display.
     * @return An Optional containing the display instance if found.
     */
    public Optional<TextDisplayInstance> getDisplay(UUID displayId) {
        return Optional.ofNullable(displays.get(displayId));
    }

    /**
     * Get a display instance by its TextDisplay entity.
     *
     * @param entity The TextDisplay entity.
     * @return An Optional containing the display instance if found.
     */
    public Optional<TextDisplayInstance> getDisplayByEntity(TextDisplay entity) {
        return displays.values().stream()
                .filter(display -> display.getTextDisplay().equals(entity))
                .findFirst();
    }

    /**
     * Get a display instance by its button ID.
     *
     * @param buttonId The button ID.
     * @return An Optional containing the display instance if found.
     */
    public Optional<TextDisplayInstance> getInstanceOfButton(UUID buttonId) {
        return displays.values().stream()
                .filter(display -> display.getButtonNames().containsKey(buttonId))
                .findFirst();
    }

    /**
     * Get all display instances associated with a player.
     *
     * @param player The player.
     * @return A set of TextDisplayInstances for the player.
     */
    public Set<TextDisplayInstance> getPlayerDisplays(Player player) {
        return playerDisplays.getOrDefault(player, Collections.emptySet())
                .stream()
                .map(displays::get)
                .filter(Objects::nonNull)
                .collect(HashSet::new, HashSet::add, HashSet::addAll);
    }

    private void startHoverDetection() {
        hoverTask = CupCodeAPI.getPlugin().getServer().getScheduler().runTaskTimer(
                CupCodeAPI.getPlugin(),
                () -> {
                    for (TextDisplayInstance display : displays.values()) {
                        display.updateHoverState();
                    }
                },
                0L, 2L
        );
    }

    /**
     * Shutdown the manager, canceling tasks and removing all displays.
     */
    public void shutdown() {
        if (hoverTask != null) {
            hoverTask.cancel();
        }

        new ArrayList<>(displays.values()).forEach(TextDisplayInstance::destroy);
        displays.clear();
        playerDisplays.clear();
    }
}
