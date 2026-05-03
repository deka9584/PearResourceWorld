package pear.resourceworld.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import pear.resourceworld.PearResourceWorld;
import pear.resourceworld.managers.CommandBlacklistManager;

public class PlayerCommandListener implements Listener {
    private final PearResourceWorld plugin;
    private final CommandBlacklistManager cblManager;

    public PlayerCommandListener(PearResourceWorld plugin) {
        this.plugin = plugin;
        this.cblManager = plugin.getCommandBlacklistManager();
    }

    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        if (!cblManager.isEnabled()) {
            return;
        }

        Player player = event.getPlayer();

        if (!plugin.getResourceWorldsManager().isResourceWorld(player.getWorld())) {
            return;
        }

        if (cblManager.isBlacklisted(event.getMessage().trim())) {
            event.setCancelled(true);
            player.sendMessage(plugin.getMessagesFileManager().getMessage("command-blacklisted"));
        }
    }
}
