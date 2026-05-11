package pear.resourceworld.helpers;

import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import pear.resourceworld.PearResourceWorld;
import pear.resourceworld.managers.CooldownManager;
import pear.resourceworld.managers.MessagesFileManager;
import pear.resourceworld.managers.ResourceWorldsManager;
import pear.resourceworld.managers.TeleportManager;
import pear.resourceworld.model.RWDimension;
import pear.resourceworld.model.ResourceWorld;
import pear.resourceworld.utils.LocationUtils;
import pear.resourceworld.utils.Utils;

public class TeleportHelper {
    private final PearResourceWorld plugin;
    private final ResourceWorldsManager rwManager;
    private final MessagesFileManager messagesFm;
    private final CooldownManager cooldownManager;
    private final TeleportManager teleportManager;

    public TeleportHelper(PearResourceWorld plugin) {
        this.plugin = plugin;
        this.rwManager = plugin.getResourceWorldsManager();
        this.messagesFm = plugin.getMessagesFileManager();
        this.cooldownManager = plugin.getCooldownManager();
        this.teleportManager = plugin.getTeleportManager();
    }

    public boolean adminTeleport(Player player, CommandSender sender, RWDimension dim) {
        if (!rwManager.isResourceWorldReady()) {
            sender.sendMessage(messagesFm.getMessage("reset-still-in-progress"));
            return false;
        }

        if (!rwManager.teleportPlayerToResourceWorld(player, dim)) {
            sender.sendMessage(messagesFm.getMessage("teleport-failed"));
            return false;
        }

        sender.sendMessage(messagesFm.getMessage("teleport-success"));
        return true;
    }

    public boolean adminTeleportSpawn(Player player, CommandSender sender) {
        if (!player.teleport(rwManager.getSpawnWorld().getSpawnLocation())) {
            sender.sendMessage(messagesFm.getMessage("teleport-failed"));
            return false;
        }

        sender.sendMessage(messagesFm.getMessage("teleport-success"));
        return true;
    }

    public void signTeleport(Player player) {
        if (rwManager.isResourceWorld(player.getWorld())) {
            teleportToSpawn(player, true);
        } else {
            teleportToRwOverworld(player, true);
        }
    }

    public void teleportToRwOverworld(Player player, boolean fromSign) {
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
        
        delayedTeleport(player, world.getSpawnLocation(), teleportManager.getRtpRange(), true, fromSign);
    }

    public void teleportToSpawn(Player player, boolean fromSign) {
        Location spawnLoc = plugin.getResourceWorldsManager().getSpawnWorld().getSpawnLocation();
        delayedTeleport(player, spawnLoc, 0, false, fromSign);
    }

    public void delayedTeleport(Player player, Location destination, int range, boolean useCooldown, boolean fromSign) {
        int delay = teleportManager.getTpDelay(player, fromSign);
        int cooldownSeconds = useCooldown ? cooldownManager.getTpRemainingSeconds(player) : 0;

        if (cooldownSeconds > 0) {
            player.sendMessage(
                messagesFm.getMessage("teleport-cooldown")
                    .replaceAll("%seconds%", String.valueOf(cooldownSeconds))
            );
            return;
        }

        if (delay == 0) {
            teleportSafely(player, destination, range, cooldownSeconds);
            return;
        }

        UUID playerUUID = player.getUniqueId();

        if (teleportManager.isDelayActive(playerUUID) || teleportManager.isSearchActive(playerUUID)) {
            return;
        }

        Location playerLoc = player.getLocation();

        player.sendMessage(
            messagesFm.getMessage("teleport-delay")
                .replaceAll("%seconds%", String.valueOf(delay))
        );

        BukkitTask task = plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (!teleportManager.removeActiveDelay(playerUUID)) { 
                return;
            }

            if (!isDestinationWorldReady(destination)) {
                plugin.debugLog("Teleport cancelled: world not loaded");
                return;
            }

            if (!LocationUtils.isSamePosition(playerLoc, player.getLocation())) {
                player.sendMessage(messagesFm.getMessage("teleport-cancelled-moved"));
                return;
            }

            if (useCooldown) {
                cooldownManager.addTpCooldown(playerUUID);
            }

            teleportSafely(player, destination, range, 0);
        }, 20 * delay);

        teleportManager.addActiveDelay(playerUUID, task);
    }

    private void teleportSafely(Player player, Location destination, int range, int attempt) {
        UUID playerUUID = player.getUniqueId();

        if (attempt > 50) {
            teleportManager.endLocationSearch(playerUUID);
            plugin.logWarn("Unable to find a safe location to teleport player: " + player.getName());
            player.sendMessage(messagesFm.getMessage("no-safe-locaiton"));
            return;
        }

        if (!player.isOnline()) {
            teleportManager.endLocationSearch(playerUUID);
            return;
        }

        if (range == 0) {
            teleportPlayer(player, destination);
            return;
        }

        if (attempt == 0) {
            if (teleportManager.isSearchActive(playerUUID)) {
                plugin.debugLog("Prevent multiple location searches on same player");
                return;
            }
            
            plugin.debugLog("Searching safe location for player: " + player.getName());
            player.sendMessage(messagesFm.getMessage("searching-safe-location"));
        }

        int spawnX = destination.getBlockX();
        int spawnZ = destination.getBlockZ();

        int randomX = Utils.getRandomInt(spawnX - range, spawnX + range);
        int randomZ = Utils.getRandomInt(spawnZ - range, spawnZ + range);

        BukkitTask task = plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (!teleportManager.isSearchActive(playerUUID)) {
                return;
            }

            if (!isDestinationWorldReady(destination)) {
                teleportManager.endLocationSearch(playerUUID);
                plugin.debugLog("Teleport cancelled: world not loaded");
                return;
            }

            World world = destination.getWorld();
            int highestY = world.getHighestBlockYAt(randomX, randomZ) + 1;
            Location randomLoc = new Location(world, randomX, highestY, randomZ);

            if (LocationUtils.isLocationSafe(randomLoc)) {
                teleportManager.endLocationSearch(playerUUID);
                teleportPlayer(player, randomLoc);
                plugin.debugLog("Safe location found in attempt: " + attempt);
                return;
            }

            teleportSafely(player, destination, range, attempt + 1);
        }, 10);

        teleportManager.addActiveSearch(playerUUID, task);
    }

    private void teleportPlayer(Player player, Location loc) {
        if (player.teleport(loc)) {
            player.sendMessage(messagesFm.getMessage("teleport-success"));
            return;
        }

        player.sendMessage(messagesFm.getMessage("teleport-failed"));
    }

    private boolean isDestinationWorldReady(Location dest) {
        if (!dest.isWorldLoaded()) {
            return false;
        }

        if (!rwManager.isResourceWorldReady() && rwManager.isResourceWorld(dest.getWorld())) {
            return false;
        }

        return true;
    }
}
