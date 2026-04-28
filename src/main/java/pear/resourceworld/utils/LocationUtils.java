package pear.resourceworld.utils;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
public class LocationUtils {

    public static boolean isLocationSafe(Location loc) {
        World world = loc.getWorld();
        Block feetBlock = world.getBlockAt(loc);

        if (!feetBlock.isEmpty()) {
            return false;
        }

        if (!feetBlock.getRelative(BlockFace.UP).isEmpty()) {
            return false;
        }

        Block belowBlock = feetBlock.getRelative(BlockFace.DOWN);

        return belowBlock.getType() != Material.CACTUS && !belowBlock.isLiquid();
    }

    public static boolean isSamePosition(Location loc1, Location loc2) {
        if (loc1 == null || loc2 == null || loc1.getWorld() != loc2.getWorld()) {
            return false;
        }

        return loc1.getX() == loc2.getX()
            && loc1.getY() == loc2.getY()
            && loc1.getZ() == loc2.getZ();
    }
}
