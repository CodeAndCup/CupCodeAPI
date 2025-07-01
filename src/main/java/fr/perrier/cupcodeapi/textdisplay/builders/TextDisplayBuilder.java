package fr.perrier.cupcodeapi.textdisplay.builders;

import fr.perrier.cupcodeapi.textdisplay.InteractionButton;
import fr.perrier.cupcodeapi.textdisplay.TextDisplayInstance;
import fr.perrier.cupcodeapi.textdisplay.TextDisplayManager;
import fr.perrier.cupcodeapi.textdisplay.hover.HoverBehavior;
import fr.perrier.cupcodeapi.textdisplay.hover.HoverableTextDisplay;
import fr.perrier.cupcodeapi.utils.ChatUtil;
import lombok.Getter;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Display;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import java.util.*;

/**
 * Builder to create TextDisplays with a fluent API.
 */
@Getter
public class TextDisplayBuilder {
    protected final World world;
    protected final Location location;
    protected final Player targetPlayer;

    // Propriétés du TextDisplay
    protected String text = "";
    protected float scale = 1.0f;
    protected TextDisplay.TextAlignment alignment = TextDisplay.TextAlignment.CENTER;
    protected boolean shadowed = false;
    protected boolean seeThrough = false;
    protected double viewRange = 32.0;
    protected boolean visible = true;
    protected int brightness = -1;
    protected Color backgroundColor;
    protected Display.Billboard billboard = Display.Billboard.FIXED;
    protected Vector rotation = new Vector(0, 0, 0);
    protected int expirationTime = -1;

    // Boutons
    private final Map<String, ButtonConfiguration> buttonConfigs = new HashMap<>();
    private final List<HoverButtonConfiguration> hoverButtonConfigs = new ArrayList<>();

    /**
     * Create a builder for a TextDisplay at the given location.
     *
     * @param location The location.
     */
    public TextDisplayBuilder(Location location) {
        this(location, null);
    }

    /**
     * Create a builder for a TextDisplay at the given location for a specific player.
     *
     * @param location The location.
     * @param targetPlayer The target player.
     */
    public TextDisplayBuilder(Location location, Player targetPlayer) {
        this.location = location.clone();
        this.world = location.getWorld();
        this.targetPlayer = targetPlayer;
    }

    // Méthodes de configuration fluides
    /**
     * Set the text for the display.
     *
     * @param text The text lines.
     * @return This builder.
     */
    public TextDisplayBuilder setText(String... text) {
        this.text = ChatUtil.translate(String.join("\n", text));
        return this;
    }

    /**
     * Set the text for the display.
     *
     * @param text The text.
     * @return This builder.
     */
    public TextDisplayBuilder setText(String text) {
        this.text = ChatUtil.translate(text);
        return this;
    }

    /**
     * Set the billboard mode.
     *
     * @param billboard The billboard mode.
     * @return This builder.
     */
    public TextDisplayBuilder setBillboard(Display.Billboard billboard) {
        this.billboard = billboard;
        return this;
    }

    /**
     * Set the scale of the display.
     *
     * @param scale The scale.
     * @return This builder.
     */
    public TextDisplayBuilder setScale(float scale) {
        this.scale = scale;
        return this;
    }

    /**
     * Set the text alignment.
     *
     * @param alignment The alignment.
     * @return This builder.
     */
    public TextDisplayBuilder setAlignment(TextDisplay.TextAlignment alignment) {
        this.alignment = alignment;
        return this;
    }

    /**
     * Set whether the display is shadowed.
     *
     * @param shadowed true if shadowed.
     * @return This builder.
     */
    public TextDisplayBuilder setShadowed(boolean shadowed) {
        this.shadowed = shadowed;
        return this;
    }

    /**
     * Set whether the display is see-through.
     *
     * @param seeThrough true if see-through.
     * @return This builder.
     */
    public TextDisplayBuilder setSeeThrough(boolean seeThrough) {
        this.seeThrough = seeThrough;
        return this;
    }

    /**
     * Set the view range.
     *
     * @param viewRange The view range.
     * @return This builder.
     */
    public TextDisplayBuilder setViewRange(double viewRange) {
        this.viewRange = viewRange;
        return this;
    }

    /**
     * Set whether the display is visible.
     *
     * @param visible true if visible.
     * @return This builder.
     */
    public TextDisplayBuilder setVisible(boolean visible) {
        this.visible = visible;
        return this;
    }

