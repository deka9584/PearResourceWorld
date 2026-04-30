package pear.resourceworld.managers;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.World;
import org.bukkit.World.Environment;

import pear.resourceworld.PearResourceWorld;
import pear.resourceworld.model.RWDimension;
import pear.resourceworld.model.ResourceWorld;
import pear.resourceworld.model.ResourceWorldSettings;
import pear.resourceworld.utils.FileUtils;
import pear.resourceworld.utils.WorldUtils;

public class ResourceWorldsManager {
    private final PearResourceWorld plugin;

    private HashMap<RWDimension, ResourceWorld> resourceWorlds = new HashMap<>();
    private ResourceWorldSettings resourceWorldSettings = new ResourceWorldSettings();
    private World spawnWorld;
    private boolean useCustomWorlds;
    private boolean resourceWorldReady;

    public ResourceWorldsManager(PearResourceWorld plugin) {
        this.plugin = plugin;
    }

    public boolean canAutoReset() {
        return plugin.getConfig().getBoolean("reset-with-players-online") || plugin.getServer().getOnlinePlayers().size() == 0;
    }

    public Set<RWDimension> getEnabledDimensions() {
        return resourceWorlds.keySet();
    }

    public ResourceWorld getResourceWorld(RWDimension dimension) {
        return resourceWorlds.get(dimension);
    }

    public ResourceWorldSettings getResourceWorldSettings() {
        return resourceWorldSettings;
    }

    public World getSpawnWorld() {
        return spawnWorld;
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

        useCustomWorlds = config.getBoolean("use-custom-worlds");
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

        resourceWorldSettings.update(config.getConfigurationSection("resource-world-settings"));

        ConfigurationSection dimensions = config.getConfigurationSection("resource-dimensions");
        String resourceOverworldName = dimensions.getString("overworld.name");
        boolean isNewWorld = false;
        
        if (resourceOverworldName == null || resourceOverworldName.isEmpty()) {
            plugin.logError("Unable to load resource world: Overworld dimension is required");
            return;
        }

        resourceWorlds.clear();

        for (String key : dimensions.getKeys(false)) {
            RWDimension dimension = RWDimension.getFromName(key);

            if (dimension == null) {
                plugin.logWarn("Invalid dimension:" + key);
                continue;
            }

            if (!dimensions.getBoolean(key + ".enabled") && !key.equals("overworld")) {
                continue;
            }

            String worldName = dimensions.getString(key + ".name");
            double border = dimensions.getDouble(key + ".border");
            Environment env = dimension.getEnvironment();
            ResourceWorld resourceWorld = new ResourceWorld(worldName, env);
            World world = plugin.getServer().getWorld(worldName);

            if (world == null) {
                if (!FileUtils.existDirectory(plugin.getServer().getWorldContainer().getPath(), worldName)) {
                    plugin.getLogger().info("Resource world does not exists generating a new one");
                    isNewWorld = true;

                    if (useCustomWorlds && existsPreloaedWorld(worldName)) {
                        plugin.getLogger().info("Preloaded world found: " + worldName);

                        if (!copyPreloadedWorld(worldName)) {
                            plugin.logError("Unable to copy preloaded world: " + worldName);
                            continue;
                        }
                    }
                }
                
                plugin.getLogger().info("Loading resource world: " + worldName);
                world = createRwWorld(resourceWorld);
            }

            resourceWorld.setWorld(world);

            if (border > 0) {
                resourceWorld.setBorderSize(border);
            }

            resourceWorld.updateWorldFlags(resourceWorldSettings);
            resourceWorlds.put(dimension, resourceWorld);
            plugin.getLogger().info("Loaded resource world: " + worldName);
        }

        finalizeWorldsLoad(false);

        if (isNewWorld && resourceWorlds.size() > 0) {
            plugin.getLogger().info("Created new resource world");
            plugin.updateLastResetDate();
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
                    String name = folder.getName();

                    if (!FileUtils.deleteDirectory(folder)) {
                        plugin.logError("Unable to delete world: " + name);
                        continue;
                    }
                    
                    plugin.debugLog("Deleted world: " + name);

                    if (useCustomWorlds && existsPreloaedWorld(name)) {
                        if (!copyPreloadedWorld(name)) {
                            plugin.logError("Unable to delete world: " + name);
                            continue;
                        }

                        plugin.debugLog("Copied world: " + name);
                    }
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

                    finalizeWorldsLoad(true);
                    plugin.updateLastResetDate();
                }, 160L);
            });

        }, 80L);
    }

    public boolean teleportPlayerToResourceWorld(Player player, RWDimension dim) {
        if (!resourceWorldReady) {
            plugin.debugLog("Resource world is not ready");
            return false;
        }

        ResourceWorld resourceWorld = resourceWorlds.get(dim);

        if (resourceWorld == null || resourceWorld.getWorld() == null) {
            plugin.logError("Resourceworld overworld is not loaded");
            return false;
        }

        return player.teleport(resourceWorld.getWorld().getSpawnLocation());
    }

    public void kickAllFromResourceWorld() {
        String message = plugin.getMessagesFileManager().getMessage("kick-from-resource-world");

        for (Player player : plugin.getServer().getOnlinePlayers()) {
            if (isResourceWorld(player.getWorld())) {
                if (player.teleport(spawnWorld.getSpawnLocation())) {
                    player.sendMessage(message);
                } else {
                    player.kickPlayer(message);
                }
            }
        }
    }

    private boolean copyPreloadedWorld(String worldName) {
        File from = new File(plugin.getDataFolder(), worldName);
        File to = new File(plugin.getServer().getWorldContainer(), worldName);
        return FileUtils.copyDirectory(from.toPath(), to.toPath());
    }

    private World createRwWorld(ResourceWorld rw) {
        Environment env = rw.getEnvironment();

        World world = WorldUtils.generateWorld(
            rw.getName(),
            resourceWorldSettings.getCustomSeed(),
            env,
            resourceWorldSettings.getWorldType(),
            resourceWorldSettings.getGenerateStructures()
        );

        return world;
    }

    private boolean existsPreloaedWorld(String worldName) {
        return FileUtils.existDirectory(plugin.getDataFolder().getPath(), worldName);
    }

    private void finalizeWorldsLoad(boolean isReset) {
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            if (resourceWorldSettings.getDisableDragonBattle()) {
                ResourceWorld rwEnd = resourceWorlds.get(RWDimension.END);

                if (rwEnd != null && rwEnd.getWorld() != null) {
                    World endW = rwEnd.getWorld();

                    if (WorldUtils.removeEnderDragon(endW)) {
                        plugin.debugLog("Removed ender dragon");
                    }

                    if (!WorldUtils.hasEndExitPortal(endW)) {
                        plugin.getRwPortalHelper().activateEndExitPortal(endW);
                    } else {
                        plugin.debugLog("End exit portal already activated");
                    }
                }
            }

            if (isReset) {
                plugin.getLogger().info("Resource worlds reset completed");

                plugin.getServer().broadcastMessage(
                    plugin.getMessagesFileManager().getMessage("reset-completed")
                );
            }

            resourceWorldReady = true;
        });
    }
}
