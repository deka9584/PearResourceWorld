package pear.resourceworld.utils;

import java.lang.reflect.Method;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.World.Environment;
import org.bukkit.boss.DragonBattle;

public class WorldUtils {
    public static boolean generateEndSpawnPortal(World endWorld) {
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

    public static boolean disableEndBattle(World endWorld) {
        DragonBattle dragonBattle = endWorld.getEnderDragonBattle();
        
        if (dragonBattle == null) {
            return false;
        }

        if (dragonBattle.getEnderDragon() != null) {
            dragonBattle.getEnderDragon().remove();
            Bukkit.getLogger().info("Removed dragon at world: " + endWorld.getName());
        }

        boolean portalGenerated = false;

        try {
            Method generateEndPortalMethod = dragonBattle.getClass().getMethod("generateEndPortal", boolean.class);
            generateEndPortalMethod.invoke(dragonBattle, true);
            portalGenerated = true;
        } catch (NoSuchMethodException ex) {
            Bukkit.getLogger().info("Generating end portal via NMS");
            portalGenerated = NMSWorldUtils.generateEndSpawnPortal(endWorld);
        } catch (Exception ex) {
            Bukkit.getLogger().log(Level.SEVERE, ex.getMessage());
        }
        
        if (portalGenerated) {
            Bukkit.getLogger().info("Generated end portal");
        }

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
