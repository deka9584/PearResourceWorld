package pear.resourceworld.gui;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import pear.resourceworld.PearResourceWorld;
import pear.resourceworld.helpers.TeleportHelper;
import pear.resourceworld.managers.MessagesFileManager;
import pear.resourceworld.managers.ResourceWorldsManager;
import pear.resourceworld.model.GuiItem;
import pear.resourceworld.model.GuiType;
import pear.resourceworld.model.RWDimension;
import pear.resourceworld.model.RWPermission;

public class AdminGui extends Gui {
    private final MessagesFileManager messagesFm;
    private final ResourceWorldsManager rwManager;
    private final TeleportHelper teleportHelper;

    public AdminGui(PearResourceWorld plugin, ConfigurationSection guiConfig) {
        super(plugin, GuiType.ADMIN);

        this.messagesFm = plugin.getMessagesFileManager();
        this.rwManager = plugin.getResourceWorldsManager();
        this.teleportHelper = plugin.getTeleportHelper();
        
        if (guiConfig == null) {
            plugin.logError("Gui configuration not found");
            return;
        }

        registerGuiItems(guiConfig, "time-item", "reset-item", "tp-item", "kick-all-item");
        registerInventory(guiConfig, 9);
    }

    @Override
    public void onClick(InventoryClickEvent event) {
        GuiItem guiItem = getGuiItem(event.getCurrentItem());

        event.setCancelled(true);

        if (guiItem == null) {
            return;
        }

        Player player = (Player) event.getWhoClicked();

        player.closeInventory();

        switch (guiItem.getId()) {
            case "time-item":
                if (!player.hasPermission(RWPermission.ADMIN_TIME.get())) {
                    player.sendMessage(messagesFm.getNoPermissionMessage());
                    return;
                }

                player.sendMessage(getPlugin().getNextResetMessage());
                return;

            case "reset-item":
                if (!player.hasPermission(RWPermission.ADMIN_RESET.get())) {
                    player.sendMessage(messagesFm.getNoPermissionMessage());
                    return;
                }

                getPlugin().getGuiManager().openGui(GuiType.CONFIRM_RESET, player);
                return;

            case "tp-item":
                if (!player.hasPermission(RWPermission.ADMIN_TP.get())) {
                    player.sendMessage(messagesFm.getNoPermissionMessage());
                    return;
                }
        
                if (rwManager.isResourceWorld(player.getWorld())) {
                    teleportHelper.adminTeleport(player, player, null);
                } else {
                    teleportHelper.adminTeleport(player, player, RWDimension.OVERWORLD);
                }

                return;

            case "kick-all-item":
                if (!player.hasPermission(RWPermission.ADMIN_KICKALL.get())) {
                    player.sendMessage(messagesFm.getNoPermissionMessage());
                    return;
                }

                rwManager.kickAllFromResourceWorld();
                player.sendMessage(messagesFm.getMessage("kicked-all-players-from-resource-world"));
                return;

            default:
                getPlugin().logWarn("No action for gui item: " + guiItem.getId());
        }
    }
    
}
