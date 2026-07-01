package pear.resourceworld.managers;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import pear.resourceworld.PearResourceWorld;

public class DataFileManager {
    private final PearResourceWorld plugin;
    
    private File dataFile;
    private FileConfiguration dataConfig;

    public DataFileManager(PearResourceWorld plugin) {
        this.plugin = plugin;
    }

    public void load() {
        if (dataFile == null) {
            dataFile = new File(plugin.getDataFolder(), "data.yml");
        }

        if (!dataFile.exists()) {
            try {
                dataFile.createNewFile();
            } catch (IOException ex) {
                plugin.logError(ex.getMessage());
            }
        }

        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
    }

    public LocalDate getLastReset() {
        String value = dataConfig.getString("last-reset");

        if (value != null && !value.isEmpty()) {
            try {
                return LocalDate.parse(value);
            } catch (DateTimeParseException ex) {
                plugin.logError(ex.getMessage());
            }
        }

        return null;
    }

    public LocalDate getNextReset(int resetInterval) {
        return getLastReset().plusDays(resetInterval);
    }

    public void setLastReset(LocalDate date) {
        dataConfig.set("last-reset", date.toString());
        
        if (!save()) {
            plugin.logError("Unable to save last reset");
        }
    }

    public boolean save() {
        return dataConfig != null && dataFile != null 
            ? plugin.saveFileConfiguration(dataConfig, dataFile)
            : false;
    }

    public FileConfiguration getDataConfig() {
        return dataConfig;
    }
}
