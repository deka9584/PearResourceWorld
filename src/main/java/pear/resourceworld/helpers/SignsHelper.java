package pear.resourceworld.helpers;

import java.util.List;

import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import net.md_5.bungee.api.ChatColor;
import pear.resourceworld.PearResourceWorld;
import pear.resourceworld.managers.SignsFileManager;
import pear.resourceworld.utils.WorldUtils;

public class SignsHelper {
    private final PearResourceWorld plugin;
    private final SignsFileManager signsFm;
    private final NamespacedKey actionKey;

    public SignsHelper(PearResourceWorld plugin) {
        this.plugin = plugin;
        this.signsFm = plugin.getSignsFileManager();
        this.actionKey = new NamespacedKey(plugin, "action");
    }

    public boolean anyProtected(List<Block> blocks) {
        for (Block block : blocks) {
            if (isProtectedBlock(block)) {
                return true;
            }
        }
        
        return false;
    }

    public String getSignAction(Sign sign) {
        return sign.getPersistentDataContainer().get(actionKey, PersistentDataType.STRING);
    }

    public List<String> getSignLines(String actionValue) {
        List<String> lines = getActionSignLines(actionValue);

        if (lines == null) {
            return null;
        }

        lines.add(0, signsFm.getTitle());
        return lines;
    }

    public boolean isActionSignTitle(String signLine) {
        return signLine != null && ChatColor.stripColor(signLine)
            .equalsIgnoreCase(ChatColor.stripColor(signsFm.getTitle()));
    }

    public boolean isActionSign(Sign sign) {
        return sign.getPersistentDataContainer().has(actionKey, PersistentDataType.STRING);
    }

    public boolean isProtectedBlock(Block block) {
        if (!signsFm.isBreakPrevented()) {
            return false;
        }

        BlockState state = block.getState();

        if (state instanceof Sign && isActionSign((Sign) state)) {
            return true;
        }

        List<Sign> signs = WorldUtils.getAttachedSigns(block);

        for (Sign sign : signs) {
            if (isActionSign(sign)) {
                return true;
            }
        }

        return false;
    }

    public void performAction(String actionValue, Player player) {
        switch (actionValue) {
            case "tp":
                plugin.getTeleportHelper().signTeleport(player);
                break;
                
            default:
                player.sendMessage(
                    plugin.getMessagesFileManager().getMessage("invalid-sign-action")
                        .replaceAll("%action%", actionValue)
                );
                break;
        }
    }

    public void setSignAction(Sign sign, String actionValue) {
        PersistentDataContainer pdc = sign.getPersistentDataContainer();
        pdc.set(actionKey, PersistentDataType.STRING, actionValue);
        sign.update();
    }

    private List<String> getActionSignLines(String actionValue) {
        switch (actionValue) {
            case "tp":
                return signsFm.getTeleportSignLines();

            default:
                return null;
        }
    }
}