package pear.resourceworld.managers;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryView;

import pear.resourceworld.PearResourceWorld;
import pear.resourceworld.gui.AdminGui;
import pear.resourceworld.gui.ConfirmResetGui;
import pear.resourceworld.gui.Gui;
import pear.resourceworld.gui.PlayerTeleportGui;
import pear.resourceworld.gui.AdminTeleportGui;
import pear.resourceworld.model.GuiType;

public class GuiManager {
    private final PearResourceWorld plugin;
    private final Map<GuiType, Gui> guiMap = new HashMap<>();

    private File guiFile;
    private FileConfiguration guiConfig;
    private boolean enableSounds;

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
        enableSounds = guiConfig.getBoolean("enable-gui-sounds");

        if (plugin.copyDefaultConfigOptions(guiConfig, "gui.yml")) {
            plugin.saveFileConfiguration(guiConfig, guiFile);
        }

        guiMap.clear();

        registerGui(createGui(GuiType.ADMIN));
        registerGui(createGui(GuiType.CONFIRM_RESET));
        registerGui(createGui(GuiType.ADMIN_TELEPORT));
        registerGui(createGui(GuiType.PLAYER_TELEPORT));
    }

    public Gui createGui(GuiType type) {
        Gui gui = initGui(type);
        gui.setSoundsEnabled(enableSounds);
        return gui;
    }

    public InventoryView openGui(GuiType type, Player player) {
        Gui gui = guiMap.get(type);
        return gui == null ? null : gui.openInvetory(player);
    }

    private Gui initGui(GuiType type) {
        ConfigurationSection configSect = guiConfig.getConfigurationSection(type.getConfigKey());

        switch (type) {
            case ADMIN:
                return new AdminGui(plugin, configSect);

            case CONFIRM_RESET:
                return new ConfirmResetGui(plugin, configSect);

            case ADMIN_TELEPORT:
                return new AdminTeleportGui(plugin, configSect);

            case PLAYER_TELEPORT:
                return new PlayerTeleportGui(plugin, configSect);
        
            default:
                plugin.logWarn("Invalid GUI type:" + type.name());
                return null;
        }
    }

    private void registerGui(Gui gui) {
        guiMap.put(gui.getType(), gui);
    }
}
