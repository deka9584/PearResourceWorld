package pear.resourceworld.model;

import org.bukkit.Difficulty;
import org.bukkit.WorldType;
import org.bukkit.configuration.ConfigurationSection;

import pear.resourceworld.utils.WorldUtils;

public class ResourceWorldSettings {
    private boolean teleportSpawnOnQuit;
    private boolean disableSetRespawn;
    private boolean keepInventory;
    private Difficulty difficulty;
    private String customSeed;
    private boolean pvp;
    private boolean alwaysDay;
    private boolean allowNetherPortals;
    private boolean allowEndPortals;
    private boolean disableDragonBattle;
    private boolean preventDragonRespawn;
    private WorldType worldType;
    private boolean generateStructures;
    private boolean keepSpawnInMemory;

    public void update(ConfigurationSection configSect) {
        teleportSpawnOnQuit = configSect.getBoolean("teleport-spawn-on-quit");
        disableSetRespawn = configSect.getBoolean("disable-set-respawn");
        keepInventory = configSect.getBoolean("keep-inventory");

        difficulty = WorldUtils.getDifficultyByName(configSect.getString("difficulty"));

        if (difficulty == null) {
            difficulty = Difficulty.EASY;
        }

        customSeed = configSect.getString("custom-seed");
        pvp = configSect.getBoolean("pvp");
        alwaysDay = configSect.getBoolean("always-day");
        allowNetherPortals = configSect.getBoolean("allow-nether-portals"); 
        allowEndPortals = configSect.getBoolean("allow-end-portals");
        disableDragonBattle = configSect.getBoolean("disable-dragon-battle");
        preventDragonRespawn = configSect.getBoolean("prevent-dragon-respawn");
        
        worldType = WorldType.getByName(configSect.getString("world-type"));

        if (worldType == null) {
            worldType = WorldType.NORMAL;
        }

        generateStructures = configSect.getBoolean("generate-structures");
        keepSpawnInMemory = configSect.getBoolean("keep-spawn-in-memory");
    }

    public boolean getTeleportSpawnOnQuit() {
        return teleportSpawnOnQuit;
    }

    public boolean getKeepInventory() {
        return keepInventory;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public String getCustomSeed() {
        return customSeed;
    }

    public boolean getPVP() {
        return pvp;
    }

    public boolean getAlwaysDay() {
        return alwaysDay;
    }

    public boolean getAllowNetherPortals() {
        return allowNetherPortals;
    }

    public boolean getAllowEndPortals() {
        return allowEndPortals;
    }
    
    public boolean getDisableDragonBattle() {
        return disableDragonBattle;
    }

    public boolean getDisableSetRespawn() {
        return disableSetRespawn;
    }

    public boolean getGenerateStructures() {
        return generateStructures;
    }

    public WorldType getWorldType() {
        return worldType;
    }

    public boolean getPreventDragonRespawn() {
        return preventDragonRespawn;
    }
    
    public boolean getKeepSpawnInMemory() {
        return keepSpawnInMemory;
    }
}
