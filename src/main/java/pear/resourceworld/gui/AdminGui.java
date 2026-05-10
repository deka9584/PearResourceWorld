package pear.resourceworld.gui;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import pear.resourceworld.PearResourceWorld;
import pear.resourceworld.managers.MessagesFileManager;
import pear.resourceworld.model.GuiItem;
import pear.resourceworld.model.GuiType;
import pear.resourceworld.model.RWPermission;

public class AdminGui extends Gui {
    private final MessagesFileManager messagesFm;

    public AdminGui(PearResourceWorld plugin, ConfigurationSection guiConfig) {
        super(plugin, GuiType.ADMIN);

        this.messagesFm = plugin.getMessagesFileManager();
        
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

        HumanEntity entity = event.getWhoClicked();

        entity.closeInventory();

        switch (guiItem.getId()) {
            case "time-item":
                if (!entity.hasPermission(RWPermission.ADMIN_TIME.get())) {
                    entity.sendMessage(messagesFm.getNoPermissionMessage());
                    return;
                }

                entity.sendMessage(getPlugin().getNextResetMessage());
                return;

            case "reset-item":
                if (!entity.hasPermission(RWPermission.ADMIN_RESET.get())) {
                    entity.sendMessage(messagesFm.getNoPermissionMessage());
                    return;
                }

                getPlugin().getGuiManager().openGui(GuiType.CONFIRM_RESET, entity);
                return;

            case "tp-item":
                if (!entity.hasPermission(RWPermission.ADMIN_TP.get())) {
                    entity.sendMessage(messagesFm.getNoPermissionMessage());
                    return;
                }
        
                getPlugin().getTeleportHelper().teleportToRwOverworld((Player) entity, false);
                return;

            case "kick-all-item":
                if (!entity.hasPermission(RWPermission.ADMIN_KICKALL.get())) {
                    entity.sendMessage(messagesFm.getNoPermissionMessage());
                    return;
                }

                getPlugin().getResourceWorldsManager().kickAllFromResourceWorld();
                entity.sendMessage(messagesFm.getMessage("kicked-all-players-from-resource-world"));
                return;

            default:
                getPlugin().logWarn("No action for gui item: " + guiItem.getId());
        }
    }
    
}
