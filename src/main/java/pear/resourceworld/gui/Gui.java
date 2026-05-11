package pear.resourceworld.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import pear.resourceworld.PearResourceWorld;
import pear.resourceworld.model.GuiItem;
import pear.resourceworld.model.GuiType;
import pear.resourceworld.utils.Utils;

public abstract class Gui implements InventoryHolder {
    private final PearResourceWorld plugin;
    private final GuiType type;
    private final List<GuiItem> guiItems = new ArrayList<>();
    
    private Inventory inventory;

    public Gui(PearResourceWorld plugin, GuiType type) {
        this.plugin = plugin;
        this.type = type;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public GuiItem getGuiItem(ItemStack item) {
        if (item != null && !item.getType().isAir()) {
            for (GuiItem gi : guiItems) {
                if (gi.isSimilar(item)) {
                    return gi;
                }
            }
        }

        return null;
    }

    public List<GuiItem> getGuiItems() {
        return guiItems;
    }

    protected PearResourceWorld getPlugin() {
        return plugin;
    }

    public GuiType getType() {
        return type;
    }

    public abstract void onClick(InventoryClickEvent event);

    public void onDrag(InventoryDragEvent event) {
        event.setCancelled(true);
    }

    public InventoryView openInvetory(Player player) {
        return inventory != null ? player.openInventory(inventory) : null;
    }

    protected void registerGuiItems(List<GuiItem> items) {
        guiItems.clear();
        guiItems.addAll(items);
    }

    protected void registerGuiItems(ConfigurationSection guiConfig, String... itemIDs) {
        guiItems.clear();

        for (String id : itemIDs) {
            ConfigurationSection itemSect = guiConfig.getConfigurationSection(id);

            if (itemSect == null) {
                plugin.logError("No config section found item: " + id);
                continue;
            }

            String materialName = itemSect.getString("material", "");
            Material material = Material.matchMaterial(materialName);
    
            if (material == null) {
                plugin.logError("Invalid material: " + materialName);
                material = Material.STONE;
            }

            String displayName = Utils.translateColorCodes(itemSect.getString("name"));
            
            List<String> lore = itemSect.getStringList("lore").stream()
                .map(s -> Utils.translateColorCodes(s))
                .collect(Collectors.toList());

            int position = itemSect.getInt("position");
    
            guiItems.add(new GuiItem(id, material, displayName, lore, position));
        }
    }

    protected void registerInventory(ConfigurationSection guiConfig, int size) {
        registerInventory(Utils.translateColorCodes(guiConfig.getString("name", "")), size);
    }

    protected void registerInventory(String displayName, int size) {
        inventory = plugin.getServer().createInventory(this, size, displayName);

        guiItems.forEach(gi -> {
            if (gi.isDisplayable()) {
                inventory.setItem(gi.getPosition(), gi.getItem());
            }
        });
    }

    protected InventoryView switchGui(GuiType guiType, Player player) {
        return plugin.getGuiManager().openGui(guiType, player);
    }
}
