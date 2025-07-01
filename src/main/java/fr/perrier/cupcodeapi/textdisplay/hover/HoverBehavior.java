package fr.perrier.cupcodeapi.textdisplay.hover;

import lombok.Getter;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;

import static org.bukkit.Bukkit.getServer;

/**
 * Comportement de survol pour un TextDisplay
 */
@Getter
public class HoverBehavior {
    private final TextDisplay textDisplay;
    private final Player targetPlayer;

    private final String originalText;
    private final String hoveredText;
    private final Color originalBackgroundColor;
    private final Color hoveredBackgroundColor;
    private final float originalScale;
    private final float hoverScale;
    private final double detectionRange;

    private boolean isHovered = false;

    public HoverBehavior(TextDisplay textDisplay, Player targetPlayer, String originalText,
                         String hoveredText, Color originalBackgroundColor, Color hoveredBackgroundColor,
                         float originalScale, float hoverScale, double detectionRange) {
        this.textDisplay = textDisplay;
        this.targetPlayer = targetPlayer;
        this.originalText = originalText;
        this.hoveredText = hoveredText;
        this.originalBackgroundColor = originalBackgroundColor;
        this.hoveredBackgroundColor = hoveredBackgroundColor;
        this.originalScale = originalScale;
        this.hoverScale = hoverScale;
        this.detectionRange = detectionRange;
    }

    public void updateHoverState() {
        if (targetPlayer != null) {
            checkHover(targetPlayer);
        } else {
            for (Player player : getServer().getOnlinePlayers()) {
                checkHover(player);
            }
        }
    }

    private void checkHover(Player player) {
        if (textDisplay.isDead()) return;

        Location eyeLoc = player.getEyeLocation();
        Vector direction = eyeLoc.getDirection();

        RayTraceResult result = player.getWorld().rayTrace(
                eyeLoc,
                direction,
                detectionRange,
                FluidCollisionMode.NEVER,
                true,
                0.2,
                entity -> entity.equals(textDisplay)
        );

        boolean newHoverState = result != null && result.getHitEntity() != null;

        if (newHoverState != isHovered) {
            isHovered = newHoverState;
            updateAppearance();

            if (isHovered) {
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HARP, 0.5f, 1.5f);
            }
        }
    }

    private void updateAppearance() {
        if (textDisplay.isDead()) return;

        // Mettre à jour le texte
        if (isHovered && hoveredText != null) {
            textDisplay.setText(hoveredText);
        } else {
            textDisplay.setText(originalText);
        }

        // Mettre à jour la couleur de fond
        if (isHovered && hoveredBackgroundColor != null) {
            textDisplay.setBackgroundColor(hoveredBackgroundColor);
        } else if (originalBackgroundColor != null) {
            textDisplay.setBackgroundColor(originalBackgroundColor);
        }

        // Mettre à jour l'échelle
        updateScale();
    }

    private void updateScale() {
        if (textDisplay.isDead()) return;

        float currentScale = isHovered ? (originalScale * hoverScale) : originalScale;

        Transformation transformation = textDisplay.getTransformation();
        transformation.getScale().set(currentScale, currentScale, currentScale);
        textDisplay.setTransformation(transformation);
    }
}