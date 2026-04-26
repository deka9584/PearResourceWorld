package pear.resourceworld.helpers;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import pear.resourceworld.PearResourceWorld;
import pear.resourceworld.managers.CooldownManager;
import pear.resourceworld.managers.MessagesFileManager;
import pear.resourceworld.managers.ResourceWorldsManager;
import pear.resourceworld.model.RWDimension;
import pear.resourceworld.model.ResourceWorld;
import pear.resourceworld.utils.LocationUtils;

public class TeleportHelper {
    private final PearResourceWorld plugin;
    private final ResourceWorldsManager rwManager;
    private final MessagesFileManager messagesFm;
    private final CooldownManager cooldownManager;
    private final Set<UUID> activeTpDelays = new HashSet<>();
    private final Set<UUID> activeTpSearches = new HashSet<>();

    public TeleportHelper(PearResourceWorld plugin) {
        this.plugin = plugin;
        this.rwManager = plugin.getResourceWorldsManager();
        this.messagesFm = plugin.getMessagesFileManager();
        this.cooldownManager = plugin.getCooldownManager();
    }

    public void handlePlayerQuit(UUID playerUUID) {
        activeTpDelays.remove(playerUUID);
        activeTpSearches.remove(playerUUID);
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
        
        teleportToWorld(player, world, range);
    }

    public void teleportToSpawn(Player player) {
        boolean bypassDelay = cooldownManager.canBypassDelay(player);
        int delay = bypassDelay ? 0 : plugin.getConfig().getInt("teleport-delay");
        Location spawnLoc = plugin.getResourceWorldsManager().getSpawnWorld().getSpawnLocation();

        if (delay == 0) {
            teleportPlayer(player, spawnLoc);
            return;
        }

        UUID playerUUID = player.getUniqueId();

        if (!activeTpDelays.add(playerUUID)) {
            return;
        }

        Location playerLoc = player.getLocation();

        String teleportMsg = messagesFm.getMessage("teleport-delay")
            .replaceAll("%seconds%", String.valueOf(delay));

        player.sendMessage(teleportMsg);

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (!activeTpDelays.remove(playerUUID) || !player.isOnline()) {
                plugin.debugLog("Player logged out: teleport cancelled");
                return;
            }

            if (!LocationUtils.isSamePosition(playerLoc, player.getLocation())) {
                player.sendMessage(messagesFm.getMessage("teleport-cancelled-moved"));
                return;
            }

            teleportPlayer(player, spawnLoc);
        }, 20 * delay);
    }
    
    public void teleportToWorld(Player player, World world, int range) {
        UUID playerUUID = player.getUniqueId();

        boolean bypassCooldown = cooldownManager.canBypassCooldown(player);
        boolean bypassDelay = cooldownManager.canBypassDelay(player);

        int delay = bypassDelay ? 0 : plugin.getConfig().getInt("teleport-delay");
        int cooldownSeconds = bypassCooldown ? 0 : cooldownManager.getTpRemainingSeconds(playerUUID);

        if (cooldownSeconds > 0) {
            player.sendMessage(
                messagesFm.getMessage("teleport-cooldown")
                    .replaceAll("%seconds%", String.valueOf(cooldownSeconds))
            );
            return;
        }
        
        if (delay == 0) {
            teleportSafely(player, world, range, 0);
            return;
        }

        if (!activeTpDelays.add(playerUUID)) {
            return;
        }

        Location playerLoc = player.getLocation();

        String teleportMsg = messagesFm.getMessage("teleport-delay")
            .replaceAll("%seconds%", String.valueOf(delay));

        player.sendMessage(teleportMsg);

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (!activeTpDelays.remove(playerUUID) || !player.isOnline()) {
                plugin.debugLog("Player logged out: teleport cancelled");
                return;
            }

            if (rwManager.isResourceWorld(world) && !rwManager.isResourceWorldReady()) {
                player.sendMessage(messagesFm.getMessage("reset-still-in-progress"));
                return;
            }

            if (!LocationUtils.isSamePosition(playerLoc, player.getLocation())) {
                player.sendMessage(messagesFm.getMessage("teleport-cancelled-moved"));
                return;
            }

            cooldownManager.addTpCooldown(playerUUID);
            teleportSafely(player, world, range, 0);
        }, 20 * delay);
    }

    private void teleportSafely(Player player, World world, int range, int attempt) {
        UUID playerUUID = player.getUniqueId();

        if (attempt > 50) {
            endLocationSearch(playerUUID);
            plugin.logWarn("Unable to find a safe location to teleport player: " + player.getName());
            player.sendMessage(messagesFm.getMessage("no-safe-locaiton"));
            return;
        }

        if (!player.isOnline()) {
            endLocationSearch(playerUUID);
            return;
        }
        
        Location spawnLocation = world.getSpawnLocation();

        if (range == 0) {
            if (player.teleport(spawnLocation)) {
                player.sendMessage(messagesFm.getMessage("teleport-success"));
                return;
            }

            player.sendMessage(messagesFm.getMessage("teleport-failed"));
            return;
        }

        if (attempt == 0) {
            if (!activeTpSearches.add(playerUUID)) {
                plugin.debugLog("Prevent multiple location searches on same player");
                return;
            }
            
            plugin.debugLog("Searching safe location for player: " + player.getName());
            player.sendMessage(messagesFm.getMessage("searching-safe-location"));
        }

        int spawnX = spawnLocation.getBlockX();
        int spawnZ = spawnLocation.getBlockZ();

        int randomX = LocationUtils.getRandomInt(spawnX - range, spawnX + range);
        int randomZ = LocationUtils.getRandomInt(spawnZ - range, spawnZ + range);

        plugin.getServer().getScheduler().runTask(plugin, () -> {
            if (!activeTpSearches.contains(playerUUID) || !player.isOnline()) {
                endLocationSearch(playerUUID);
                return;
            }

            int highestY = world.getHighestBlockYAt(randomX, randomZ) + 1;
            Location randomLoc = new Location(world, randomX, highestY, randomZ);

            if (LocationUtils.isLocationSafe(randomLoc)) {
                endLocationSearch(playerUUID);
                teleportPlayer(player, randomLoc);
                plugin.debugLog("Safe location found in attempt: " + attempt);
                return;
            }

            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                teleportSafely(player, world, range, attempt + 1);
            }, 1L);
        });
    }

    private void endLocationSearch(UUID playerUUID) {
        activeTpSearches.remove(playerUUID);
        plugin.debugLog("End safe location search search");
    }

    private void teleportPlayer(Player player, Location loc) {
        if (player.teleport(loc)) {
            player.sendMessage(messagesFm.getMessage("teleport-success"));
            return;
        }

        player.sendMessage(messagesFm.getMessage("teleport-failed"));
    }
}
