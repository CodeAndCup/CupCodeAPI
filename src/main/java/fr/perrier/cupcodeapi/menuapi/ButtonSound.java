package fr.perrier.cupcodeapi.menuapi;

import com.cryptomorin.xseries.XSound;
import org.bukkit.Sound;

public enum ButtonSound {
    CLICK(XSound.UI_BUTTON_CLICK.parseSound()),
    SUCCESS(XSound.ENTITY_VILLAGER_YES.parseSound()),
    FAIL(XSound.BLOCK_GRASS_BREAK.parseSound());

    private final Sound sound;

    ButtonSound(final Sound sound) {
        this.sound = sound;
    }

    public Sound getSound() {
        return this.sound;
    }
}
