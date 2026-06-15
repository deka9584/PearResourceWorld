package pear.resourceworld.listeners;

import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerBedLeaveEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.World.Environment;

import pear.resourceworld.PearResourceWorld;
import pear.resourceworld.managers.ResourceWorldsManager;

public class PlayerRespawnListener implements Listener {
    private final PearResourceWorld plugin;
    private final ResourceWorldsManager rwManager;

    public PlayerRespawnListener(PearResourceWorld plugin) {
        this.plugin = plugin;
        this.rwManager = plugin.getResourceWorldsManager();
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        if (!rwManager.isResourceWorldReady() || rwManager.getRWSettings().getDisableSetRespawn()) {
            Location respawnLoc = event.getRespawnLocation();

            if (respawnLoc != null && rwManager.isResourceWorld(respawnLoc.getWorld())) {
                event.setRespawnLocation(rwManager.getSpawnWorld().getSpawnLocation());
                plugin.debugLog("Prevented respawn in resource world: " + event.getPlayer().getName());
            }
            return;
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();

        plugin.getTeleportManager().stopTeleportTasks(player.getUniqueId());

        if (!rwManager.isResourceWorldReady() || rwManager.getRWSettings().getDisableSetRespawn()) {
            Location bedLoc = player.getBedSpawnLocation();

            if (bedLoc != null && rwManager.isResourceWorld(bedLoc.getWorld())) {
                player.setBedSpawnLocation(null);
                plugin.debugLog("Removed bed spawn location in resource world: " + player.getName());
            }
        }
    }

    @EventHandler
    public void onBedLeave(PlayerBedLeaveEvent event) {
        Player player = event.getPlayer();
        World world = player.getWorld();

        if (rwManager.getRWSettings().getDisableSetRespawn() && rwManager.isResourceWorld(world)) {
            event.setSpawnLocation(false);
            plugin.debugLog("Prevented setting new spawn location for player: " + player.getName());
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Player player = event.getPlayer();
        World world = player.getWorld();
        Block block = event.getClickedBlock();

        if (block != null && rwManager.isResourceWorld(world)) {
            Environment worldEnv = world.getEnvironment();
            Material blockType = block.getType();

            if (worldEnv == Environment.NORMAL && Tag.BEDS.isTagged(blockType)) {
                restoreSpawnLocation(player.getUniqueId(), player.getBedSpawnLocation());
            }
            
            if (worldEnv == Environment.NETHER && blockType == Material.matchMaterial("RESPAWN_ANCHOR")) {
                event.setCancelled(true);
                player.sendMessage(plugin.getMessagesFileManager().getMessage("unable-to-set-respawn"));
                plugin.debugLog("Prevented placing respawn anchor for player: " + player.getName());
            }
        }
    }

    private void restoreSpawnLocation(UUID playerUUID, Location oldSpawn) {
        Location spawnLocation = oldSpawn != null ? oldSpawn.clone() : null;

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            Player player = plugin.getServer().getPlayer(playerUUID);

            if (player == null) {
                return;
            }

            player.setBedSpawnLocation(spawnLocation, true);
            player.sendMessage(plugin.getMessagesFileManager().getMessage("unable-to-set-respawn"));
            plugin.debugLog("Restored old spawn location for player: " + player.getName());
        }, 2L);
    }
}
