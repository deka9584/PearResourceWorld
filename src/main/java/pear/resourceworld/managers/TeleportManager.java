package pear.resourceworld.managers;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import pear.resourceworld.PearResourceWorld;

public class TeleportManager {
    private final PearResourceWorld plugin;
    private final Map<UUID, BukkitTask> activeDelays = new HashMap<>();
    private final Map<UUID, BukkitTask> activeSearches = new HashMap<>();

    private int rtpRange;
    private int tpDelay;
    private int signTpDelay;
    private boolean bypassDelayPerm;

    public TeleportManager(PearResourceWorld plugin) {
        this.plugin = plugin;
    }

    public void load() {
        FileConfiguration config = plugin.getConfig();

        rtpRange = config.getInt("teleport-range");
        tpDelay = config.getInt("teleport-delay");
        signTpDelay = config.getInt("signs-teleport-delay");
        bypassDelayPerm = config.getBoolean("bypass-delay-permission");
    }

    public BukkitTask addActiveDelay(UUID playerUUID, BukkitTask task) {
        return activeDelays.put(playerUUID, task);
    }

    public BukkitTask addActiveSearch(UUID playerUUID, BukkitTask task) {
        return activeSearches.put(playerUUID, task);
    }

    public boolean canBypassDelay(Player player) {
        return bypassDelayPerm && player.hasPermission("pearresourceworld.tp.delay.bypass");
    }

    public int getRtpRange() {
        return rtpRange;
    }

    public int getTpDelay() {
        return tpDelay;
    }

    public int getTpDelay(Player player, boolean fromSign) {
        if (player != null && canBypassDelay(player)) {
            return 0;
        }

        return fromSign ? signTpDelay : tpDelay;
    }

    public void handlePlayerQuit(UUID playerUUID) {
        BukkitTask delayTask = activeDelays.remove(playerUUID);

        if (delayTask != null && !delayTask.isCancelled()) {
            delayTask.cancel();
            plugin.debugLog("Player logged out: teleport delay cancelled");
        }

        endLocationSearch(playerUUID);
    }

    public void endLocationSearch(UUID playerUUID) {
        BukkitTask task = activeSearches.remove(playerUUID);

        if (task != null && !task.isCancelled()) {
            task.cancel();
            plugin.debugLog("End safe location search search");
        }
    }

    public boolean isSearchActive(UUID playerUUID) {
        return activeSearches.containsKey(playerUUID);
    }

    public boolean isDelayActive(UUID playerUUID) {
        return activeDelays.containsKey(playerUUID);
    }

    public boolean removeActiveDelay(UUID playerUUID) {
        return activeDelays.remove(playerUUID) != null;
    }
}
