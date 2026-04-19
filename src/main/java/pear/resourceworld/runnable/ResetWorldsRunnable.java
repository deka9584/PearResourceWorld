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
            LocalDate lastReset = plugin.getDataFileManager().getLastReset();
            LocalDate now = LocalDate.now();
    
            if (lastReset.plusMonths(1).isBefore(now) || lastReset.plusMonths(1).isEqual(now)) {
                plugin.getResourceWorldsManager().resetWorlds();
            }
        }
    }
}
