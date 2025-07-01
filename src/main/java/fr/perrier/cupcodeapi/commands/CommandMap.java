package fr.perrier.cupcodeapi.commands;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.command.SimpleCommandMap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class CommandMap extends SimpleCommandMap {

    static Map<UUID, String[]> parameters = new HashMap<>();

    public CommandMap(Server server) {
        super(server);
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String cmdLine) {
        Bukkit.broadcastMessage(sender.getName() + " " + cmdLine);
        return super.tabComplete(sender,cmdLine);
    }

    @Override
    public boolean dispatch(CommandSender sender, String commandLine) {
        return super.dispatch(sender, commandLine);
    }
}
