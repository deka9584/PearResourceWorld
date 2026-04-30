package pear.resourceworld.listeners;

import java.util.List;

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
        if (!signsHelper.isActionSignTitle(event.getLine(0))) {
            return;
        }

        Player player = event.getPlayer();

        if (!player.hasPermission(RWPermission.SIGNS_CREATE.get())) {
            event.setCancelled(true);
            player.sendMessage(plugin.getMessagesFileManager().getNoPermissionMessage());
            return;
        }

        String signAction = event.getLine(1);

        if (signAction == null) {
            return;
        }

        signAction = ChatColor.stripColor(signAction).toLowerCase();
        List<String> signLines = signsHelper.getSignLines(signAction);

        if (signLines == null) {
            event.setCancelled(true);
            
            player.sendMessage(
                plugin.getMessagesFileManager().getMessage("invalid-sign-action")
                    .replaceAll("%action%", signAction)
            );
            return;
        }

        for (int i = 0; i < event.getLines().length && i < signLines.size(); i++) {
            event.setLine(i, signLines.get(i));
        }

        BlockState state = event.getBlock().getState();
        
        if (state instanceof Sign) {
            signsHelper.setSignAction((Sign) state, signAction);
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
            String signAction = signsHelper.getSignAction((Sign) state);

            if (signAction == null) {
                return;
            }

            if (!player.hasPermission(RWPermission.SIGNS_USE.get())) {
                player.sendMessage(plugin.getMessagesFileManager().getNoPermissionMessage());
                return;
            }

            signsHelper.performAction(signAction, player);
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
