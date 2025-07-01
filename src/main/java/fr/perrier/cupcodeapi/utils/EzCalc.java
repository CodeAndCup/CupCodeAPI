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


    public static Location getLocationBetween(Location loc1, Location loc2) {
        double x = (loc1.getX() + loc2.getX()) / 2;
        double y = (loc1.getY() + loc2.getY()) / 2;
        double z = (loc1.getZ() + loc2.getZ()) / 2;

        return new Location(loc1.getWorld(), x, y, z);
    }
}
