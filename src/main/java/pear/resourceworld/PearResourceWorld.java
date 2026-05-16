package pear.resourceworld;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.util.logging.Level;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import pear.resourceworld.commands.ResourceWorldAdminCommand;
import pear.resourceworld.commands.ResourceWorldCommand;
import pear.resourceworld.helpers.RWPortalHelper;
import pear.resourceworld.helpers.SignsHelper;
import pear.resourceworld.helpers.TeleportHelper;
import pear.resourceworld.listeners.DragonRespawnListener;
import pear.resourceworld.listeners.GuiListener;
import pear.resourceworld.listeners.PlayerCommandListener;
import pear.resourceworld.listeners.PlayerJoinLeaveListener;
import pear.resourceworld.listeners.PlayerRespawnListener;
import pear.resourceworld.listeners.PortalListener;
import pear.resourceworld.listeners.SignsListener;
import pear.resourceworld.listeners.TeleportListener;
import pear.resourceworld.managers.CommandBlacklistManager;
import pear.resourceworld.managers.CooldownManager;
import pear.resourceworld.managers.DataFileManager;
import pear.resourceworld.managers.GuiManager;
import pear.resourceworld.managers.MessagesFileManager;
import pear.resourceworld.managers.ResourceWorldsManager;
import pear.resourceworld.managers.SignsFileManager;
import pear.resourceworld.managers.TeleportManager;
import pear.resourceworld.runnable.ResetWorldsRunnable;

public class PearResourceWorld extends JavaPlugin {
    private static PearResourceWorld plugin;

    private DataFileManager dataFileManager;
    private MessagesFileManager messagesFileManager;
    private ResourceWorldsManager resourceWorldsManager;
    private CooldownManager cooldownManager;
    private TeleportManager teleportManager;
    private SignsFileManager signsFileManager;
    private CommandBlacklistManager commandBlacklistManager;
    private GuiManager guiManager;
    private RWPortalHelper rwPortalHelper;
    private TeleportHelper teleportHelper;
    private SignsHelper signsHelper;
    private BukkitTask resetWorldsTask;

    // This code is called after the server starts and after the /reload command
    @Override
    public void onEnable() {
        plugin = this;

        saveDefaultConfig();

        dataFileManager = new DataFileManager(this);
        messagesFileManager = new MessagesFileManager(this);
        resourceWorldsManager = new ResourceWorldsManager(this);
        signsFileManager = new SignsFileManager(this);
        cooldownManager = new CooldownManager(this);
        teleportManager = new TeleportManager(this);
        commandBlacklistManager = new CommandBlacklistManager(this);
        guiManager = new GuiManager(this);

        rwPortalHelper = new RWPortalHelper(this);
        teleportHelper = new TeleportHelper(this);
        signsHelper = new SignsHelper(this);

        dataFileManager.load();
        messagesFileManager.load();
        signsFileManager.load();
        resourceWorldsManager.loadWorlds();
        cooldownManager.load();
        teleportManager.load();
        commandBlacklistManager.load();
        guiManager.load();

        getCommand("pearresourceworldadmin").setExecutor(new ResourceWorldAdminCommand(this));
        getCommand("pearresourceworld").setExecutor(new ResourceWorldCommand(this));

        registerListeners();
        updateTaskTimer();

        getLogger().log(Level.INFO, "{0}.onEnable()", this.getClass().getName());
    }

    // This code is called before the server stops and after the /reload command
    @Override
    public void onDisable() {
        if (resetWorldsTask != null && !resetWorldsTask.isCancelled()) {
            resetWorldsTask.cancel();
        }

        getLogger().log(Level.INFO, "{0}.onDisable()", this.getClass().getName());
    }

    public boolean copyDefaultConfigOptions(FileConfiguration fc, String resName) {
        InputStream configResource = getResource(resName);

        if (configResource == null) {
            logError("Unable load default config resource");
            return false;
        }

        FileConfiguration configDefaults = YamlConfiguration.loadConfiguration(
            new InputStreamReader(configResource)
        );

        boolean changed = false;

        for (String key : configDefaults.getKeys(true)) {
            if (!fc.contains(key, true)) {
                fc.set(key, configDefaults.get(key));
                changed = true;
            }
        }

        if (changed) {
            getLogger().info("Detected changes in resource: " + resName);
        }

        return changed;
    }

    public void debugLog(String msg) {
        if (getConfig().getBoolean("debug")) {
            getLogger().log(Level.INFO, msg);
        }
    }

    public DataFileManager getDataFileManager() {
        return dataFileManager;
    }

    public MessagesFileManager getMessagesFileManager() {
        return messagesFileManager;
    }

    public SignsFileManager getSignsFileManager() {
        return signsFileManager;
    }

    public ResourceWorldsManager getResourceWorldsManager() {
        return resourceWorldsManager;
    }

    public CooldownManager getCooldownManager() {
        return cooldownManager;
    }

    public TeleportManager getTeleportManager() {
        return teleportManager;
    }

    public CommandBlacklistManager getCommandBlacklistManager() {
        return commandBlacklistManager;
    }

    public GuiManager getGuiManager() {
        return guiManager;
    }

    public RWPortalHelper getRwPortalHelper() {
        return rwPortalHelper;
    }

    public TeleportHelper getTeleportHelper() {
        return teleportHelper;
    }

    public SignsHelper getSignsHelper() {
        return signsHelper;
    }

    public String getNextResetMessage() {
        int resetInterval = plugin.getConfig().getInt("reset-interval", 0);
        boolean isAutoReset = resetInterval > 0 && plugin.getConfig().getBoolean("auto-reset");

        return messagesFileManager.getMessage("show-reset-time")
            .replaceAll("%lastReset%", dataFileManager.getLastReset().toString())
            .replaceAll("%nextReset%", isAutoReset ? dataFileManager.getNextReset(resetInterval).toString() : "N/A");
    }

    public void logError(String msg) {
        getLogger().log(Level.SEVERE, msg);
    }

    public void logWarn(String msg) {
        getLogger().log(Level.WARNING, msg);
    }

    public void registerListeners() {
        PluginManager pm = getServer().getPluginManager();

        pm.registerEvents(new PlayerJoinLeaveListener(this), this);
        pm.registerEvents(new PortalListener(this), this);
        pm.registerEvents(new TeleportListener(this), this);
        pm.registerEvents(new PlayerRespawnListener(this), this);
        pm.registerEvents(new DragonRespawnListener(this), this);
        pm.registerEvents(new SignsListener(this), this);
        pm.registerEvents(new PlayerCommandListener(this), this);
        pm.registerEvents(new GuiListener(this), this);
    }

    public void updateTaskTimer() {
        if (resetWorldsTask != null && !resetWorldsTask.isCancelled()) {
            resetWorldsTask.cancel();
        }

        if (getConfig().getBoolean("auto-reset")) {
            if (dataFileManager.getLastReset() == null) {
                getLogger().info("No reset date found: Setting current date");
                updateLastResetDate();
            }

            resetWorldsTask = new ResetWorldsRunnable(this).runTaskTimer(this, 0L, 20L * 60 * 60);
        }
    }

    public void updateLastResetDate() {
        dataFileManager.setLastReset(LocalDate.now());
    }

    public static PearResourceWorld getInstance() {
        return plugin;
    }
}
