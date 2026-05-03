package pear.resourceworld.listeners;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerBedLeaveEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;

import pear.resourceworld.PearResourceWorld;
import pear.resourceworld.managers.ResourceWorldsManager;
import pear.resourceworld.utils.WorldUtils;

public class PlayerRespawnListener implements Listener {
    private final PearResourceWorld plugin;
    private final ResourceWorldsManager rwManager;

    public PlayerRespawnListener(PearResourceWorld plugin) {
        this.plugin = plugin;
        this.rwManager = plugin.getResourceWorldsManager();
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Location respawnLoc = event.getRespawnLocation();

        if (respawnLoc == null || !rwManager.isResourceWorld(respawnLoc.getWorld())) {
            return;
        }

        Player player = event.getPlayer();

        if (rwManager.getResourceWorldSettings().getDisableSetRespawn()) {
            event.setRespawnLocation(rwManager.getSpawnWorld().getSpawnLocation());
            plugin.debugLog("Prevented respawn in to resource world from player: " + player.getName());
            return;
        }

        if (!rwManager.isResourceWorldReady()) {
            event.setRespawnLocation(rwManager.getSpawnWorld().getSpawnLocation());
            player.sendMessage(plugin.getMessagesFileManager().getMessage("reset-still-in-progress"));
            plugin.debugLog("Prevented respawn in to world under reset from player: " + player.getName());
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        Location bedLoc = player.getBedSpawnLocation();

        plugin.getTeleportManager().stopTeleportTasks(player.getUniqueId());

        if (bedLoc == null || !rwManager.isResourceWorld(bedLoc.getWorld())) {
            return;
        }

        if (!rwManager.isResourceWorldReady() || rwManager.getResourceWorldSettings().getDisableSetRespawn()) {
            player.setBedSpawnLocation(null);
            plugin.debugLog("Removed bed spawn location in resource world for player: " + player.getName());
        }
    }

    @EventHandler
    public void onBedLeave(PlayerBedLeaveEvent event) {
        Player player = event.getPlayer();

        if (rwManager.isResourceWorld(player.getWorld()) && rwManager.getResourceWorldSettings().getDisableSetRespawn()) {
            event.setSpawnLocation(false);
            player.sendMessage(plugin.getMessagesFileManager().getMessage("unable-to-set-respawn"));
            plugin.debugLog("Prevented setting new spawn location for player: " + player.getName());
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        World world = player.getWorld();

        if (!rwManager.isResourceWorld(world)) {
            return;
        }

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            ItemStack item = event.getItem();
            Block block = event.getClickedBlock();

            if (item == null || block == null) {
                return;
            }

            if (item.getType().name().equals("RESPAWN_ANCHOR")) {
                event.setCancelled(true);
                plugin.debugLog("Prevented placing respawn anchor for player: " + player.getName());
                return;
            }

            if (block.getType().name().equals("RESPAWN_ANCHOR")) {
                event.setCancelled(true);
                plugin.debugLog("Prevented interacting with respawn anchor for player: " + player.getName());
                player.sendMessage(plugin.getMessagesFileManager().getMessage("unable-to-set-respawn"));
                return;
            }

            if (item.getType() == Material.END_CRYSTAL) {
                boolean preventCrystalPlace = (
                    world.getEnvironment() == Environment.THE_END &&
                    rwManager.getResourceWorldSettings().getPreventDragonRespawn() &&
                    block.getType() == Material.BEDROCK &&
                    WorldUtils.hasRelativeBlockType(block, Material.BEDROCK)
                );
    
                if (preventCrystalPlace) {
                    event.setCancelled(true);
                    player.sendMessage(plugin.getMessagesFileManager().getMessage("dragon-respawn-disabled"));
                    plugin.debugLog("Prevented placing crystal on bedrock from player: " + player.getName());
                }
            }
        }
    }
}
