package pear.resourceworld.utils;

import org.bukkit.Location;

public class LocationUtils {
    public static boolean isSamePosition(Location loc1, Location loc2) {
        if (loc1 == null || loc2 == null || loc1.getWorld() != loc2.getWorld()) {
            return false;
        }

        return loc1.getX() == loc2.getX()
            && loc1.getY() == loc2.getY()
            && loc1.getZ() == loc2.getZ();
    }
}
