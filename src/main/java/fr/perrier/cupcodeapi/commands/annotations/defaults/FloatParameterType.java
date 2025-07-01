package fr.perrier.cupcodeapi.commands.annotations.defaults;

import fr.perrier.cupcodeapi.commands.annotations.*;
import fr.perrier.cupcodeapi.utils.*;
import fr.perrier.cupcodeapi.utils.ChatUtil;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class FloatParameterType implements ParameterType<Float> {

    public Float transform(CommandSender sender, String source) {
        if (source.toLowerCase().contains("e")) {
            sender.sendMessage(ChatUtil.translate(ChatColor.RED + source + " n'est pas un nombre valide."));
            return (null);
        }

        try {
            float parsed = Float.parseFloat(source);

            if (Float.isNaN(parsed) || !Float.isFinite(parsed)) {
                sender.sendMessage(ChatUtil.translate("&cCe nombre n'est pas valide"));
                return (null);
            }

            return (parsed);
        } catch (NumberFormatException exception) {
            sender.sendMessage(ChatUtil.translate("&cCe nombre n'est pas valide"));
            return (null);
        }
    }

    public List<String> tabComplete(Player sender, Set<String> flags, String source) {
        return (new ArrayList<>());
    }

}