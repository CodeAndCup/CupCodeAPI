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

    /**
     * Calcule l'angle en degrés entre deux vecteurs J1 et J2 par rapport au point pivot (centre).
     * L'angle est mesuré dans le plan X-Z (horizontal), comme un yaw.
     *
     * Cette méthode est valide : elle utilise le produit scalaire pour l'angle et le produit vectoriel pour le signe,
     * ce qui correspond à la convention du yaw dans Minecraft (plan XZ).
     */
    public static float getYawAngleBetween(Location pivot, Location j1, Location j2) {
        double dx1 = j1.getX() - pivot.getX();
        double dz1 = j1.getZ() - pivot.getZ();

        double dx2 = j2.getX() - pivot.getX();
        double dz2 = j2.getZ() - pivot.getZ();

        double dot = dx1 * dx2 + dz1 * dz2;
        double mag1 = Math.sqrt(dx1 * dx1 + dz1 * dz1);
        double mag2 = Math.sqrt(dx2 * dx2 + dz2 * dz2);

        if (mag1 == 0 || mag2 == 0) return 0;

        double cos = dot / (mag1 * mag2);
        cos = Math.max(-1.0, Math.min(1.0, cos));

        double angleRad = Math.acos(cos);
        double angleDeg = Math.toDegrees(angleRad);

        double cross = dx1 * dz2 - dz1 * dx2;
        if (cross < 0) angleDeg = -angleDeg;

        return (float) angleDeg;
    }
}
