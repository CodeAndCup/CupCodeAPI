package fr.perrier.cupcodeapi.utils;

import net.md_5.bungee.api.ChatColor;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.bukkit.ChatColor.COLOR_CHAR;

public class ChatUtil {

    /**
     * Translates color codes in the format & + code to Minecraft color codes.
     * Also processes hex color codes and gradient tags.
     * @param message The message to translate
     * @return The translated message
     */
    public static String translate(String message) {
        return ChatColor.translateAlternateColorCodes('&', translateHexColorCodes("&#","",ChatUtil.processGradientTags(message)));
    }

    /**
     * Translates hex color codes in the format of startTag + RRGGBB + endTag to Minecraft color codes.
     * For example, with startTag = "&#" and endTag = "", the input "This is a &#FF5733red text" becomes
     * "This is a §x§F§F§5§7§3§3red text".
     *
     * @param startTag The starting tag for hex color codes (e.g., "&#").
     * @param endTag   The ending tag for hex color codes (e.g., "").
     * @param message  The message containing hex color codes to translate.
     * @return The message with hex color codes translated to Minecraft format.
     */
    public static String translateHexColorCodes(String startTag, String endTag, String message)
    {
        final Pattern hexPattern = Pattern.compile(startTag + "([A-Fa-f0-9]{6})" + endTag);
        Matcher matcher = hexPattern.matcher(message);
        StringBuffer buffer = new StringBuffer(message.length() + 4 * 8);
        while (matcher.find())
        {
            String group = matcher.group(1);
            matcher.appendReplacement(buffer, COLOR_CHAR + "x"
                    + COLOR_CHAR + group.charAt(0) + COLOR_CHAR + group.charAt(1)
                    + COLOR_CHAR + group.charAt(2) + COLOR_CHAR + group.charAt(3)
                    + COLOR_CHAR + group.charAt(4) + COLOR_CHAR + group.charAt(5)
            );
        }
        return matcher.appendTail(buffer).toString();
    }

    /**
     * Generates a decorative bar string for chat messages.
     * @return A string representing a decorative bar
     */
    public static String getBar() {
        return translate("&8&m\u0020\u0020\u0020\u0020\u0020\u0020\u0020\u0020\u0020\u0020\u0020\u0020\u0020\u0020\u0020\u0020\u0020\u0020\u0020\u0020\u0020\u0020\u0020\u0020\u0020\u0020\u0020\u0020\u0020\u0020\u0020\u0020\u0020\u0020\u0020\u0020\u0020\u0020\u0020\u0020\u0020\u0020\u0020\u0020\u0020\u0020\u0020\u0020\u0020\u0020\u0020&r");
    }

