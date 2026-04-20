package pear.resourceworld.listeners;

import org.bukkit.Location;
import org.bukkit.PortalType;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.event.player.PlayerPortalEvent;

import pear.resourceworld.PearResourceWorld;
import pear.resourceworld.managers.ResourceWorldsManager;
import pear.resourceworld.model.ResourceWorld;
import pear.resourceworld.model.ResourceWorldSettings;

public class PortalListener implements Listener {
    private final PearResourceWorld plugin;
    private final ResourceWorldsManager rwManager;

    public PortalListener(PearResourceWorld plugin) {
        this.plugin = plugin;
        this.rwManager = plugin.getResourceWorldsManager();
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerPortal(PlayerPortalEvent event) {
        Location from = event.getFrom();

        if (from == null || event.isCancelled() || !rwManager.isResourceWorld(from.getWorld())) {
            return;
        }

        PortalType portalType = null;

        switch (event.getCause()) {
            case END_PORTAL:
                portalType = PortalType.ENDER;
                break;
            case NETHER_PORTAL:
                portalType = PortalType.NETHER;
                break;
            default:
                return;
        }

        if (!isResourcePortalAllowed(portalType)) {
            event.setCancelled(true);
            return;
        }

        if (event.getTo() == null) {
            plugin.getLogger().info("getTo() returned null");
            return;
        }

        Location dest = getDestinationForResource(from, portalType);

        if (dest == null) {
            plugin.debugLog("Player portal location default");
            return;
        }

        event.setTo(dest);
        plugin.debugLog("Player portal location on world: " + dest.getWorld().getName());
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityPortal(EntityPortalEvent event) {
        Location from = event.getFrom();

        if (from == null || event.isCancelled() || !rwManager.isResourceWorld(from.getWorld())) {
            return;
        }
        
        if (event.getTo() == null) {
            plugin.getLogger().info("getTo() returned null");
            return;
        }

        World fromWorld = from.getWorld();
        World toWorld = event.getTo().getWorld();
        PortalType portalType = null;

        if ((fromWorld.getEnvironment() == Environment.NETHER && toWorld.getEnvironment() == Environment.NORMAL) ||
            (fromWorld.getEnvironment() == Environment.NORMAL && toWorld.getEnvironment() == World.Environment.NETHER)) {
            portalType = PortalType.NETHER;
        } else if ((fromWorld.getEnvironment() == Environment.THE_END && toWorld.getEnvironment() == Environment.NORMAL) ||
            (fromWorld.getEnvironment() == Environment.NORMAL && toWorld.getEnvironment() == World.Environment.THE_END)) {
            portalType = PortalType.ENDER;
        }

        if (portalType == null) {
            return;
        }

        if (!isResourcePortalAllowed(portalType)) {
            event.setCancelled(true);
            return;
        }

        Location dest = getDestinationForResource(from, portalType);

        if (dest == null) {
            plugin.debugLog("Entity portal location default");
            return;
        }

        event.setTo(dest);
        plugin.debugLog("Entity portal location on world: " + dest.getWorld().getName());
    }

    private Location getDestinationForResource(Location from, PortalType portalType) {
        Environment fromEnv = from.getWorld().getEnvironment();

        // Overworld -> nether
        if (fromEnv == Environment.NORMAL && portalType == PortalType.NETHER) {
            ResourceWorld rw = rwManager.getResourceWorld("nether");
            if (rw == null || rw.getWorld() == null) return null;

            World dstWorld = rw.getWorld();
            int x = (int) from.getX() / 8;
            int z = (int) from.getZ() / 8;
            int y = Math.min(dstWorld.getHighestBlockYAt(x, z), 120);

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
            int y = dstWorld.getHighestBlockYAt(x, z);

            return new Location(dstWorld, x, y, z);
        }

        return null;
    }

    private boolean isResourcePortalAllowed(PortalType portalType) {
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
}
