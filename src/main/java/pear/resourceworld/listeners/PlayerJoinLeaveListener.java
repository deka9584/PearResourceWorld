package pear.resourceworld.listeners;

import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import pear.resourceworld.PearResourceWorld;
import pear.resourceworld.managers.ResourceWorldsManager;

public class PlayerJoinLeaveListener implements Listener {
    private final PearResourceWorld plugin;
    private final ResourceWorldsManager rwManager;

    public PlayerJoinLeaveListener(PearResourceWorld plugin) {
        this.plugin = plugin;
        this.rwManager = plugin.getResourceWorldsManager();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!rwManager.isResourceWorldReady() || rwManager.getRWSettings().getTeleportSpawnOnQuit()) {
            Player player = event.getPlayer();

            if (rwManager.isResourceWorld(player.getWorld())) {
                player.teleport(rwManager.getSpawnWorld().getSpawnLocation());
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();

        plugin.getTeleportManager().stopTeleportTasks(playerUUID);
        
        if (plugin.getCooldownManager().removeTpCooldown(playerUUID, false)) {
            plugin.debugLog("Removed expired teleport cooldown for player: " + player.getName());
        }

        if (rwManager.getRWSettings().getTeleportSpawnOnQuit() && rwManager.isResourceWorld(player.getWorld())) {
            if (player.teleport(rwManager.getSpawnWorld().getSpawnLocation())) {
                plugin.debugLog("Quit player teleported to spawn: " + player.getName());
            } else {
                plugin.logWarn("Unable to teleport player to spawn: " + player.getName());
            }
        }
    }
}
