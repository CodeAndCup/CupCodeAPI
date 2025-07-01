package fr.perrier.cupcodeapi.textdisplay;

import fr.perrier.cupcodeapi.CupCodeAPI;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.metadata.FixedMetadataValue;
import org.joml.Vector3f;

/**
 * Bouton d'interaction pour TextDisplay
 */
@Getter
public class InteractionButton {
    private final String id;
    private final Vector3f offset;
    private final Vector3f size;
    private final Interaction interaction;
    private final TextDisplay parentDisplay;
    private final Player targetPlayer;

    public InteractionButton(String id, Vector3f offset, Vector3f size,
                             TextDisplay parentDisplay, World world, Player targetPlayer) {
        this.id = id;
        this.offset = offset;
        this.size = size;
        this.parentDisplay = parentDisplay;
        this.targetPlayer = targetPlayer;

        Location buttonLocation = parentDisplay.getLocation().clone().add(offset.x, offset.y, offset.z);

        this.interaction = (Interaction) world.spawnEntity(buttonLocation, EntityType.INTERACTION);
        this.interaction.setInteractionWidth(size.x);
        this.interaction.setInteractionHeight(size.y);
        this.interaction.setPersistent(true);

        // Métadonnées pour l'identification
        this.interaction.setMetadata("BUTTON_ID", new FixedMetadataValue(CupCodeAPI.getPlugin(), id));
        this.interaction.setMetadata("TARGET_PLAYER", new FixedMetadataValue(
                CupCodeAPI.getPlugin(),
                targetPlayer != null ? targetPlayer.getUniqueId().toString() : "ALL"
        ));
    }

    public void remove() {
        if (interaction != null && !interaction.isDead()) {
            interaction.remove();
        }
    }

    public boolean isTargetedFor(Player player) {
        if (targetPlayer == null) return true;
        return targetPlayer.equals(player);
    }
}