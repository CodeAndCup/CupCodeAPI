package fr.perrier.cupcodeapi.textdisplay.hover;

import fr.perrier.cupcodeapi.textdisplay.utils.PlanePointDetector;
import lombok.Getter;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.util.Transformation;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static org.bukkit.Bukkit.getServer;

/**
 * TextDisplay amélioré avec capacités de hover utilisant la détection de plan.
 * Permet une interaction plus précise sur toute la surface du TextDisplay.
 */
@Getter
public class ButtonTextDisplay {
    private final UUID id;
    private final TextDisplay textDisplay;
    private final Player targetPlayer;
    private final Location location;

    private final String originalText;
    private final String hoveredText;
    private final Color originalBackgroundColor;
    private final Color hoveredBackgroundColor;
    private final float originalScale;
    private final float hoverScale;
    private final double detectionRange;
    
    // Nouvelles propriétés pour la détection de plan
    private final float displayWidth;
    private final float displayHeight;

    private boolean isHovered = false;
    private PlanePointDetector pointDetector;

    @Getter
    private static HashMap<UUID, ButtonTextDisplay> hoveredDisplays = new HashMap<>();

    /**
     * Crée un nouveau ImprovedHoverableTextDisplay.
     */
    public ButtonTextDisplay(UUID id, TextDisplay textDisplay, Player targetPlayer, Location location,
                             String originalText, String hoveredText, Color originalBackgroundColor,
                             Color hoveredBackgroundColor, float originalScale, float hoverScale,
                             double detectionRange, float displayWidth, float displayHeight) {
        this.id = id;
        this.textDisplay = textDisplay;
        this.targetPlayer = targetPlayer;
        this.location = location;
        this.originalText = originalText;
        this.hoveredText = hoveredText;
        this.originalBackgroundColor = originalBackgroundColor;
        this.hoveredBackgroundColor = hoveredBackgroundColor;
        this.originalScale = originalScale;
        this.hoverScale = hoverScale;
        this.detectionRange = detectionRange;
        this.displayWidth = displayWidth;
        this.displayHeight = displayHeight;
        
        // Initialiser le détecteur de points
        initializePointDetector();
    }

    private void initializePointDetector() {
        List<Player> players = new ArrayList<>();
        if (targetPlayer != null) {
            players.add(targetPlayer);
        } else {
            players.addAll(getServer().getOnlinePlayers());
        }
        
        this.pointDetector = new PlanePointDetector(players, location.toVector());
    }

    /**
     * Affiche les particules autour du TextDisplay pour visualiser la zone de détection.
     */
    public void showDetectionZone() {
        if (textDisplay.isDead()) return;

        Location center = location.clone();

        // Récupérer la transformation actuelle pour calculer la taille réelle
        Matrix4f transform = getTextDisplayTransform();
        
        // Extraire les valeurs d'échelle de la matrice
        float scaleX = transform.m00();
        float scaleY = transform.m11();
        
        // Calculer les dimensions réelles en tenant compte de l'échelle
        float halfWidth = displayWidth / 2f;
        float halfHeight = displayHeight / 2f;
        
        // Créer les coins du rectangle de détection avec les dimensions réelles
        Location[] corners = new Location[4];
        corners[0] = center.clone().add(-halfWidth, -halfHeight, 0); // Coin en bas à gauche
        corners[1] = center.clone().add(halfWidth, -halfHeight, 0);  // Coin en bas à droite
        corners[2] = center.clone().add(halfWidth, halfHeight, 0);    // Coin en haut à droite
        corners[3] = center.clone().add(-halfWidth, halfHeight, 0);   // Coin en haut à gauche

        // Afficher les particules sur les coins et les bords
        for (Location corner : corners) {
            corner.getWorld().spawnParticle(
                Particle.DUST,
                corner,
                1,
                0, 0, 0,
                new Particle.DustOptions(Color.GREEN, 1.0f)
            );
        }

        // Afficher les particules sur les bords
        int particlesPerSide = 10;
        for (int i = 0; i < particlesPerSide; i++) {
            float t = i / (float) (particlesPerSide - 1);
            
            // Bords horizontaux
            Location bottom = center.clone()
                .add(-halfWidth + t * displayWidth, -halfHeight, 0);
            Location top = center.clone()
                .add(-halfWidth + t * displayWidth, halfHeight, 0);
            
            // Bords verticaux
            Location left = center.clone()
                .add(-halfWidth, -halfHeight + t * displayHeight, 0);
            Location right = center.clone()
                .add(halfWidth, -halfHeight + t * displayHeight, 0);

            bottom.getWorld().spawnParticle(
                Particle.DUST,
                bottom,
                1,
                0, 0, 0,
                new Particle.DustOptions(Color.GREEN, 0.5f)
            );
            top.getWorld().spawnParticle(
                Particle.DUST,
                top,
                1,
                0, 0, 0,
                new Particle.DustOptions(Color.GREEN, 0.5f)
            );
            left.getWorld().spawnParticle(
                Particle.DUST,
                left,
                1,
                0, 0, 0,
                new Particle.DustOptions(Color.GREEN, 0.5f)
            );
            right.getWorld().spawnParticle(
                Particle.DUST,
                right,
                1,
                0, 0, 0,
                new Particle.DustOptions(Color.GREEN, 0.5f)
            );
        }
    }