    /**
     * Converts a string to small caps using Unicode characters.
     * Only lowercase a-z are converted; other characters remain unchanged.
     * @param message The input string
     * @return The string converted to small caps
     */
    public static String toSmallCaps(String message) {
        HashMap<Character,String> smallCaps = new HashMap<>(){
            {
                put('a',"ᴀ");
                put('b',"ʙ");
                put('c', "ᴄ");
                put('d', "ᴅ");
                put('e', "ᴇ");
                put('f', "ғ");
                put('g', "ɢ");
                put('h', "ʜ");
                put('i', "ɪ");
                put('j', "ᴊ");
                put('k', "ᴋ");
                put('l', "ʟ");
                put('m', "ᴍ");
                put('n', "ɴ");
                put('o', "ᴏ");
                put('p', "ᴘ");
                put('q', "ǫ");
                put('r', "ʀ");
                put('s', "s");
                put('t', "ᴛ");
                put('u', "ᴜ");
                put('v', "ᴠ");
                put('w', "ᴡ");
                put('x', "x");
                put('y', "ʏ");
                put('z', "ᴢ");
            }
        };

        StringBuilder result = new StringBuilder();
        for (char c : message.toCharArray()) {
            if (smallCaps.containsKey(c)) {
                result.append(smallCaps.get(c));
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }

    /**
     * Converts a hex color code to RGB array
     * @param hexColor Hex color without # (e.g., "FF0000")
     * @return int array [r, g, b]
     */
    private static int[] hexToRGB(String hexColor) {
        // Remove # if present and ensure uppercase
        hexColor = hexColor.replace("#", "").toUpperCase();

        // Ensure it's 6 characters
        if (hexColor.length() != 6) {
            throw new IllegalArgumentException("Invalid hex color: " + hexColor);
        }

        int r = Integer.parseInt(hexColor.substring(0, 2), 16);
        int g = Integer.parseInt(hexColor.substring(2, 4), 16);
        int b = Integer.parseInt(hexColor.substring(4, 6), 16);

        return new int[]{r, g, b};
    }

    /**
     * Converts a single hex color to the Minecraft color format
     * @param hexColor Hex color without # (e.g., "FF0000")
     * @return Minecraft color code string
     */
    private static String translateHexColorCode(String hexColor) {
        hexColor = hexColor.replace("#", "").toUpperCase();
        return COLOR_CHAR + "x"
               + COLOR_CHAR + hexColor.charAt(0) + COLOR_CHAR + hexColor.charAt(1)
               + COLOR_CHAR + hexColor.charAt(2) + COLOR_CHAR + hexColor.charAt(3)
               + COLOR_CHAR + hexColor.charAt(4) + COLOR_CHAR + hexColor.charAt(5);
    }

    /**
     * Processes gradient tags in the format:
     * <gradient:#RRGGBB>text</gradient:#RRGGBB> or
     * <gradient:#RRGGBB:maxColors>text</gradient:#RRGGBB>
     * and applies gradients to the text within those tags
     * @param message The message containing gradient tags
     * @param defaultMaxColors Default maximum number of colors to use when not specified in tag
     * @return The processed message with gradients applied and translated
     */
    public static String processGradientTags(String message, int defaultMaxColors) {
        if (message == null || message.isEmpty()) {
            return message;
        }

        final Pattern gradientPattern = Pattern.compile("<gradient:(#?[A-Fa-f0-9]{6})(?::(\\d+))?>(.*?)</gradient:(#?[A-Fa-f0-9]{6})>");
        Matcher matcher = gradientPattern.matcher(message);
        StringBuffer buffer = new StringBuffer();

        while (matcher.find()) {
            String startColor = matcher.group(1).replace("#", "");
            String maxColorsStr = matcher.group(2); // This will be null if not specified
            String text = matcher.group(3);
            String endColor = matcher.group(4).replace("#", "");

            int maxColors = defaultMaxColors;
            if (maxColorsStr != null) {
                try {
                    maxColors = Integer.parseInt(maxColorsStr);
                } catch (NumberFormatException ignored) {
                }
            }

            String gradientText = gradient(text, startColor, endColor, maxColors);
            matcher.appendReplacement(buffer, Matcher.quoteReplacement(gradientText));
        }

        String result = matcher.appendTail(buffer).toString();
        return translate(result);
    }

    /**
     * Overloaded method with automatic maxColors calculation
     * @param message The message containing gradient tags
     * @return The processed message with gradients applied and translated
     */
    public static String processGradientTags(String message) {
        return processGradientTags(message, -1); // -1 will be handled in the gradient method
    }

    /**
     * Modified gradient method to handle automatic maxColors when -1 is passed
     */
    public static String gradient(String text, String startColor, String endColor, int maxColors) {
        if (text == null || text.isEmpty()) {
            return "";
        }

        String cleanText = text.replaceAll("(?i)&[0-9A-FK-OR]", "").replaceAll("(?i)&#[0-9A-F]{6}", "");

        if (cleanText.length() <= 1) {
            return translateHexColorCode(startColor) + cleanText;
        }

        if (maxColors <= 0) {
            maxColors = Math.min(cleanText.length(), 10);
        }

        maxColors = Math.min(maxColors, cleanText.length());

        int[] startRGB = hexToRGB(startColor);
        int[] endRGB = hexToRGB(endColor);

        StringBuilder gradientText = new StringBuilder();

        for (int i = 0; i < cleanText.length(); i++) {
            char character = cleanText.charAt(i);

            int colorIndex = (i * (maxColors - 1)) / Math.max(1, cleanText.length() - 1);

            double factor = (double) colorIndex / Math.max(1, maxColors - 1);

            int r = (int) (startRGB[0] + (endRGB[0] - startRGB[0]) * factor);
            int g = (int) (startRGB[1] + (endRGB[1] - startRGB[1]) * factor);
            int b = (int) (startRGB[2] + (endRGB[2] - startRGB[2]) * factor);

            String hexColor = String.format("%02X%02X%02X", r, g, b);

            gradientText.append(translateHexColorCode(hexColor)).append(character);
        }

        return gradientText.toString();
    }
}
