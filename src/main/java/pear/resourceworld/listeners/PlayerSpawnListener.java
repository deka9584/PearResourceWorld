package pear.resourceworld.listeners;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;

import pear.resourceworld.PearResourceWorld;
import pear.resourceworld.managers.ResourceWorldsManager;

public class PlayerSpawnListener implements Listener {
    private final PearResourceWorld plugin;
    private final ResourceWorldsManager rwManager;

    public PlayerSpawnListener(PearResourceWorld plugin) {
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

        if (bedLoc == null || !rwManager.isResourceWorld(bedLoc.getWorld())) {
            return;
        }

        if (!rwManager.isResourceWorldReady() || rwManager.getResourceWorldSettings().getDisableSetRespawn()) {
            player.setBedSpawnLocation(null);
            plugin.debugLog("Removed bed spawn location in resource world for player: " + player.getName());
        }
    }

    @EventHandler
    public void onPlayerSpawnLocation(PlayerSpawnLocationEvent event) {
        Location loc = event.getSpawnLocation();

        if (loc == null || !rwManager.isResourceWorld(loc.getWorld())) {
            return;
        }

        if (!rwManager.isResourceWorldReady() || rwManager.getResourceWorldSettings().getTeleportSpawnOnQuit()) {
            event.setSpawnLocation(rwManager.getSpawnWorld().getSpawnLocation());
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        Location loc = player.getLocation();

        if (loc == null || !rwManager.isResourceWorld(loc.getWorld())) {
            return;
        }

        if (rwManager.getResourceWorldSettings().getTeleportSpawnOnQuit()) {
            if (player.teleport(rwManager.getSpawnWorld().getSpawnLocation())) {
                plugin.debugLog("Quit player teleported to spawn: " + player.getName());
            } else {
                plugin.logWarn("Unable to teleport player to spawn: " + player.getName());
            }
        }
    }
}
