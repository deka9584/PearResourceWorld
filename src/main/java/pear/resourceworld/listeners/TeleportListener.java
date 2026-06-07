package pear.resourceworld.listeners;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import pear.resourceworld.PearResourceWorld;
import pear.resourceworld.managers.ResourceWorldsManager;

public class TeleportListener implements Listener {
    private final PearResourceWorld plugin;
    private final ResourceWorldsManager rwManager;

    public TeleportListener(PearResourceWorld plugin) {
        this.plugin = plugin;
        this.rwManager = plugin.getResourceWorldsManager();
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Location to = event.getTo();

        if (to != null && !rwManager.isResourceWorldReady() && rwManager.isResourceWorld(to.getWorld())) {
            Player player = event.getPlayer();

            event.setCancelled(true);
            player.sendMessage(plugin.getMessagesFileManager().getMessage("reset-still-in-progress"));
            plugin.debugLog("Prevented teleport in to world under reset from player: " + player.getName());
        }
    }

    @EventHandler
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        plugin.getTeleportManager().stopTeleportTasks(event.getPlayer().getUniqueId());
    }
}
