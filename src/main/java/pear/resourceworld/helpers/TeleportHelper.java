package pear.resourceworld.helpers;

import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;

import pear.resourceworld.PearResourceWorld;
import pear.resourceworld.managers.MessagesFileManager;
import pear.resourceworld.managers.ResourceWorldsManager;
import pear.resourceworld.model.RWDimension;
import pear.resourceworld.model.ResourceWorld;

public class TeleportHelper {
    private final PearResourceWorld plugin;
    private final ResourceWorldsManager rwManager;
    private final MessagesFileManager messagesFm;

    public TeleportHelper(PearResourceWorld plugin) {
        this.plugin = plugin;
        this.rwManager = plugin.getResourceWorldsManager();
        this.messagesFm = plugin.getMessagesFileManager();
    }

    // public Location findSafeRandomLocation(World world, int radius) {
    //     Random random = new Random();

    //     for (int i = 0; i < 50; i++) {
    //         int x = random.nextInt(radius * 2) - radius;
    //         int z = random.nextInt(radius * 2) - radius;

    //         int y = world.getHighestBlockYAt(x, z);
    //         Location loc = new Location(world, x, y, z);

    //         Material block = loc.getBlock().getType();
    //         Material below = loc.clone().add(0, -1, 0).getBlock().getType();

    //         if (block.isAir() &&
    //             below.isSolid() &&
    //             below != Material.LAVA &&
    //             below != Material.WATER &&
    //             below != Material.CACTUS) {

    //             return loc.add(0.5, 1, 0.5);
    //         }
    //     }

    //     return null;
    // }

    public void teleportPlayerToResourceWorld(Player player) {
        if (!rwManager.isResourceWorldReady()) {
            player.sendMessage(messagesFm.getMessage("reset-still-in-progress"));
            return;
        }

        if (rwManager.isResourceWorld(player.getWorld())) {
            player.sendMessage(messagesFm.getMessage("already-resource-world-self"));
            return;
        }

        ResourceWorld resourceWorld = rwManager.getResourceWorld(RWDimension.OVERWORLD);

        if (resourceWorld == null || resourceWorld.getWorld() == null) {
            plugin.logError("Resource world not found");
            player.sendMessage(messagesFm.getMessage("teleport-failed"));
            return;
        }

        World world = resourceWorld.getWorld();

        int radius = plugin.getConfig().getInt("teleport-radius");
        int delay = plugin.getConfig().getInt("teleport-delay");

        teleportPlayer(player, world.getSpawnLocation(), delay);

        // if (radius == 0) {
        //     teleportPlayer(player, world.getSpawnLocation(), delay);
        //     return;
        // }

        // Location randomLoc = findSafeRandomLocation(world, radius);

        // if (randomLoc == null) {
        //     player.sendMessage(messagesFm.getMessage("no-safe-locaiton"));
        //     return;
        // }

        // teleportPlayer(player, randomLoc, delay);
    }

    public void teleportPlayer(Player player, Location destination, int delay) {
        // int radius = plugin.getConfig().getInt("random-teleport-radius");
        // int delay = plugin.getConfig().getInt("teleport-delay");

        if (delay == 0) {
            if (!player.teleport(destination)) {
                player.sendMessage(messagesFm.getMessage("teleport-failed"));
                return;
            }

            player.sendMessage(messagesFm.getMessage("teleport-success"));
            return;
        }

        Location loc = player.getLocation();

        String teleportMsg = messagesFm.getMessage("teleport-delay")
            .replaceAll("%seconds%", String.valueOf(delay));

        player.sendMessage(teleportMsg);

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (player == null || !player.isOnline()) {
                plugin.debugLog("Player logged out: teleport cancelled");
                return;
            }

            if (!player.getLocation().equals(loc)) {
                player.sendMessage(messagesFm.getMessage("teleport-cancelled-moved"));
                return;
            }

            if (!player.teleport(destination)) {
                player.sendMessage(messagesFm.getMessage("teleport-failed"));
                return;
            }

            player.sendMessage(messagesFm.getMessage("teleport-success"));
        }, 20 * delay);
    }
}
