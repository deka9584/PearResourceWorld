package pear.resourceworld.gui;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import pear.resourceworld.PearResourceWorld;
import pear.resourceworld.model.GuiItem;
import pear.resourceworld.model.GuiType;
import pear.resourceworld.model.RWPermission;

public class ConfirmResetGui extends Gui {
    public ConfirmResetGui(PearResourceWorld plugin, ConfigurationSection guiConfig) {
        super(plugin, GuiType.CONFIRM_RESET);
        
        if (guiConfig == null) {
            plugin.logError("Gui configuration not found");
            return;
        }

        registerGuiItems(guiConfig, "confirm-item", "cancel-item");
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
            case "confirm-item":
                if (!player.hasPermission(RWPermission.ADMIN_RESET.get())) {
                    player.sendMessage(getPlugin().getMessagesFileManager().getNoPermissionMessage());
                    return;
                }

                getPlugin().getResourceWorldsManager().resetWorlds();
                return;

            case "cancel-item":
                return;
        
            default:
                getPlugin().logWarn("No action for gui item: " + guiItem.getId());
        }
    }
}
