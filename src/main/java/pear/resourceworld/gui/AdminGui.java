package pear.resourceworld.gui;

import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import pear.resourceworld.PearResourceWorld;
import pear.resourceworld.managers.MessagesFileManager;
import pear.resourceworld.managers.ResourceWorldsManager;
import pear.resourceworld.model.GuiItem;
import pear.resourceworld.model.GuiType;
import pear.resourceworld.model.RWPermission;

public class AdminGui extends Gui {
    private final MessagesFileManager messagesFm;
    private final ResourceWorldsManager rwManager;

    public AdminGui(PearResourceWorld plugin, ConfigurationSection guiConfigSect) {
        super(plugin, GuiType.ADMIN);

        this.messagesFm = plugin.getMessagesFileManager();
        this.rwManager = plugin.getResourceWorldsManager();
        
        if (guiConfigSect == null) {
            plugin.logError("Gui configuration not found");
            return;
        }

        registerGuiItems(guiConfigSect, "time-item", "reset-item", "tp-item", "kick-all-item");
        registerInventory(guiConfigSect, 9);
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
        playSound(player, Sound.UI_BUTTON_CLICK);

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

                switchGui(GuiType.CONFIRM_RESET, player);
                return;

            case "tp-item":
                if (!player.hasPermission(RWPermission.ADMIN_TP.get())) {
                    player.sendMessage(messagesFm.getNoPermissionMessage());
                    return;
                }
        
                switchGui(GuiType.ADMIN_TELEPORT, player);
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
