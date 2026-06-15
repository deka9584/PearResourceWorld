package pear.resourceworld.listeners;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.PortalType;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityPortalEnterEvent;
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.world.PortalCreateEvent;
import org.bukkit.inventory.ItemStack;

import pear.resourceworld.PearResourceWorld;
import pear.resourceworld.helpers.RWPortalHelper;
import pear.resourceworld.managers.ResourceWorldsManager;

public class PortalListener implements Listener {
    private final PearResourceWorld plugin;
    private final RWPortalHelper rwPortalHelper;
    private final ResourceWorldsManager rwManager;

    public PortalListener(PearResourceWorld plugin) {
        this.plugin = plugin;
        this.rwPortalHelper = plugin.getRwPortalHelper();
        this.rwManager = plugin.getResourceWorldsManager();
    }

    @EventHandler(ignoreCancelled = true)
    public void onPortalCreate(PortalCreateEvent event) {
        World world = event.getWorld();

        if (!rwManager.isResourceWorld(world)) {
            return;
        }

        PortalType portalType;

        switch (event.getReason()) {
            case FIRE:
                portalType = event.getBlocks()
                    .stream()
                    .anyMatch(b -> b.getType() == Material.NETHER_PORTAL)
                        ? PortalType.NETHER
                        : PortalType.CUSTOM;
                break;
            case NETHER_PAIR:
                portalType = PortalType.NETHER;
                break;
            case END_PLATFORM:
                portalType = PortalType.ENDER;
                break;
            default:
                portalType = PortalType.CUSTOM;
        }

        if (portalType == PortalType.CUSTOM) {
            plugin.debugLog("Created a custom portal type in world: " + world.getName());
            return;
        }

        if (!rwPortalHelper.isPortalAllowed(portalType)) {
            event.setCancelled(true);
            
            Entity entity = event.getEntity();

            if (entity instanceof Player) {
                entity.sendMessage(plugin.getMessagesFileManager().getMessage("portal-disabled"));
            }

            plugin.debugLog("Prevented portal creation on world: " + world.getName());
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerPortal(PlayerPortalEvent event) {
        Location from = event.getFrom();

        if (event.isCancelled() || !rwManager.isResourceWorld(from.getWorld())) {
            return;
        }

        PortalType portalType;

        switch (event.getCause()) {
            case END_PORTAL:
                portalType = PortalType.ENDER;
                break;
            case NETHER_PORTAL:
                portalType = PortalType.NETHER;
                break;
            default:
                portalType = PortalType.CUSTOM;
        }

        if (portalType == PortalType.CUSTOM) {
            plugin.debugLog("Player entered in a custom portal in world: " + from.getWorld().getName());
            return;
        }

        if (!rwPortalHelper.isPortalAllowed(portalType)) {
            event.setCancelled(true);
            return;
        }

        Location to = event.getTo();

        if (to == null) {
            plugin.getLogger().info("getTo() returned null");
            return;
        }

        Location dest = rwPortalHelper.getPortalDestination(from, portalType, to);

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
        World fromWorld = from.getWorld();

        if (event.isCancelled() || !rwManager.isResourceWorld(fromWorld)) {
            return;
        }

        Location to = event.getTo();
        
        if (to == null) {
            plugin.getLogger().info("getTo() returned null");
            return;
        }

        World toWorld = to.getWorld();
        PortalType portalType;

        if ((fromWorld.getEnvironment() == Environment.NETHER && toWorld.getEnvironment() == Environment.NORMAL) ||
            (fromWorld.getEnvironment() == Environment.NORMAL && toWorld.getEnvironment() == World.Environment.NETHER)) {
            portalType = PortalType.NETHER;
        } else if ((fromWorld.getEnvironment() == Environment.THE_END && toWorld.getEnvironment() == Environment.NORMAL) ||
            (fromWorld.getEnvironment() == Environment.NORMAL && toWorld.getEnvironment() == World.Environment.THE_END)) {
            portalType = PortalType.ENDER;
        } else {
            portalType = PortalType.CUSTOM;
        }

        if (portalType == PortalType.CUSTOM) {
            plugin.debugLog("Entity entered in a custom portal in world: " + fromWorld.getName());
            return;
        }

        if (!rwPortalHelper.isPortalAllowed(portalType)) {
            event.setCancelled(true);
            return;
        }

        Location dest = rwPortalHelper.getPortalDestination(from, portalType, to);

        if (dest == null) {
            plugin.debugLog("Entity portal location default: " + event.getEntityType().name());
            return;
        }

        event.setTo(dest);
        plugin.debugLog("Entity portal location on world: " + dest.getWorld().getName());
    }

    @EventHandler
    public void onPortalEnter(EntityPortalEnterEvent event) {
        if (event.getEntityType() != EntityType.PLAYER) {
            return;
        }

        Location loc = event.getLocation();
        
        if (loc.getBlock().getType() == Material.END_PORTAL) {
            World world = loc.getWorld();
        
            if (world.getEnvironment() == Environment.THE_END && rwManager.isResourceWorld(world)) {
                Entity entity = event.getEntity();

                if (rwManager.getRWSettings().getSkipEndCredits()) {
                    rwPortalHelper.skipEndCredits(entity.getUniqueId());
                }

                plugin.debugLog("End portal to overworld: " + entity.getName());
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (!rwManager.isResourceWorld(player.getWorld())) {
            return;
        }

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            ItemStack item = event.getItem();
            Block block = event.getClickedBlock();

            if (item != null && block != null) {
                boolean preventEndPortal = !rwPortalHelper.isPortalAllowed(PortalType.ENDER);
                Material itemType = item.getType();
                Material blockType = block.getType();

                if (preventEndPortal && itemType == Material.ENDER_EYE && blockType == Material.END_PORTAL_FRAME) {
                    event.setCancelled(true);
                    player.sendMessage(plugin.getMessagesFileManager().getMessage("portal-disabled"));
                    plugin.debugLog("Prevented placing eye on end portal frame from player: " + player.getName());
                }
            }
        }
    }
}
