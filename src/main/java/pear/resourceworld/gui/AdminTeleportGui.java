package pear.resourceworld.gui;

import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.md_5.bungee.api.ChatColor;
import pear.resourceworld.PearResourceWorld;
import pear.resourceworld.helpers.TeleportHelper;
import pear.resourceworld.managers.MessagesFileManager;
import pear.resourceworld.managers.ResourceWorldsManager;
import pear.resourceworld.model.GuiItem;
import pear.resourceworld.model.GuiType;
import pear.resourceworld.model.RWDimension;
import pear.resourceworld.model.RWPermission;

public class AdminTeleportGui extends Gui {
    private final MessagesFileManager messagesFm;
    private final ResourceWorldsManager rwManager;
    private final TeleportHelper teleportHelper;

    public AdminTeleportGui(PearResourceWorld plugin, ConfigurationSection guiConfig) {
        super(plugin, GuiType.ADMIN_TELEPORT);

        this.messagesFm = plugin.getMessagesFileManager();
        this.rwManager = plugin.getResourceWorldsManager();
        this.teleportHelper = plugin.getTeleportHelper();
        
        if (guiConfig == null) {
            plugin.logError("Gui configuration not found");
            return;
        }

        registerGuiItems(guiConfig, "overworld-item", "nether-item", "end-item", "spawn-item");

        getGuiItems().forEach(gi -> {
            updateItem(gi);
        });

        registerInventory(guiConfig, 9);
    }

    @Override
    public void onClick(InventoryClickEvent event) {
        GuiItem guiItem = getGuiItem(event.getCurrentItem());

        event.setCancelled(true);

        if (guiItem == null || guiItem.isDisabled()) {
            return;
        }

        Player player = (Player) event.getWhoClicked();

        player.closeInventory();

        if (!player.hasPermission(RWPermission.ADMIN_TP.get())) {
            player.sendMessage(messagesFm.getNoPermissionMessage());
            return;
        }

        switch (guiItem.getId()) {
            case "overworld-item":
                teleportHelper.adminTeleport(player, player, RWDimension.OVERWORLD);
                return;

            case "nether-item":
                teleportHelper.adminTeleport(player, player, RWDimension.NETHER);
                return;
            
            case "end-item":
                teleportHelper.adminTeleport(player, player, RWDimension.END);
                return;

            case "spawn-item":
                teleportHelper.adminTeleportSpawn(player, player);
                return;

            default:
                getPlugin().logWarn("No action for gui item: " + guiItem.getId());
        }
    }

    private void updateItem(GuiItem gi) {
        ItemStack item = gi.getItem();
        ItemMeta meta = item.getItemMeta();
        RWDimension dim;

        switch (gi.getId()) {
            case "overworld-item":
                dim = RWDimension.OVERWORLD;
                break;

            case "nether-item":
                dim = RWDimension.NETHER;
                break;
            
            case "end-item":
                dim = RWDimension.END;
                break;
        
            default:
                return;
        }

        boolean isEnabled = rwManager.getEnabledDimensions().contains(dim);

        List<String> lore = meta.getLore().stream()
            .map(s -> s.replaceAll("%status%", isEnabled ? ChatColor.GREEN + "ON" : ChatColor.RED + "OFF"))
            .collect(Collectors.toList());

        meta.setLore(lore);
        item.setItemMeta(meta);
        gi.setDisabled(!isEnabled);
    }
}
