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

public class PortalListener implements Listener {
    private final PearResourceWorld plugin;

    public PortalListener(PearResourceWorld plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerPortal(PlayerPortalEvent event) {
        Location from = event.getFrom();

        if (event.isCancelled() || !plugin.getRwPortalHelper().isFromResourceWorld(from)) {
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

        if (!plugin.getRwPortalHelper().isPortalAllowed(portalType)) {
            event.setCancelled(true);
            return;
        }

        if (event.getTo() == null) {
            plugin.getLogger().info("getTo() returned null");
            return;
        }

        Location dest = plugin.getRwPortalHelper().getPortalDestination(from, portalType);

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

        if (event.isCancelled() || !plugin.getRwPortalHelper().isFromResourceWorld(from)) {
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

        if (!plugin.getRwPortalHelper().isPortalAllowed(portalType)) {
            event.setCancelled(true);
            return;
        }

        Location dest = plugin.getRwPortalHelper().getPortalDestination(from, portalType);

        if (dest == null) {
            plugin.debugLog("Entity portal location default: " + event.getEntityType().name());
            return;
        }

        event.setTo(dest);
        plugin.debugLog("Entity portal location on world: " + dest.getWorld().getName());
    }
}
