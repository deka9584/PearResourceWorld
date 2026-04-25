package pear.resourceworld.utils;

import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
public class LocationUtils {
    public static final boolean PAPER_API;

    static {
        boolean isPaper;
        try {
            Class.forName("io.papermc.paper.util.Tick");
            isPaper = true;
        } catch (ClassNotFoundException ex) {
            isPaper = false;
        }
        PAPER_API = isPaper;
    }

    public static CompletableFuture<?> getChuckAtAsync(World w, Location loc) {
        try {
            Method getChuckAsyncMethod = w.getClass().getMethod("getChunkAtAsync", Location.class);
            return (CompletableFuture<?>) getChuckAsyncMethod.invoke(w, loc);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

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

    public static int getRandomInt(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }
}
