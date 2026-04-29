package pear.resourceworld.managers;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import pear.resourceworld.PearResourceWorld;
import pear.resourceworld.utils.Utils;

public class SignsFileManager {
    private final PearResourceWorld plugin;
    private File signsFile;
    private FileConfiguration signsConfig;

    public SignsFileManager(PearResourceWorld plugin) {
        this.plugin = plugin;
    }

    public void load() {
        if (signsFile == null) {
            signsFile = new File(plugin.getDataFolder(), "signs.yml");
        }

        if (!signsFile.exists()) {
            plugin.getLogger().info("Saving a new signs.yml file");
            plugin.saveResource("signs.yml", false);
        }

        signsConfig = YamlConfiguration.loadConfiguration(signsFile);
    }

    public String getColoredString(String path) {
        return Utils.translateColorCodes(signsConfig.getString(path, ""));
    }

    public String getTitle() {
        return getColoredString("signs-title");
    }

    public List<String> getTeleportSignLines() {
        return signsConfig.getStringList("teleport-sign").stream()
            .map(s -> Utils.translateColorCodes(s))
            .collect(Collectors.toList());
    }

    public boolean isBreakPrevented() {
        return signsConfig.getBoolean("prevent-sign-break");
    }
}