    /**
     * Set the brightness.
     *
     * @param brightness The brightness.
     * @return This builder.
     */
    public TextDisplayBuilder setBrightness(int brightness) {
        this.brightness = brightness;
        return this;
    }

    /**
     * Set the rotation.
     *
     * @param yaw The yaw.
     * @param pitch The pitch.
     * @return This builder.
     */
    public TextDisplayBuilder setRotation(float yaw, float pitch) {
        this.rotation = new Vector(yaw, pitch, 0.0F);
        return this;
    }

    /**
     * Set the background color.
     *
     * @param backgroundColor The background color.
     * @return This builder.
     */
    public TextDisplayBuilder setBackgroundColor(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
        return this;
    }

    /**
     * Set the expiration time in seconds.
     *
     * @param seconds The expiration time.
     * @return This builder.
     */
    public TextDisplayBuilder setExpirationTime(int seconds) {
        this.expirationTime = seconds;
        return this;
    }

    // Méthodes pour les boutons
    /**
     * Add an interaction button.
     *
     * @param id The button ID.
     * @param x The X position.
     * @param y The Y position.
     * @param width The width.
     * @param height The height.
     * @return This builder.
     */
    public TextDisplayBuilder addButton(String id, float x, float y, float width, float height) {
        buttonConfigs.put(id, new ButtonConfiguration(x, y, width, height));
        return this;
    }

    /**
     * Add a hoverable button.
     *
     * @param id The button ID.
     * @param text The button text.
     * @param hoverText The text when hovered.
     * @param x The X position.
     * @param y The Y position.
     * @param width The width.
     * @param height The height.
     * @return This builder.
     */
    public TextDisplayBuilder addHoverButton(String id, String text, String hoverText,
                                             float x, float y, float width, float height) {
        return addHoverButton(id, text, hoverText, null, 1.0f, 1.3f, x, y, width, height);
    }

    public TextDisplayBuilder addHoverButton(String id, String text, String hoverText,
                                             Color hoverBackgroundColor, float x, float y,
                                             float width, float height) {
        return addHoverButton(id, text, hoverText, hoverBackgroundColor, 1.0f, 1.3f, x, y, width, height);
    }

    public TextDisplayBuilder addHoverButton(String id, String text, String hoverText,
                                             Color hoverBackgroundColor, float scale,
                                             float x, float y, float width, float height) {
        return addHoverButton(id, text, hoverText, hoverBackgroundColor, scale, 1.3f, x, y, width, height);
    }

    public TextDisplayBuilder addHoverButton(String id, String text, String hoverText,
                                             Color hoverBackgroundColor, float scale, float hoverScale,
                                             float x, float y, float width, float height) {
        buttonConfigs.put(id, new ButtonConfiguration(x,y,width,height));
        hoverButtonConfigs.add(new HoverButtonConfiguration(
                id, text, hoverText, hoverBackgroundColor, scale, hoverScale, x, y, width, height
        ));
        return this;
    }

    /**
     * Build and return the TextDisplayInstance.
     *
     * @return The created TextDisplayInstance.
     */
    public TextDisplayInstance build() {
        // Créer le TextDisplay principal
        TextDisplay display = createTextDisplay();

        // Créer le comportement de survol si nécessaire
        HoverBehavior hoverBehavior = null;
        if (this instanceof HoverableTextDisplayBuilder) {
            HoverableTextDisplayBuilder hoverBuilder = (HoverableTextDisplayBuilder) this;
            hoverBehavior = new HoverBehavior(
                    display,
                    targetPlayer,
                    text,
                    hoverBuilder.getHoveredText(),
                    backgroundColor,
                    hoverBuilder.getHoveredColor(),
                    scale,
                    hoverBuilder.getHoverScale(),
                    hoverBuilder.getDetectionRange()
            );
        }

        // Créer l'instance
        UUID instanceId = UUID.randomUUID();
        TextDisplayInstance instance = new TextDisplayInstance(
                instanceId, display, targetPlayer, location, expirationTime, hoverBehavior
        );

        // Créer les boutons d'interaction
        createInteractionButtons(instance, display);

        // Créer les boutons survolables
        createHoverableButtons(instance);

        // Enregistrer dans le manager
        TextDisplayManager.getInstance().registerDisplay(instance);

        return instance;
    }

