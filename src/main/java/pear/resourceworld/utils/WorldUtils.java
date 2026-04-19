package pear.resourceworld.utils;

import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.World.Environment;

public class WorldUtils {
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
