package pear.resourceworld.utils;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
public class LocationUtils {
    private static final String[] UNSAFE_TELEPORT_BLOCKS = {
        "CACTUS",
        "MAGMA_BLOCK",
        "COBWEBS",
        "SWEET_BERRY_BUSHES",
        "BUBBLE_COLUMNS",
        "POWDER_SNOW",
        "TALL_SEAGRASS",
        "FIRE"
    };

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

        return isSafeTpMaterial(belowBlock.getType()) && !belowBlock.isLiquid();
    }

    public static boolean isSafeTpMaterial(Material material) {
        String matName = material.name();

        for (int i = 0; i < UNSAFE_TELEPORT_BLOCKS.length; i++) {
            if (UNSAFE_TELEPORT_BLOCKS[i].equals(matName)) {
                return false;
            }
        }

        return true;
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
