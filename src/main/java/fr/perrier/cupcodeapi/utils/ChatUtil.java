package fr.perrier.cupcodeapi.utils;

import net.md_5.bungee.api.ChatColor;

public class ChatUtil {

    public static String translate(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
    public static String getBar() {
        return translate("&8&m\u0020\u0020\u0020\u0020\u0020\u0020\u0020\u0020\u0020\u0020\u0020\u0020\u0020\u0020\u0020\u0020\u0020\u0020\u0020\u0020\u0020\u0020\u0020\u0020\u0020\u0020\u0020\u0020\u0020\u0020\u0020\u0020\u0020\u0020\u0020\u0020\u0020\u0020\u0020\u0020\u0020\u0020\u0020\u0020\u0020\u0020\u0020\u0020\u0020\u0020\u0020&r");
    }
}
