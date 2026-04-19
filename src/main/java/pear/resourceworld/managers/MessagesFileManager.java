package pear.resourceworld.managers;

import java.io.File;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import net.md_5.bungee.api.ChatColor;
import pear.resourceworld.PearResourceWorld;

public class MessagesFileManager {
    private final PearResourceWorld plugin;
    private File file;
    private FileConfiguration messages;

    public MessagesFileManager(PearResourceWorld plugin) {
        this.plugin = plugin;
    }

    public void load() {
        file = new File(plugin.getDataFolder(), "messages.yml");
        
        if (!file.exists()) {
            plugin.getLogger().info("Saving a new messages.yml file");
            plugin.saveResource("messages.yml", false);
        }

        messages = YamlConfiguration.loadConfiguration(file);
    }

    public void reload() {
        messages = YamlConfiguration.loadConfiguration(file);
    }

    public String getMessage(String path) {
        String msg = messages.getString(path, "").replaceAll("%prefix%", messages.getString("prefix"));
        return ChatColor.translateAlternateColorCodes('&', msg);
    }
}
