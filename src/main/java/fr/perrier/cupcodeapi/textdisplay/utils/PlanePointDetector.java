package fr.perrier.cupcodeapi.textdisplay.utils;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInputEvent;
import org.bukkit.util.Vector;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.List;

/**
 * Détecteur de points sur un plan pour améliorer la précision des interactions.
 * Basé sur le système du SVPicker.
 */
public class PlanePointDetector {
    private final List<Player> players;
    private final Vector position;

    public PlanePointDetector(List<Player> players, Vector position) {
        this.players = players;
        this.position = position;
    }

    /**
     * Détecte les clics sur un plan défini par une matrice de transformation.
     *
     * @param transform La matrice de transformation du plan
     * @return Liste des résultats de détection pour chaque joueur
     */
    public List<ClickResult> detectClick(Matrix4f transform) {
        List<ClickResult> results = new ArrayList<>();

        for (Player player : players) {
            ClickResult result = detectClickForPlayer(player, transform);
            if (result != null) {
                results.add(result);
            }
        }

        return results;
    }

    /**
     * Détecte un clic pour un joueur spécifique sur le plan.
     */
    private ClickResult detectClickForPlayer(Player player, Matrix4f transform) {
        Vector eyeLocation = player.getEyeLocation().toVector();
        Vector direction = player.getEyeLocation().getDirection();

        // Convertir en coordonnées relatives à la position du TextDisplay
        Vector relativeEyePos = eyeLocation.clone().subtract(position);

        // Calculer l'intersection avec le plan
        Vector3f intersection = calculatePlaneIntersection(
                new Vector3f((float) relativeEyePos.getX(), (float) relativeEyePos.getY(), (float) relativeEyePos.getZ()),
                new Vector3f((float) direction.getX(), (float) direction.getY(), (float) direction.getZ()),
                transform
        );

        if (intersection == null) {
            return null;
        }

        // Vérifier si l'intersection est dans les limites du plan (0-1 sur x et y)
        boolean isInBounds = intersection.x >= 0 && intersection.x <= 1 &&
                intersection.y >= 0 && intersection.y <= 1;

        if (!isInBounds) {
            return null;
        }

        // Calculer la distance pour vérifier si c'est dans la portée
        // Calculer la distance entre le joueur et le point d'intersection
        double distance = Math.sqrt(
            Math.pow(relativeEyePos.getX() - intersection.x, 2) +
            Math.pow(relativeEyePos.getY() - intersection.y, 2) +
            Math.pow(relativeEyePos.getZ() - intersection.z, 2)
        );

        // Vérifier si le joueur clique (vous pouvez adapter cette logique selon vos besoins)
        boolean isClicked = player.isBlocking();

        return new ClickResult(player, intersection, distance, isClicked, isInBounds);
    }

    /**
     * Calcule l'intersection entre un rayon et un plan défini par une matrice de transformation.
     */
    private Vector3f calculatePlaneIntersection(Vector3f rayOrigin, Vector3f rayDirection, Matrix4f transform) {
        try {
            // Inverser la matrice de transformation pour convertir du monde vers l'espace local
            Matrix4f invTransform = new Matrix4f(transform).invert();

            // Transformer l'origine et la direction du rayon dans l'espace local
            Vector4f localOrigin = new Vector4f(rayOrigin.x, rayOrigin.y, rayOrigin.z, 1.0f);
            Vector4f localDirection = new Vector4f(rayDirection.x, rayDirection.y, rayDirection.z, 0.0f);

            invTransform.transform(localOrigin);
            invTransform.transform(localDirection);

            // Le plan est à z=0 dans l'espace local
            // Calculer t pour l'intersection avec le plan z=0
            if (Math.abs(localDirection.z) < 1e-6) {
                return null; // Le rayon est parallèle au plan
            }

            float t = -localOrigin.z / localDirection.z;

            if (t < 0) {
                return null; // L'intersection est derrière le joueur
            }

            // Calculer le point d'intersection
            Vector3f intersection = new Vector3f(
                    localOrigin.x + t * localDirection.x,
                    localOrigin.y + t * localDirection.y,
                    0.0f
            );

            // Vérifier que les coordonnées sont dans les limites du plan
            if (intersection.x < 0 || intersection.x > 1 || intersection.y < 0 || intersection.y > 1) {
                return null;
            }

            return intersection;

        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Résultat d'une détection de clic.
     */
    @Getter
    public static class ClickResult {
        private final Player player;
        private final Vector3f intersection;
        private final double distance;
        private final boolean clicked;
        private final boolean inBounds;

        public ClickResult(Player player, Vector3f intersection, double distance, boolean isClicked, boolean isInBounds) {
            this.player = player;
            this.intersection = intersection;
            this.distance = distance;
            this.clicked = isClicked;
            this.inBounds = isInBounds;
        }
    }
}