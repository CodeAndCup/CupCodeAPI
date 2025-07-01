package fr.perrier.cupcodeapi.commands.annotations.defaults;

import fr.perrier.cupcodeapi.commands.annotations.*;
import fr.perrier.cupcodeapi.utils.*;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class IntegerParameterType implements ParameterType<Integer> {

    public Integer transform(CommandSender sender, String source) {
        try {
            return (Integer.parseInt(source));
        } catch (NumberFormatException exception) {
            sender.sendMessage(ChatUtil.translate(ChatColor.RED + source + " n'est pas un nombre valide."));
            return (null);
        }
    }

    public List<String> tabComplete(Player sender, Set<String> flags, String source) {
        return (new ArrayList<>());
    }

}