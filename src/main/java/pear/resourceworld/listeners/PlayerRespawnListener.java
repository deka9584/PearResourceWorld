package pear.resourceworld.listeners;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

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
}
