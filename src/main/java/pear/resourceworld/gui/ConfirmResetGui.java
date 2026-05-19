package pear.resourceworld.gui;

import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import pear.resourceworld.PearResourceWorld;
import pear.resourceworld.model.GuiItem;
import pear.resourceworld.model.GuiType;
import pear.resourceworld.model.RWPermission;

public class ConfirmResetGui extends Gui {
    public ConfirmResetGui(PearResourceWorld plugin, ConfigurationSection guiConfigSect) {
        super(plugin, GuiType.CONFIRM_RESET);
        
        if (guiConfigSect == null) {
            plugin.logError("Gui configuration not found");
            return;
        }

        registerGuiItems(guiConfigSect, "confirm-item", "cancel-item");
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

        switch (guiItem.getId()) {
            case "confirm-item":
                if (!player.hasPermission(RWPermission.ADMIN_RESET.get())) {
                    player.sendMessage(getPlugin().getMessagesFileManager().getNoPermissionMessage());
                    playSound(player, Sound.BLOCK_ANVIL_HIT);
                    return;
                }

                getPlugin().getResourceWorldsManager().resetWorlds();
                playSound(player, Sound.BLOCK_ANVIL_USE);
                return;

            case "cancel-item":
                playSound(player, Sound.BLOCK_ANVIL_HIT);
                return;
        
            default:
                getPlugin().logWarn("No action for gui item: " + guiItem.getId());
        }
    }
}