    /**
     * Create and configure the main TextDisplay entity.
     *
     * @return The created TextDisplay.
     */
    protected TextDisplay createTextDisplay() {
        TextDisplay display = (TextDisplay) world.spawnEntity(location, EntityType.TEXT_DISPLAY);

        // Configuration de base
        display.setText(text);
        display.setAlignment(alignment);
        display.setShadowed(shadowed);
        display.setSeeThrough(seeThrough);
        display.setViewRange((float) viewRange);
        display.setRotation((float) rotation.getX(), (float) rotation.getY());
        display.setVisibleByDefault(visible);
        display.setBillboard(billboard);

        // Configuration avancée
        if (backgroundColor != null) {
            display.setBackgroundColor(backgroundColor);
        }

        display.setTransformation(new Transformation(
                new Vector3f(0, 0, 0),
                new AxisAngle4f(0, 0, 0, 0),
                new Vector3f(scale, scale, scale),
                new AxisAngle4f(0, 0, 0, 0)
        ));

        if (brightness >= 0) {
            display.setBrightness(new Display.Brightness(brightness, brightness));
        }

        return display;
    }

    /**
     * Create and add interaction buttons to the given TextDisplayInstance.
     *
     * @param instance The TextDisplayInstance to add buttons to.
     * @param parentDisplay The parent TextDisplay entity.
     */
    private void createInteractionButtons(TextDisplayInstance instance, TextDisplay parentDisplay) {
        buttonConfigs.forEach((id, config) -> {
            double yawRad = Math.toRadians(rotation.getX());

            double relativeX = config.x * Math.cos(yawRad) - 0.05 * Math.sin(yawRad);
            double relativeZ = config.x * Math.sin(yawRad) + 0.05 * Math.cos(yawRad);

            InteractionButton button = new InteractionButton(
                    id,
                    new Vector3f((float) relativeX, config.y, (float) relativeZ),
                    new Vector3f(config.width, config.height, 0.1f),
                    parentDisplay,
                    world,
                    targetPlayer
            );
            instance.addButton(id, button);
        });
    }

    /**
     * Create and add hoverable buttons to the given TextDisplayInstance.
     *
     * @param instance The TextDisplayInstance to add hoverable buttons to.
     */
    private void createHoverableButtons(TextDisplayInstance instance) {


        hoverButtonConfigs.forEach(config -> {

            double yawRad = Math.toRadians(rotation.getX());

            double relativeX = config.x * Math.cos(yawRad) - 0.05 * Math.sin(yawRad);
            double relativeZ = config.x * Math.sin(yawRad) + 0.05 * Math.cos(yawRad);

            Location buttonLocation = location.clone().add(relativeX, config.y, relativeZ);

            HoverableTextDisplay hoverButton = new HoverableTextDisplayBuilder(buttonLocation, targetPlayer)
                    .setText(config.text)
                    .setHoveredText(config.hoverText)
                    .setScale(config.scale)
                    .setHoverScale(config.hoverScale)
                    .setBackgroundColor(backgroundColor)
                    .setRotation((float) rotation.getX(), (float) rotation.getY())
                    .setHoveredBackgroundColor(config.hoverBackgroundColor)
                    .buildHoverable();

            instance.addHoverableButton(hoverButton);
        });
    }

    // Classes de configuration interne
    /**
     * Configuration for a standard interaction button.
     */
    @Getter
    protected static class ButtonConfiguration {
        final float x, y, width, height;

        /**
         * Construct a ButtonConfiguration.
         *
         * @param x The X position.
         * @param y The Y position.
         * @param width The width.
         * @param height The height.
         */
        ButtonConfiguration(float x, float y, float width, float height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }
    }

    /**
     * Configuration for a hoverable button.
     */
    @Getter
    protected static class HoverButtonConfiguration {
        final String id, text, hoverText;
        final Color hoverBackgroundColor;
        final float scale, hoverScale, x, y, width, height;

        /**
         * Construct a HoverButtonConfiguration.
         *
         * @param id The button ID.
         * @param text The button text.
         * @param hoverText The text when hovered.
         * @param hoverBackgroundColor The background color when hovered.
         * @param scale The normal scale.
         * @param hoverScale The scale when hovered.
         * @param x The X position.
         * @param y The Y position.
         * @param width The width.
         * @param height The height.
         */
        HoverButtonConfiguration(String id, String text, String hoverText,
                                 Color hoverBackgroundColor, float scale, float hoverScale,
                                 float x, float y, float width, float height) {
            this.id = id;
            this.text = text;
            this.hoverText = hoverText;
            this.hoverBackgroundColor = hoverBackgroundColor;
            this.scale = scale;
            this.hoverScale = hoverScale;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }
    }
}

