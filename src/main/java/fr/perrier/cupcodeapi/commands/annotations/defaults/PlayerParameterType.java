package fr.perrier.cupcodeapi.commands.annotations.defaults;

import fr.perrier.cupcodeapi.commands.annotations.*;
import fr.perrier.cupcodeapi.utils.*;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class PlayerParameterType implements ParameterType<Player> {

    public Player transform(CommandSender sender, String source) {
        if (sender instanceof Player && (source.equalsIgnoreCase("self") || source.equals(""))) {
            return ((Player) sender);
        }
        if (!(sender instanceof Player) && (source.equalsIgnoreCase("self") || source.equals(""))) {
            sender.sendMessage(ChatUtil.translate("&cVous êtes fou ?"));
            return (null);
        }

        Player player = Bukkit.getPlayer(source);

        if (player == null) {
            sender.sendMessage(ChatUtil.translate("&cCe joueur n'est pas connecté"));
            return (null);
        }

        return (player);
    }

    public List<String> tabComplete(Player sender, Set<String> flags, String source) {
        List<String> completions = new ArrayList<>();

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (StringUtils.startsWithIgnoreCase(player.getName(), source) && (sender.canSee(player))) {
                completions.add(player.getName());
            }
        }

        return (completions);
    }

}