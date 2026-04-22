package pear.resourceworld.helpers;

import org.bukkit.Location;
import org.bukkit.PortalType;
import org.bukkit.World;
import org.bukkit.World.Environment;

import pear.resourceworld.PearResourceWorld;
import pear.resourceworld.managers.ResourceWorldsManager;
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

    public boolean createEndSpawnPortal(World endWorld) {
        plugin.getLogger().info("Creating end spawn portal");

        if (WorldUtils.generateEndSpawnPortal(endWorld)) {
            plugin.getLogger().info("Created end spawn portal");
            return true;
        }

        if (NMSWorldUtils.generateEndSpawnPortal(endWorld)) {
            plugin.getLogger().info("Created end spawn portal using NMS");
            return true;
        }

        plugin.logWarn("End spawn portal creation failed");
        return false;
    }

    public boolean isDragonBattleDisabled() {
        ResourceWorldSettings rwSettings = rwManager.getResourceWorldSettings();
        return rwSettings != null && rwSettings.getDisableDragonBattle();
    }

    public boolean isFromResourceWorld(Location from) {
        return from != null && rwManager.isResourceWorld(from.getWorld());
    }

    public boolean isPortalAllowed(PortalType portalType) {
        ResourceWorldSettings rwSettings = rwManager.getResourceWorldSettings();

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

    public Location getPortalDestination(Location from, PortalType portalType) {
        Environment fromEnv = from.getWorld().getEnvironment();

        // Overworld -> nether
        if (fromEnv == Environment.NORMAL && portalType == PortalType.NETHER) {
            ResourceWorld rw = rwManager.getResourceWorld("nether");
            if (rw == null || rw.getWorld() == null) return null;

            World dstWorld = rw.getWorld();
            int x = (int) from.getX() / 8;
            int z = (int) from.getZ() / 8;
            int y = Math.min((int) from.getY() / 2, 120);

            return new Location(dstWorld, x, y, z);
        }

        // Overworld -> end
        if (fromEnv == Environment.NORMAL && portalType == PortalType.ENDER) {
            ResourceWorld rw = rwManager.getResourceWorld("end");
            if (rw == null || rw.getWorld() == null) return null;
            
            return rw.getWorld().getSpawnLocation();
        }

        // Nether -> overworld
        if (fromEnv == Environment.NETHER && portalType == PortalType.NETHER) {
            ResourceWorld rw = rwManager.getResourceWorld("overworld");
            if (rw == null || rw.getWorld() == null) return null;
            
            World dstWorld = rw.getWorld();
            int x = (int) from.getX() * 8;
            int z = (int) from.getZ() * 8;
            int y = (int) from.getY() * 2;

            return new Location(dstWorld, x, y, z);
        }

        return null;
    }
}
