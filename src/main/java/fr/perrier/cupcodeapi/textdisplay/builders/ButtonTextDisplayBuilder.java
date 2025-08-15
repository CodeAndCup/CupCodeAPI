package fr.perrier.cupcodeapi.textdisplay.builders;

import fr.perrier.cupcodeapi.textdisplay.TextDisplayInstance;
import fr.perrier.cupcodeapi.textdisplay.hover.ButtonTextDisplay;
import fr.perrier.cupcodeapi.utils.ChatUtil;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.entity.Display;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;

import java.util.UUID;

public class ButtonTextDisplayBuilder extends TextDisplayBuilder {
    private String hoveredText;
    private Color hoveredColor;
    private float hoverScale = 1.3f;
    private double detectionRange = 5.0;
    private float displayWidth;
    private float displayHeight;

    /**
     * Create a builder for a hoverable TextDisplay at the given location.
     *
     * @param location The location.
     */
    public ButtonTextDisplayBuilder(Location location) {
        super(location);
    }

    /**
     * Create a builder for a hoverable TextDisplay at the given location for a specific player.
     *
     * @param location The location.
     * @param targetPlayer The target player.
     */
    public ButtonTextDisplayBuilder(Location location, Player targetPlayer) {
        super(location, targetPlayer);
    }

    /**
     * Set the text to display when hovered.
     *
     * @param text The hovered text lines.
     * @return This builder.
     */
    public ButtonTextDisplayBuilder setHoveredText(String... text) {
        this.hoveredText = ChatUtil.translate(String.join("\n", text));
        return this;
    }

    /**
     * Set the text to display when hovered.
     *
     * @param hoveredText The hovered text.
     * @return This builder.
     */
    public ButtonTextDisplayBuilder setHoveredText(String hoveredText) {
        this.hoveredText = ChatUtil.translate(hoveredText);
        return this;
    }

    /**
     * Set the background color when hovered.
     *
     * @param hoveredColor The hovered background color.
     * @return This builder.
     */
    public ButtonTextDisplayBuilder setHoveredBackgroundColor(Color hoveredColor) {
        this.hoveredColor = hoveredColor;
        return this;
    }

    /**
     * Set the scale when hovered.
     *
     * @param hoverScale The hover scale.
     * @return This builder.
     */
    public ButtonTextDisplayBuilder setHoverScale(float hoverScale) {
        this.hoverScale = hoverScale;
        return this;
    }

    /**
     * Set the detection range for hover.
     *
     * @param detectionRange The detection range.
     * @return This builder.
     */
    public ButtonTextDisplayBuilder setDetectionRange(double detectionRange) {
        this.detectionRange = detectionRange;
        return this;
    }

    /**
     * Set the width of the TextDisplay.
     *
     * @param displayWidth The width
     * @return This builder
     */
    public ButtonTextDisplayBuilder setDisplayWidth(float displayWidth) {
        this.displayWidth = displayWidth;
        return this;
    }

    /**
     * Set the height of the TextDisplay.
     *
     * @param displayHeight The height
     * @return This builder
     */
    public ButtonTextDisplayBuilder setDisplayHeight(float displayHeight) {
        this.displayHeight = displayHeight;
        return this;
    }

    // Override des méthodes pour maintenir le type de retour
    @Override
    public ButtonTextDisplayBuilder setText(String... text) {
        super.setText(text);
        return this;
    }

    @Override
    public ButtonTextDisplayBuilder setText(String text) {
        super.setText(text);
        return this;
    }

    @Override
    public ButtonTextDisplayBuilder setBillboard(Display.Billboard billboard) {
        super.setBillboard(billboard);
        return this;
    }

    @Override
    public ButtonTextDisplayBuilder setScale(float scale) {
        super.setScale(scale);
        return this;
    }

    @Override
    public ButtonTextDisplayBuilder setAlignment(TextDisplay.TextAlignment alignment) {
        super.setAlignment(alignment);
        return this;
    }

    @Override
    public ButtonTextDisplayBuilder setShadowed(boolean shadowed) {
        super.setShadowed(shadowed);
        return this;
    }

    @Override
    public ButtonTextDisplayBuilder setSeeThrough(boolean seeThrough) {
        super.setSeeThrough(seeThrough);
        return this;
    }

    @Override
    public ButtonTextDisplayBuilder setViewRange(double viewRange) {
        super.setViewRange(viewRange);
        return this;
    }

    @Override
    public ButtonTextDisplayBuilder setVisible(boolean visible) {
        super.setVisible(visible);
        return this;
    }

    @Override
    public ButtonTextDisplayBuilder setBrightness(int brightness) {
        super.setBrightness(brightness);
        return this;
    }

    @Override
    public ButtonTextDisplayBuilder setRotation(float yaw, float pitch) {
        super.setRotation(yaw, pitch);
        return this;
    }

    @Override
    public ButtonTextDisplayBuilder setBackgroundColor(Color backgroundColor) {
        super.setBackgroundColor(backgroundColor);
        return this;
    }

    /**
     * Build and return the HoverableTextDisplay.
     *
     * @return The created HoverableTextDisplay.
     */
    public ButtonTextDisplay buildHoverable() {
        TextDisplay display = createTextDisplay();

        return new ButtonTextDisplay(
                UUID.randomUUID(),
                display,
                targetPlayer,
                location,
                text,
                hoveredText,
                backgroundColor,
                hoveredColor,
                scale,
                hoverScale,
                detectionRange,
                displayWidth,
                displayHeight
        );
    }

    /**
     * Build and return the TextDisplayInstance as a hoverable instance.
     *
     * @return The created TextDisplayInstance.
     */
    public TextDisplayInstance buildAsInstance() {
        // Utiliser la méthode de la classe parent mais avec le comportement hoverable
        return super.build();
    }
}
