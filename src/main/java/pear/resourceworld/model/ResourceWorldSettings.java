package pear.resourceworld.model;

import org.bukkit.Difficulty;
import org.bukkit.configuration.ConfigurationSection;

import pear.resourceworld.utils.WorldUtils;

public class ResourceWorldSettings {
    private boolean teleportSpawnOnQuit;
    private boolean keepInventory;
    private Difficulty difficulty;
    private String customSeed;
    private boolean pvp;
    private boolean alwaysDay;
    private boolean allowNetherPortals;
    private boolean allowEndPortals;
    private boolean disableDragonBattle;
    private boolean disableBedRespawn;

    public ResourceWorldSettings(ConfigurationSection configSect) {
        this.teleportSpawnOnQuit = configSect.getBoolean("teleport-spawn-on-quit");
        this.keepInventory = configSect.getBoolean("keep-inventory");

        this.difficulty = WorldUtils.getDifficultyByName(configSect.getString("difficulty"));

        if (this.difficulty == null) {
            this.difficulty = Difficulty.EASY;
        }

        this.customSeed = configSect.getString("custom-seed");
        this.pvp = configSect.getBoolean("pvp");
        this.alwaysDay = configSect.getBoolean("always-day");
        this.allowNetherPortals = configSect.getBoolean("allow-nether-portals"); 
        this.allowEndPortals = configSect.getBoolean("allow-end-portals");
        this.disableDragonBattle = configSect.getBoolean("disable-dragon-battle");
        this.disableBedRespawn = configSect.getBoolean("disable-bed-respawn");
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

    public boolean getPvp() {
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

    public boolean getDisableBedRespawn() {
        return disableBedRespawn;
    }
}
