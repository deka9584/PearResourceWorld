package pear.resourceworld.listeners;

import java.util.List;

import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import net.md_5.bungee.api.ChatColor;
import pear.resourceworld.PearResourceWorld;
import pear.resourceworld.managers.SignsFileManager;

public class SignsListener implements Listener {
    private final PearResourceWorld plugin;
    private final SignsFileManager signsFm;
    private final NamespacedKey actionKey;

    public SignsListener(PearResourceWorld plugin) {
        this.plugin = plugin;
        this.signsFm = plugin.getSignsFileManager();
        this.actionKey = new NamespacedKey(plugin, "action");
    }

    @EventHandler(ignoreCancelled = true)
    public void onSignChange(SignChangeEvent event) {
        String firstLine = event.getLine(0);

        if (firstLine == null) {
            return;
        }

        firstLine = ChatColor.stripColor(firstLine);

        if (firstLine.equalsIgnoreCase(ChatColor.stripColor(signsFm.getTitle()))) {
            Player player = event.getPlayer();

            if (!player.hasPermission("pearresourceworld.signs.create")) {
                plugin.debugLog(player.getName() + " doesn't have permission to create signs");
                return;
            }

            String signAction = event.getLine(1);

            if (signAction == null) {
                plugin.debugLog("Sign action line is null");
                return;
            }

            signAction = ChatColor.stripColor(signAction).toLowerCase();

            switch (signAction) {
                case "tp":
                    setSignLines(event, signsFm.getTeleportSignLines());
                    break;
            
                default:
                    player.sendMessage(ChatColor.RED + "Inavlid action: " + signAction);
                    return;
            }

            BlockState state = event.getBlock().getState();
            
            if (state instanceof Sign) {
                Sign sign = (Sign) state;
                PersistentDataContainer pdc = sign.getPersistentDataContainer();
                pdc.set(actionKey, PersistentDataType.STRING, signAction);
                sign.update();
            }
        }
    }

    @EventHandler (ignoreCancelled = true)
    public void onRightClick(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Block block = event.getClickedBlock();
        Player player = event.getPlayer();

        if (block == null || player.isSneaking()) {
            return;
        }

        BlockState state = block.getState();

        if (state instanceof Sign) {
            Sign sign = (Sign) state;
            PersistentDataContainer pdc = sign.getPersistentDataContainer();

            if (!pdc.has(actionKey, PersistentDataType.STRING)) {
                return;
            }
            
            String action = pdc.get(actionKey, PersistentDataType.STRING);

            if (!player.hasPermission("pearresourceworld.signs.use")) {
                player.sendMessage(plugin.getMessagesFileManager().getMessage("no-permission"));
                return;
            }

            switch (action) {
                case "tp":
                    plugin.getTeleportHelper().signTeleport(player);
                    break;
            
                default:
                    plugin.logWarn("Invalid sign action: " + action);
                    break;
            }
        }
    }

    @EventHandler (ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        BlockState state = event.getBlock().getState();

        if (state instanceof Sign) {
            handleSignBreak(event, (Sign) state);
        }

        // TO DO: Get relative blocks and check if an attached block is a sign
    }

    public void handleSignBreak(BlockBreakEvent event, Sign sign) {
        PersistentDataContainer pdc = sign.getPersistentDataContainer();

        if (!pdc.has(actionKey, PersistentDataType.STRING)) {
            return;
        }

        Player player = event.getPlayer();

        if (player == null) {
            event.setCancelled(true);
            return;
        }

        if (!player.hasPermission("pearresourceworld.signs.break")) {
            player.sendMessage(plugin.getMessagesFileManager().getMessage("no-permission"));
            event.setCancelled(true);
        }
    }

    private void setSignLines(SignChangeEvent event, List<String> newLines) {
        event.setLine(0, signsFm.getTitle());

        for (int i = 0; i + 1 < event.getLines().length && i < newLines.size(); i++) {
            event.setLine(i + 1, newLines.get(i));
        }
    }
}
