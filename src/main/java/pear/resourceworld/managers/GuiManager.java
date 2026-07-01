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

        if (plugin.copyDefaultConfigOptions(guiConfig, "gui.yml")) {
            plugin.saveFileConfiguration(guiConfig, guiFile);
        }

        enableSounds = guiConfig.getBoolean("enable-gui-sounds");

        guiMap.clear();

        registerGui(setupGui(GuiType.ADMIN));
        registerGui(setupGui(GuiType.CONFIRM_RESET));
        registerGui(setupGui(GuiType.ADMIN_TELEPORT));
        registerGui(setupGui(GuiType.PLAYER_TELEPORT));
    }

    public Gui setupGui(GuiType type) {
        Gui gui = createGui(type);

        if (gui == null) {
            plugin.logError("Unable to create GUI: " + type.name());
            return null;
        }

        gui.setSoundsEnabled(enableSounds);
        return gui;
    }

    public InventoryView openGui(GuiType type, Player player) {
        Gui gui = guiMap.get(type);
        return gui == null ? null : gui.openInvetory(player);
    }

    private Gui createGui(GuiType type) {
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
                return null;
        }
    }

    private void registerGui(Gui gui) {
        guiMap.put(gui.getType(), gui);
    }
}
