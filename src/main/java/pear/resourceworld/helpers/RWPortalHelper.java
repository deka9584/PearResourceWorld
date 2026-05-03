package pear.resourceworld.helpers;

import org.bukkit.Location;
import org.bukkit.PortalType;
import org.bukkit.World;
import org.bukkit.World.Environment;

import pear.resourceworld.PearResourceWorld;
import pear.resourceworld.managers.ResourceWorldsManager;
import pear.resourceworld.model.RWDimension;
import pear.resourceworld.model.ResourceWorld;
import pear.resourceworld.model.ResourceWorldSettings;
import pear.resourceworld.utils.NMSWorldUtils;
import pear.resourceworld.utils.WorldUtils;

public class RWPortalHelper {
    private final PearResourceWorld plugin;
    private final ResourceWorldsManager rwManager;

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

    public boolean isDragonBattleDisabled() {
        ResourceWorldSettings rwSettings = rwManager.getRWSettings();
        return rwSettings != null && rwSettings.getDisableDragonBattle();
    }

    public boolean isFromResourceWorld(Location from) {
        return from != null && rwManager.isResourceWorld(from.getWorld());
    }

    public boolean isPortalAllowed(PortalType portalType) {
        ResourceWorldSettings rwSettings = rwManager.getRWSettings();

        if (rwSettings == null) {
            plugin.logWarn("Resource world settings not initalized");
            return false;
        }

        switch (portalType) {
            case NETHER:
                return rwSettings.getAllowNetherPortals();
            case ENDER:
                return rwSettings.getAllowEndPortals();
            default:
                return false;
        }
    }

    public Location getPortalDestination(Location from, PortalType portalType, Location originalTo) {
        Environment fromEnv = from.getWorld().getEnvironment();

        // Overworld -> nether
        if (fromEnv == Environment.NORMAL && portalType == PortalType.NETHER) {
            World dstWorld = getRwWorld(RWDimension.NETHER);
            
            if (dstWorld == null) {
                return null;
            }

            int x = from.getBlockX() / 8;
            int z = from.getBlockZ() / 8;
            int y = Math.min(from.getBlockY() / 2, 120);

            return new Location(dstWorld, x, y, z);
        }

        // Overworld -> end
        if (fromEnv == Environment.NORMAL && portalType == PortalType.ENDER) {
            World dstWorld = getRwWorld(RWDimension.END);

            return dstWorld != null && originalTo != null
                ? new Location(dstWorld, originalTo.getX(), originalTo.getY(), originalTo.getZ())
                : null;
        }

        // Nether -> overworld
        if (fromEnv == Environment.NETHER && portalType == PortalType.NETHER) {
            World dstWorld = getRwWorld(RWDimension.OVERWORLD);

            if (dstWorld == null) {
                return null;
            }
            
            int x = from.getBlockX() * 8;
            int z = from.getBlockZ() * 8;
            int y = Math.min(from.getBlockY() * 2, 240);

            return new Location(dstWorld, x, y, z);
        }

        return null;
    }

    private World getRwWorld(RWDimension dim) {
        ResourceWorld rw = rwManager.getResourceWorld(dim);
        return rw != null ? rw.getWorld() : null;
    }
}
