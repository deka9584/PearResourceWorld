package pear.resourceworld.managers;

import java.time.LocalDate;
import java.util.HashMap;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.World;
import org.bukkit.WorldType;
import org.bukkit.World.Environment;

import pear.resourceworld.PearResourceWorld;
import pear.resourceworld.model.ResourceWorld;
import pear.resourceworld.model.ResourceWorldSettings;
import pear.resourceworld.utils.FileUtils;
import pear.resourceworld.utils.WorldUtils;

public class ResourceWorldsManager {
    private final PearResourceWorld plugin;
    private final MessagesFileManager messagesFm;
    private HashMap<String, ResourceWorld> resourceWorlds = new HashMap<>();
    private ResourceWorldSettings resourceWorldSettings;
    private World spawnWorld;
    private boolean resourceWorldReady = false;

    public ResourceWorldsManager(PearResourceWorld plugin) {
        this.plugin = plugin;
        this.messagesFm = plugin.getMessagesFileManager();
    }

    public boolean canAutoReset() {
        return plugin.getConfig().getBoolean("reset-with-players-online") || plugin.getServer().getOnlinePlayers().size() == 0;
    }

    public boolean isInResourceWorld(Player player) {
        for (ResourceWorld rw : resourceWorlds.values()) {
            if (rw.getWorld().getUID() == player.getWorld().getUID()) {
                return true;
            }
        }
        
        return false;
    }

    public boolean isResourceWorldReady() {
        return resourceWorldReady;
    }

    public void loadWorlds() {
        FileConfiguration config = plugin.getConfig();
        boolean isNewWorld = false;
        String spawnWorldName = config.getString("spawn-world");

        if (spawnWorldName == null || spawnWorldName.isEmpty()) {
            plugin.logWarn("Spawn world not specified: Using the default server spawnpoint!");
            spawnWorld = plugin.getServer().getWorlds().get(0);
        } else {
            spawnWorld = plugin.getServer().getWorld(spawnWorldName);
        }

        if (spawnWorld == null) {
            plugin.logError("Unable to load resource world: Spawn world not found");
            return;
        }

        resourceWorldSettings = new ResourceWorldSettings(config.getConfigurationSection("resource-world-settings"));

        ConfigurationSection dimensions = config.getConfigurationSection("resource-dimensions");
        String resourceOverworldName = dimensions.getString("overworld.name");
        
        if (resourceOverworldName == null || resourceOverworldName.isEmpty()) {
            plugin.logError("Unable to load resource world: Overworld dimension is required");
            return;
        }

        for (String key : dimensions.getKeys(false)) {
            Environment env = getConfigEnvironment(key);

            if (env == null) {
                plugin.logWarn("Invalid dimension:" + key);
                continue;
            }

            if (!dimensions.getBoolean(key + ".enabled") && !key.equals("overworld")) {
                continue;
            }

            String worldName = dimensions.getString(key + ".name");
            double border = dimensions.getDouble(key + ".border");
            ResourceWorld resourceWorld = new ResourceWorld(worldName, env);
            World world = plugin.getServer().getWorld(worldName);

            if (world == null) {
                world = WorldUtils.generateWorld(
                    worldName,
                    resourceWorldSettings.getCustomSeed(),
                    env,
                    WorldType.NORMAL,
                    true
                );

                isNewWorld = true;
                plugin.getLogger().info("Created a new resource world: " + worldName);
            }

            resourceWorld.setWorld(world);

            if (border > 0) {
                resourceWorld.setBorderSize(border);
            }

            resourceWorld.updateWorldFlags(resourceWorldSettings);
            resourceWorlds.put(key, resourceWorld);
            plugin.getLogger().info("Loaded resource world: " + worldName);
        }

        if (isNewWorld) {
            plugin.getDataFileManager().setLastReset(LocalDate.now());
        }

        resourceWorldReady = true;
    }

    public void resetWorlds() {
        if (!resourceWorldReady) {
            plugin.debugLog("Unable to start reset: Resource worlds are not ready");
            return;
        }

        BukkitScheduler scheduler = plugin.getServer().getScheduler();
        resourceWorldReady = false;
        
        plugin.getLogger().info("Resetting resource worlds");
        plugin.getServer().broadcastMessage(messagesFm.getMessage("reset-started"));

        kickAllFromResourceWorld();

        scheduler.runTaskLater(plugin, () -> {
            for (ResourceWorld rw : resourceWorlds.values()) {
                World w = rw.getWorld();
                plugin.getServer().unloadWorld(w, false);
                FileUtils.deleteDirectory(w.getWorldFolder());
                rw.setWorld(null);
                plugin.debugLog("Deleted world: " + rw.getName());
            }

            scheduler.runTaskLater(plugin, () -> {
                for (ResourceWorld rw : resourceWorlds.values()) {

                    World w = WorldUtils.generateWorld(
                        rw.getName(),
                        resourceWorldSettings.getCustomSeed(),
                        rw.getEnvironment(),
                        WorldType.NORMAL,
                        true
                    );

                    rw.setWorld(w);
                    rw.updateWorldBorder();
                    rw.updateWorldFlags(resourceWorldSettings);
                    plugin.debugLog("Generated world: " + rw.getName());
                }

                plugin.getDataFileManager().setLastReset(LocalDate.now());
                plugin.getLogger().info("Resource worlds reset completed");
                plugin.getServer().broadcastMessage(messagesFm.getMessage("reset-completed"));
                resourceWorldReady = true;
            }, 160L);
        }, 80L);
    }

    public boolean teleportPlayerToResourceWorld(Player player) {
        if (!resourceWorldReady) {
            plugin.debugLog("Resource world is not ready");
            return false;
        }

        ResourceWorld resourceWorld = resourceWorlds.get("overworld");

        if (resourceWorld == null || resourceWorld.getWorld() == null) {
            plugin.logError("Resourceworld overworld is not loaded");
            return false;
        }

        return player.teleport(resourceWorld.getWorld().getSpawnLocation());
    }

    public void kickAllFromResourceWorld() {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            if (isInResourceWorld(player)) {
                if (player.teleport(spawnWorld.getSpawnLocation())) {
                    player.sendMessage(messagesFm.getMessage("teleport-to-spawn"));
                } else {
                    player.kickPlayer(messagesFm.getMessage("kick-from-resource-world"));
                }
            }
        }
    }

    private Environment getConfigEnvironment(String key) {
        switch (key) {
            case "overworld":
                return Environment.NORMAL;
            case "nether":
                return Environment.NETHER;
            case "end":
                return Environment.THE_END;
            default:
                return null;
        }
    }
}
