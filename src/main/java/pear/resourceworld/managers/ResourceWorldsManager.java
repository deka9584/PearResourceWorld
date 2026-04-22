package pear.resourceworld.managers;

import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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

    private HashMap<String, ResourceWorld> resourceWorlds = new HashMap<>();
    private ResourceWorldSettings resourceWorldSettings;
    private World spawnWorld;
    private boolean resourceWorldReady = false;

    public ResourceWorldsManager(PearResourceWorld plugin) {
        this.plugin = plugin;
    }

    public boolean canAutoReset() {
        return plugin.getConfig().getBoolean("reset-with-players-online") || plugin.getServer().getOnlinePlayers().size() == 0;
    }

    public ResourceWorld getResourceWorld(String dimension) {
        return resourceWorlds.get(dimension);
    }

    public ResourceWorldSettings getResourceWorldSettings() {
        return resourceWorldSettings;
    }

    public boolean isResourceWorld(World world) {
        for (ResourceWorld rw : resourceWorlds.values()) {
            if (rw.getName().equals(world.getName())) {
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
        String spawnWorldName = config.getString("spawn-world");
        resourceWorldReady = false;

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
        boolean isNewWorld = false;
        
        if (resourceOverworldName == null || resourceOverworldName.isEmpty()) {
            plugin.logError("Unable to load resource world: Overworld dimension is required");
            return;
        }

        resourceWorlds.clear();

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
                if (!FileUtils.existDirectory(plugin.getServer().getWorldContainer().getPath(), worldName)) {
                    plugin.getLogger().info("Resource world does not exists generating a new one");
                    isNewWorld = true;
                }
                
                plugin.getLogger().info("Loading resource world: " + worldName);
                world = createRwWorld(resourceWorld);

                plugin.getLogger().info("Loaded resource world: " + worldName);
            }

            resourceWorld.setWorld(world);

            if (border > 0) {
                resourceWorld.setBorderSize(border);
            }

            resourceWorld.updateWorldFlags(resourceWorldSettings);
            resourceWorlds.put(key, resourceWorld);
            plugin.getLogger().info("Loaded resource world: " + worldName);
        }

        finalizeWorldsLoad();

        if (isNewWorld && resourceWorlds.size() > 0) {
            plugin.getLogger().info("Created new worlds: " + String.join(", ", resourceWorlds.keySet()));
            plugin.getDataFileManager().setLastReset(LocalDate.now());
        }
    }

    public void resetWorlds() {
        if (!resourceWorldReady) {
            plugin.debugLog("Unable to start reset: Resource worlds are not ready");
            return;
        }

        BukkitScheduler scheduler = plugin.getServer().getScheduler();
        resourceWorldReady = false;
        
        plugin.getLogger().info("Resetting resource worlds");

        plugin.getServer().broadcastMessage(
            plugin.getMessagesFileManager().getMessage("reset-started")
        );

        kickAllFromResourceWorld();

        scheduler.runTaskLater(plugin, () -> {
            List<File> folderToDelete = new ArrayList<>();

            for (ResourceWorld rw : resourceWorlds.values()) {
                World w = rw.getWorld();

                if (!plugin.getServer().unloadWorld(w, false)) {
                    plugin.logError("Unable to unload world: " + rw.getName());
                    continue;
                }

                rw.setWorld(null);
                folderToDelete.add(w.getWorldFolder());
            }

            scheduler.runTaskAsynchronously(plugin, () -> {
                for (File folder : folderToDelete) {
                    if (!FileUtils.deleteDirectory(folder)) {
                        plugin.logError("Unable to delete world: " + folder.getName());
                        continue;
                    }
                    
                    plugin.debugLog("Deleted world: " + folder.getName());
                }

                if (!plugin.isEnabled()) {
                    plugin.logWarn("Plugin disabled during async reset. Shkipping world regen");
                    return;
                }
                
                scheduler.runTaskLater(plugin, () -> {
                    for (ResourceWorld rw : resourceWorlds.values()) {
                        if (rw.getWorld() != null) {
                            plugin.logError("Unable to regenerate world: " + rw.getName());
                            continue;
                        }
    
                        World w = createRwWorld(rw);
    
                        rw.setWorld(w);
                        rw.updateWorldBorder();
                        rw.updateWorldFlags(resourceWorldSettings);

                        plugin.debugLog("Generated world: " + rw.getName());
                    }

                    finalizeWorldsLoad();
    
                    plugin.getDataFileManager().setLastReset(LocalDate.now());
                    plugin.getLogger().info("Resource worlds reset completed");

                    plugin.getServer().broadcastMessage(
                        plugin.getMessagesFileManager().getMessage("reset-completed")
                    );
                }, 160L);
            });

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
            if (isResourceWorld(player.getWorld())) {
                if (player.teleport(spawnWorld.getSpawnLocation())) {
                    player.sendMessage(
                        plugin.getMessagesFileManager().getMessage("teleport-to-spawn")
                    );
                } else {
                    player.kickPlayer(
                        plugin.getMessagesFileManager().getMessage("kick-from-resource-world")
                    );
                }
            }
        }
    }

    private World createRwWorld(ResourceWorld rw) {
        Environment env = rw.getEnvironment();

        World world = WorldUtils.generateWorld(
            rw.getName(),
            resourceWorldSettings.getCustomSeed(),
            env,
            WorldType.NORMAL,
            true
        );

        return world;
    }

    private void finalizeWorldsLoad() {
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            
            if (resourceWorldSettings.getDisableDragonBattle()) {
                ResourceWorld rwEnd = resourceWorlds.get("end");

                if (rwEnd != null && rwEnd.getWorld() != null) {
                    World endW = rwEnd.getWorld();

                    if (WorldUtils.removeEnderDragon(endW)) {
                        plugin.debugLog("Removed ender dragon");
                    }

                    plugin.getRwPortalHelper().activateEndExitPortal(endW);
                }
            }

            resourceWorldReady = true;
        });
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
