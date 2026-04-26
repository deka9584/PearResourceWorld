package pear.resourceworld.helpers;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import pear.resourceworld.PearResourceWorld;
import pear.resourceworld.managers.MessagesFileManager;
import pear.resourceworld.managers.ResourceWorldsManager;
import pear.resourceworld.model.RWDimension;
import pear.resourceworld.model.ResourceWorld;
import pear.resourceworld.utils.LocationUtils;

public class TeleportHelper {
    private final PearResourceWorld plugin;
    private final ResourceWorldsManager rwManager;
    private final MessagesFileManager messagesFm;

    public TeleportHelper(PearResourceWorld plugin) {
        this.plugin = plugin;
        this.rwManager = plugin.getResourceWorldsManager();
        this.messagesFm = plugin.getMessagesFileManager();
    }

    public void teleportToRwOverworld(Player player) {
        if (!rwManager.isResourceWorldReady()) {
            player.sendMessage(messagesFm.getMessage("reset-still-in-progress"));
            return;
        }

        if (rwManager.isResourceWorld(player.getWorld())) {
            player.sendMessage(messagesFm.getMessage("already-resource-world-self"));
            return;
        }
        
        ResourceWorld resourceWorld = rwManager.getResourceWorld(RWDimension.OVERWORLD);
        World world = resourceWorld != null ? resourceWorld.getWorld() : null;

        if (world == null) {
            plugin.logError("Resource world not found");
            player.sendMessage(messagesFm.getMessage("teleport-failed"));
            return;
        }
        
        int range = plugin.getConfig().getInt("teleport-range");
        int delay = plugin.getConfig().getInt("teleport-delay");

        teleportToWorld(player, world, delay, range);
    }
    
    public void teleportToWorld(Player player, World world, int delay, int range) {
        if (delay == 0) {
            teleportSafely(player, world, range, range);
            return;
        }

        Location playerLoc = player.getLocation();

        String teleportMsg = messagesFm.getMessage("teleport-delay")
            .replaceAll("%seconds%", String.valueOf(delay));

        player.sendMessage(teleportMsg);

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (!player.isOnline()) {
                plugin.debugLog("Player logged out: teleport cancelled");
                return;
            }

            if (!rwManager.isResourceWorldReady()) {
                player.sendMessage(messagesFm.getMessage("reset-still-in-progress"));
                return;
            }

            if (!LocationUtils.isSamePosition(playerLoc, player.getLocation())) {
                player.sendMessage(messagesFm.getMessage("teleport-cancelled-moved"));
                return;
            }

            teleportSafely(player, world, range, 0);
        }, 20 * delay);
    }

    private void teleportSafely(Player player, World world, int range, int attempt) {
        Location spawnLocation = world.getSpawnLocation();

        if (range == 0) {
            if (player.teleport(spawnLocation)) {
                player.sendMessage(messagesFm.getMessage("teleport-success"));
                return;
            }

            player.sendMessage(messagesFm.getMessage("teleport-failed"));
            return;
        }

        if (attempt > 50) {
            plugin.logWarn("Unable to find a safe location to teleport player: " + player.getName());
            player.sendMessage(messagesFm.getMessage("no-safe-locaiton"));
            return;
        }

        if (attempt == 0) {
            player.sendMessage(messagesFm.getMessage("searching-safe-location"));
        } 

        int spawnX = spawnLocation.getBlockX();
        int spawnZ = spawnLocation.getBlockZ();

        int randomX = LocationUtils.getRandomInt(spawnX - range, spawnX + range);
        int randomZ = LocationUtils.getRandomInt(spawnZ - range, spawnZ + range);

        plugin.getServer().getScheduler().runTask(plugin, () -> {
            if (!player.isOnline()) {
                plugin.debugLog("Player logged out: teleport cancelled");
                return;
            }
            
            world.getChunkAt(randomX, randomZ);

            int highestY = world.getHighestBlockYAt(randomX, randomZ) + 1;
            Location randomLoc = new Location(world, randomX, highestY, randomZ);

            processLocationTeleport(player, randomLoc, range, attempt);
        });
    }

    private void processLocationTeleport(Player player, Location loc, int range, int attempt) {
        if (!LocationUtils.isLocationSafe(loc)) {
            teleportSafely(player, loc.getWorld(), range, attempt + 1);
            return;
        }

        if (player.teleport(loc)) {
            player.sendMessage(messagesFm.getMessage("teleport-success"));
            return;
        }

        player.sendMessage(messagesFm.getMessage("teleport-failed"));
    }
}
