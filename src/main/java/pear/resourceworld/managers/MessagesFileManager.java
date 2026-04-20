package pear.resourceworld.managers;

import java.io.File;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import net.md_5.bungee.api.ChatColor;
import pear.resourceworld.PearResourceWorld;

public class MessagesFileManager {
    private final PearResourceWorld plugin;
    private File messagesFile;
    private FileConfiguration messagesConfig;

    public MessagesFileManager(PearResourceWorld plugin) {
        this.plugin = plugin;
    }

    public void load() {
        if (messagesFile == null) {
            messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        }
        
        if (!messagesFile.exists()) {
            plugin.getLogger().info("Saving a new messages.yml file");
            plugin.saveResource("messages.yml", false);
        }

        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
    }

    public String getMessage(String path) {
        String msg = messagesConfig.getString(path, "")
            .replaceAll("%prefix%", messagesConfig.getString("prefix"));
        return ChatColor.translateAlternateColorCodes('&', msg);
    }
}
