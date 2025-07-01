package fr.perrier.cupcodeapi.utils;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class EzCalc {

    public static Location getLocationInFrontOfPlayer(Player player, double distance) {
        Location loc = player.getLocation();
        Vector direction = loc.getDirection().normalize();

        double x = loc.getX() + direction.getX() * distance;
        double y = loc.getY() + direction.getY() * distance;
        double z = loc.getZ() + direction.getZ() * distance;

        return new Location(loc.getWorld(), x, y, z, loc.getYaw(), loc.getPitch());
    }

    public static Location getLocationInFrontOfPlayer_YFlat(Player player, double distance) {
        Location loc = player.getLocation();
        Vector direction = loc.getDirection().normalize();

        double x = loc.getX() + direction.getX() * distance;
        double y = loc.getY();
        double z = loc.getZ() + direction.getZ() * distance;

        return new Location(loc.getWorld(), x, y, z, loc.getYaw(), loc.getPitch());
    }
}
