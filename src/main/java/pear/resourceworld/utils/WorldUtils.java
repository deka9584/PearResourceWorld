package pear.resourceworld.utils;

import java.lang.reflect.Method;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Difficulty;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.block.BlockState;
import org.bukkit.World.Environment;
import org.bukkit.boss.DragonBattle;
import org.bukkit.entity.EnderDragon;

public class WorldUtils {
    public static boolean generateEndExitPortal(World endWorld) {
        DragonBattle dragonBattle = endWorld.getEnderDragonBattle();

        if (dragonBattle == null) {
            return false;
        }

        try {
            Method generateEndPortalMethod = dragonBattle.getClass().getMethod("generateEndPortal", boolean.class);
            generateEndPortalMethod.invoke(dragonBattle, true);
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
        DragonBattle dragonBattle = endWorld.getEnderDragonBattle();
        
        if (dragonBattle == null) {
            return false;
        }

        EnderDragon enderDragon = dragonBattle.getEnderDragon();

        if (enderDragon == null) {
            return false;
        }

        enderDragon.remove();
        return true;
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
}
