package pear.resourceworld.model;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class GuiItem {
    private final String id;
    private final ItemStack item;
    private final int position;
    private boolean disabled;

    public GuiItem(String id, ItemStack item, int position) {
        this.id = id;
        this.item = item;
        this.position = position;
    }

    public GuiItem(String id, Material material, String name, List<String> lore, int position) {
        ItemStack item = new ItemStack(material, 1);
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(name);
        meta.setLore(lore);

        item.setItemMeta(meta);
        
        this.id = id;
        this.item = item;
        this.position = position;
    }
    
    public String getId() {
        return id;
    }

    public ItemStack getItem() {
        return item;
    }

    public int getPosition() {
        return position;
    }

    public boolean isDisabled() {
        return disabled;
    }
    
    public boolean isDisplayable() {
        return position >= 0;
    }

    public boolean isSimilar(ItemStack itemToCompare) {
        return item.isSimilar(itemToCompare);
    }

    public void setDisabled(boolean state) {
        disabled = state;
    }
}