    /**
     * Met à jour l'état de hover pour ce display.
     */
    public void updateHoverState() {
        if (textDisplay.isDead()) return;
        
        // Afficher la zone de détection
        //showDetectionZone();
        
        // Récupérer la transformation actuelle du TextDisplay
        Matrix4f transform = getTextDisplayTransform();
        
        // Détecter les interactions avec le plan
        List<PlanePointDetector.ClickResult> results = pointDetector.detectClick(transform);
        
        boolean newHoverState = false;
        Player hoveringPlayer = null;
        
        for (PlanePointDetector.ClickResult result : results) {
            if (result.isInBounds() && result.getDistance() <= detectionRange) {
                newHoverState = true;
                hoveringPlayer = result.getPlayer();
                
                // Gérer les clics si nécessaire
                if (result.isClicked()) {
                    onTextDisplayClicked(result.getPlayer(), result.getIntersection());
                }
                
                break; // Prendre le premier joueur qui hover
            }
        }

        // Mettre à jour l'état de hover
        if (newHoverState != isHovered) {
            isHovered = newHoverState;
            updateAppearance();

            if (isHovered && hoveringPlayer != null) {
                hoveredDisplays.put(hoveringPlayer.getUniqueId(), this);
                hoveringPlayer.playSound(hoveringPlayer.getLocation(), Sound.BLOCK_NOTE_BLOCK_HARP, 0.5f, 1.5f);
            } else if (!isHovered && hoveringPlayer != null) {
                hoveredDisplays.remove(hoveringPlayer.getUniqueId());
            }
        }
    }
    
    /**
     * Obtient la matrice de transformation du TextDisplay.
     */
    private Matrix4f getTextDisplayTransform() {
        Transformation transformation = textDisplay.getTransformation();
        
        // Créer la matrice de transformation
        Matrix4f transform = new Matrix4f();
        
        // Appliquer la rotation
        Quaternionf rotation = new Quaternionf(
            transformation.getLeftRotation().x(),
            transformation.getLeftRotation().y(),
            transformation.getLeftRotation().z(),
            transformation.getLeftRotation().w()
        );
        transform.rotate(rotation);
        
        // Appliquer la translation (position du TextDisplay)
        Vector3f translation = new Vector3f(
            transformation.getTranslation().x(),
            transformation.getTranslation().y(),
            transformation.getTranslation().z()
        );
        transform.translate(translation);
        
        // Appliquer l'échelle
        Vector3f scale = new Vector3f(
            transformation.getScale().x() * displayWidth,
            transformation.getScale().y() * displayHeight,
            transformation.getScale().z()
        );
        transform.scale(scale);
        
        // Centrer le plan (décalage de -0.5 pour que le centre soit à 0,0)
        transform.translate(-0.5f, -0.5f, 0f);
        
        return transform;
    }
    
    /**
     * Appelé quand le TextDisplay est cliqué.
     */
    protected void onTextDisplayClicked(Player player, Vector3f intersection) {
        // Méthode à override dans les classes enfant
        player.sendMessage("§aTextDisplay cliqué à la position: §f" + intersection.toString());
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

        float currentScale = isHovered ? hoverScale : originalScale;

        Transformation transformation = textDisplay.getTransformation();
        transformation.getScale().set(currentScale, currentScale, currentScale);
        textDisplay.setTransformation(transformation);
    }
    
    /**
     * Met à jour la liste des joueurs pour la détection.
     */
    public void updatePlayerList() {
        initializePointDetector();
    }

    /**
     * Détruit ce display hoverable.
     */
    public void destroy() {
        hoveredDisplays.entrySet().removeIf(entry -> entry.getValue() == this);
        if (textDisplay != null && !textDisplay.isDead()) {
            textDisplay.remove();
        }
    }
}