package pear.resourceworld.runnable;

import java.time.LocalDate;

import org.bukkit.scheduler.BukkitRunnable;

import pear.resourceworld.PearResourceWorld;

public class ResetWorldsRunnable extends BukkitRunnable {
    private final PearResourceWorld plugin;

    public ResetWorldsRunnable(PearResourceWorld plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        if (plugin.getResourceWorldsManager().canAutoReset()) {
            int resetInterval = plugin.getConfig().getInt("reset-interval", 0);
            plugin.debugLog("Checking for resource world reset");

            if (resetInterval > 0) {
                LocalDate lastReset = plugin.getDataFileManager().getLastReset();
                LocalDate now = LocalDate.now();
        
                if (!lastReset.plusDays(resetInterval).isAfter(now)) {
                    plugin.getResourceWorldsManager().resetWorlds();
                }
            }
        }
    }
}
