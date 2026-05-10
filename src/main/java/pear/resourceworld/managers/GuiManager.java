package pear.resourceworld.managers;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.InventoryView;

import pear.resourceworld.PearResourceWorld;
import pear.resourceworld.gui.AdminGui;
import pear.resourceworld.gui.ConfirmResetGui;
import pear.resourceworld.gui.Gui;
import pear.resourceworld.model.GuiType;

public class GuiManager {
    private final PearResourceWorld plugin;
    private final Map<GuiType, Gui> guiMap = new HashMap<>();

    private File guiFile;
    private FileConfiguration guiConfig;

    public GuiManager(PearResourceWorld plugin) {
        this.plugin = plugin;
    }

    public void load() {
        if (guiFile == null) {
            guiFile = new File(plugin.getDataFolder(), "gui.yml");
        }

        if (!guiFile.exists()) {
            plugin.getLogger().info("Saving a new gui.yml file");
            plugin.saveResource("gui.yml", false);
        }

        guiConfig = YamlConfiguration.loadConfiguration(guiFile);

        guiMap.clear();

        guiMap.put(
            GuiType.ADMIN,
            new AdminGui(plugin, getConfigForGui(GuiType.ADMIN))
        );

        guiMap.put(
            GuiType.CONFIRM_RESET,
            new ConfirmResetGui(plugin, getConfigForGui(GuiType.CONFIRM_RESET))
        );
    }

    public InventoryView openGui(GuiType type, HumanEntity entity) {
        Gui gui = guiMap.get(type);

        if (gui != null) {
            return gui.openInvetory(entity);
        }

        return null;
    }

    private ConfigurationSection getConfigForGui(GuiType type) {
        return guiConfig.getConfigurationSection(type.getConfigKey());
    }
}
