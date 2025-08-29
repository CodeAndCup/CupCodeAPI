package fr.perrier.cupcodeapi.utils;

import org.bukkit.Location;

import java.util.Objects;

public class RegionUtils {

    /**
     * Checks if a location is inside a cuboid region defined by two corner locations.
     * The cuboid includes all blocks whose coordinates are between the minimum and maximum
     * x, y, and z coordinates of the two corners.
     *
     * @param pos1 First corner of the cuboid
     * @param pos2 Second corner of the cuboid
     * @param loc The location to check
     *
     * @return true if the location is inside the cuboid, false otherwise
     */
    public static boolean isInside(Location pos1, Location pos2, Location loc) {
        if (loc == null || pos1 == null || pos2 == null) {
            return false;
        }

        if (!Objects.equals(loc.getWorld(), pos1.getWorld()) || !Objects.equals(loc.getWorld(), pos2.getWorld())) {
            return false;
        }

        double minX = Math.min(pos1.getX(), pos2.getX());
        double minY = Math.min(pos1.getY(), pos2.getY());
        double minZ = Math.min(pos1.getZ(), pos2.getZ());

        double maxX = Math.max(pos1.getX(), pos2.getX());
        double maxY = Math.max(pos1.getY(), pos2.getY());
        double maxZ = Math.max(pos1.getZ(), pos2.getZ());

        return loc.getX() >= minX && loc.getX() <= maxX
                && loc.getY() >= minY && loc.getY() <= maxY
                && loc.getZ() >= minZ && loc.getZ() <= maxZ;
    }
}