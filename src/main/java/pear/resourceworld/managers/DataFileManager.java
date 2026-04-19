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
    private FileConfiguration data;

    public DataFileManager(PearResourceWorld plugin) {
        this.plugin = plugin;
    }

    public LocalDate getLastReset() {
        String value = data.getString("last-reset");

        if (value != null && !value.isEmpty()) {
            try {
                return LocalDate.parse(value);
            } catch (DateTimeParseException ex) {
                plugin.logError(ex.getMessage());
            }
        }

        return null;
    }

    public void setLastReset(LocalDate date) {
        data.set("last-reset", date.toString());
        save();
    }

    public void load() {
        dataFile = new File(plugin.getDataFolder(), "data.yml");

        if (!dataFile.exists()) {
            try {
                dataFile.createNewFile();
            } catch (IOException ex) {
                plugin.logError("Unable to create data.yml file");
                plugin.logError(ex.getMessage());
            }
        }

        data = YamlConfiguration.loadConfiguration(dataFile);
        data.options().copyDefaults();
        plugin.debugLog("Loaded data file");
    }

    public boolean save() {
        if (dataFile == null || data == null) {
            try {
                data.save(dataFile);
                return true;
            } catch (IOException ex) {
                plugin.logError("Unable to save data.yml file");
                plugin.logError(ex.getMessage());
            }
        }

        return false;
    }

    public FileConfiguration getData() {
        return data;
    }
}
