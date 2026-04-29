package pear.resourceworld.listeners;

import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import net.md_5.bungee.api.ChatColor;
import pear.resourceworld.PearResourceWorld;
import pear.resourceworld.helpers.SignsHelper;
import pear.resourceworld.model.RWPermission;

public class SignsListener implements Listener {
    private final PearResourceWorld plugin;
    private final SignsHelper signsHelper;

    public SignsListener(PearResourceWorld plugin) {
        this.plugin = plugin;
        this.signsHelper = plugin.getSignsHelper();
    }

    @EventHandler(ignoreCancelled = true)
    public void onSignChange(SignChangeEvent event) {
        if (signsHelper.isResourceWorldSignTitle(event.getLine(0))) {
            Player player = event.getPlayer();

            if (!player.hasPermission(RWPermission.SIGNS_CREATE.get())) {
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
                    signsHelper.setTeleportSignLines(event);
                    break;
            
                default:
                    player.sendMessage(ChatColor.RED + "Inavlid action: " + signAction);
                    return;
            }

            BlockState state = event.getBlock().getState();
            
            if (state instanceof Sign) {
                signsHelper.setSignAction((Sign) state, signAction);
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
            String action = signsHelper.getSignAction((Sign) state);

            if (action == null) {
                return;
            }

            if (!player.hasPermission(RWPermission.SINGS_USE.get())) {
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

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        
        if (signsHelper.isProtectedBlock(block)) {
            Player player = event.getPlayer();

            if (player == null) {
                event.setCancelled(true);
            }

            if (!player.hasPermission(RWPermission.SIGNS_BREAK.get())) {
                player.sendMessage(plugin.getMessagesFileManager().getMessage("no-permission"));
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onBlockExplode(BlockExplodeEvent event) {
        event.blockList().removeIf(block -> signsHelper.isProtectedBlock(block));
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        event.blockList().removeIf(block -> signsHelper.isProtectedBlock(block));
    }

    @EventHandler
    public void onBlockBurn(BlockBurnEvent event) {
        if (signsHelper.isProtectedBlock(event.getBlock())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPistonExtend(BlockPistonExtendEvent event) {
        if (signsHelper.anyProtected(event.getBlocks())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPistonRetract(BlockPistonRetractEvent event) {
        if (event.isSticky() && signsHelper.anyProtected(event.getBlocks())) {
            event.setCancelled(true);
        }
    }
}
