package pear.resourceworld.utils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Difficulty;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.World.Environment;
import org.bukkit.boss.DragonBattle;
import org.bukkit.entity.EnderDragon;

public class WorldUtils {
    public static boolean generateEndExitPortal(World endWorld) {
        DragonBattle battle = endWorld.getEnderDragonBattle();

        if (battle == null) {
            return false;
        }

        try {
            Method generateEndPortalMethod = battle.getClass().getMethod("generateEndPortal", boolean.class);
            generateEndPortalMethod.invoke(battle, true);
            return true;
        } catch (Exception ex) {
            if (!(ex instanceof NoSuchMethodException)) {
                ex.printStackTrace();
            }
        }

        return false;
    }

    public static boolean hasEndExitPortal(World endWorld) {
        for (int x = -8; x <= 8; x++) {
            for (int z = -8; z <= 8; z++) {
                Chunk chunk = endWorld.getChunkAt(x, z);

                for (BlockState state : chunk.getTileEntities()) {
                    if (state.getType() == Material.END_PORTAL) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public static boolean removeEnderDragon(World endWorld) {
        DragonBattle battle = endWorld.getEnderDragonBattle();
        
        if (battle == null) {
            return false;
        }

        EnderDragon enderDragon = battle.getEnderDragon();

        if (enderDragon == null) {
            return false;
        }

        enderDragon.remove();
        return true;
    }

    public static boolean setDragonPreviouslyKilled(World endWorld, boolean flag) {
        DragonBattle battle = endWorld.getEnderDragonBattle();

        if (battle == null) {
            return false;
        }

        try {
            Method setPreviouslyKilledMethod = battle.getClass().getMethod("setPreviouslyKilled", boolean.class);
            setPreviouslyKilledMethod.invoke(battle, flag);
            return true;
        } catch (Exception ex) {
            if (!(ex instanceof NoSuchMethodException)) {
                ex.printStackTrace();
            }
        }

        return false;
    }

    public static World generateWorld(String name, String customSeed, Environment env, WorldType type, boolean generateStructures) {
        WorldCreator wCreator = new WorldCreator(name);
        wCreator.type(type);
        wCreator.environment(env);
        wCreator.generateStructures(generateStructures);

        if (!customSeed.isEmpty()) {
            try {
                wCreator.seed(Long.parseLong(customSeed));
            } catch (NumberFormatException ex) {
                Bukkit.getLogger().log(Level.SEVERE, "Invalid seed format");
                ex.printStackTrace();
            }
        }

        return wCreator.createWorld();
    }

    public static Difficulty getDifficultyByName(String name) {
        for (Difficulty diff : Difficulty.values()) {
            if (diff.name().equalsIgnoreCase(name)) {
                return diff;
            }
        }

        return null;
    }

    public static List<Sign> getAttachedSigns(Block block) {
        List<Sign> signs = new ArrayList<>();

        for (BlockFace face : BlockFace.values()) {
            Block relBlock = block.getRelative(face);
            BlockState relState = relBlock.getState();

            if (relState instanceof Sign) {
                if (face == BlockFace.UP && Tag.STANDING_SIGNS.isTagged(relBlock.getType())) {
                    signs.add((Sign) relState);
                    continue;
                }

                BlockData relData = relBlock.getBlockData();

                if (relData instanceof WallSign && ((WallSign) relData).getFacing() == face) {
                    signs.add((Sign) relState);
                }
            }
        }

        return signs;
    }
}
