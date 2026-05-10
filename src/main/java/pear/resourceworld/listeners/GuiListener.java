package pear.resourceworld.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;

import pear.resourceworld.PearResourceWorld;
import pear.resourceworld.gui.Gui;

public class GuiListener implements Listener {
    private final PearResourceWorld plugin;

    public GuiListener(PearResourceWorld plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory inv = event.getView().getTopInventory();

        if (inv.getHolder() instanceof Gui) {
            if (event.getWhoClicked() instanceof Player) {
                ((Gui) inv.getHolder()).onClick(event);
            } else {
                event.setCancelled(true);
                plugin.logWarn("Gui clicked by a non-player entity: " +  event.getView().getTitle());
            }
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        Inventory inv = event.getView().getTopInventory();

        if (inv.getHolder() instanceof Gui) {
            if (event.getWhoClicked() instanceof Player) {
                ((Gui) inv.getHolder()).onDrag(event);
            } else {
                event.setCancelled(true);
                plugin.logWarn("Gui item dragged by a non-player entity");
            }
        }
    }
}
