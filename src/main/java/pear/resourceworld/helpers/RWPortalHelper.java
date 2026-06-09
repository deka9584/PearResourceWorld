package pear.resourceworld.helpers;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.PortalType;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.entity.Player;

import pear.resourceworld.PearResourceWorld;
import pear.resourceworld.managers.ResourceWorldsManager;
import pear.resourceworld.model.RWDimension;
import pear.resourceworld.model.ResourceWorld;
import pear.resourceworld.utils.NMSWorldUtils;
import pear.resourceworld.utils.WorldUtils;

public class RWPortalHelper {
    private final PearResourceWorld plugin;
    private final ResourceWorldsManager rwManager;
    private final Set<UUID> skipEndCreditsRunning = new HashSet<>();

    public RWPortalHelper(PearResourceWorld plugin) {
        this.plugin = plugin;
        this.rwManager = plugin.getResourceWorldsManager();
    }

    public boolean activateEndExitPortal(World endWorld) {
        plugin.getLogger().info("Creating end spawn portal");

        if (WorldUtils.setDragonPreviouslyKilled(endWorld, true)) {
            plugin.debugLog("Set dragon previously killed for world: " + endWorld.getName());

            if (WorldUtils.generateEndExitPortal(endWorld)) {
                plugin.getLogger().info("Created end spawn portal");
                return true;
            }
        }

        if (NMSWorldUtils.generateEndExitPortal(endWorld, true)) {
            plugin.getLogger().info("Created end spawn portal using NMS");
            return true;
        }

        plugin.logWarn("End spawn portal creation failed");
        return false;
    }

    public boolean isPortalAllowed(PortalType portalType) {
        switch (portalType) {
            case NETHER:
                return rwManager.getRWSettings().getAllowNetherPortals();
            case ENDER:
                return rwManager.getRWSettings().getAllowEndPortals();
            default:
                return false;
        }
    }

    public Location getPortalDestination(Location from, PortalType portalType, Location originalTo) {
        Environment fromEnv = from.getWorld().getEnvironment();

        if (fromEnv == Environment.NORMAL && portalType == PortalType.NETHER) {
            return getOverworldToNetherLoc(from);
        }

        if (fromEnv == Environment.NETHER && portalType == PortalType.NETHER) {
            return getNetherToOverworldLoc(from);
        }

        if (fromEnv == Environment.NORMAL && portalType == PortalType.ENDER) {
            return getOverworldToEndLoc(from, originalTo);
        }

        return null;
    }

    public boolean skipEndCredits(UUID playerUUID) {
        if (!skipEndCreditsRunning.add(playerUUID)) {
            return false;
        }

        plugin.getServer().getScheduler().runTask(plugin, () -> {
            Player player = plugin.getServer().getPlayer(playerUUID);

            if (player == null) {
                skipEndCreditsRunning.remove(playerUUID);
                return;
            }

            if (!player.isValid() && player.isDead()) {
                if (!NMSWorldUtils.skipEndCredits(player)) {
                    plugin.logWarn("Unable to skip end credits for player: " + player.getName());
                }
            } else {
                plugin.debugLog("End credits not showed to player: " + player.getName());
            }

            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                skipEndCreditsRunning.remove(playerUUID);
            }, 100L); 
        });

        return true;
    }

    private Location getNetherToOverworldLoc(Location from) {
        ResourceWorld rw = rwManager.getResourceWorld(RWDimension.OVERWORLD);

        if (rw == null) {
            return null;
        }

        int x = from.getBlockX() * 8;
        int z = from.getBlockZ() * 8;
        int y = Math.min(from.getBlockY() * 2, 240);

        return new Location(rw.getWorld(), x, y, z);
    }

    private Location getOverworldToNetherLoc(Location from) {
        ResourceWorld rw = rwManager.getResourceWorld(RWDimension.NETHER);
            
        if (rw == null) {
            return null;
        }

        int x = from.getBlockX() / 8;
        int z = from.getBlockZ() / 8;
        int y = Math.min(from.getBlockY() / 2, 120);

        return new Location(rw.getWorld(), x, y, z);
    }

    private Location getOverworldToEndLoc(Location from, Location originalTo) {
        ResourceWorld rw = rwManager.getResourceWorld(RWDimension.END);

        if (rw == null) {
            return null;
        }

        return new Location(
            rw.getWorld(),
            originalTo.getX(),
            originalTo.getY(),
            originalTo.getZ()
        );
    }
}
