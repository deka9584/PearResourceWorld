package pear.resourceworld.managers;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import pear.resourceworld.PearResourceWorld;

public class TeleportManager {
    private final PearResourceWorld plugin;
    private final Set<UUID> activeDelays = new HashSet<>();
    private final Set<UUID> activeSearches = new HashSet<>();

    private int rtpRange;
    private int tpDelay;
    private boolean bypassDelayPerm;

    public TeleportManager(PearResourceWorld plugin) {
        this.plugin = plugin;
    }

    public void load() {
        FileConfiguration config = plugin.getConfig();

        rtpRange = config.getInt("teleport-range");
        tpDelay = config.getInt("teleport-delay");
        bypassDelayPerm = config.getBoolean("bypass-delay-permission");
    }

    public boolean addActiveDelay(UUID playerUUID) {
        return activeDelays.add(playerUUID);
    }

    public boolean addActiveSearch(UUID playerUUID) {
        return activeSearches.add(playerUUID);
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

    public int getTpDelay(Player player) {
        return canBypassDelay(player) ? 0 : tpDelay;
    }

    public void handlePlayerQuit(UUID playerUUID) {
        activeDelays.remove(playerUUID);
        activeSearches.remove(playerUUID);
    }

    public void endLocationSearch(UUID playerUUID) {
        activeSearches.remove(playerUUID);
        plugin.debugLog("End safe location search search");
    }

    public boolean isSearchActive(UUID playerUUID) {
        if (!activeSearches.contains(playerUUID)) {
            return false;
        }

        // Remove active search is player has logged out and return FALSE
        if (plugin.getServer().getPlayer(playerUUID) == null) {
            activeSearches.remove(playerUUID);
            return false;
        }

        return true;
    }

    public boolean isDelayActive(UUID playerUUID) {
        return activeDelays.contains(playerUUID);
    }

    public boolean removeActiveDelay(UUID playerUUID) {
        // Returns TRUE if player had a delay active and is online
        return activeDelays.remove(playerUUID) && plugin.getServer().getPlayer(playerUUID) != null;
    }
}
