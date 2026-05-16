package pear.resourceworld.managers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import pear.resourceworld.PearResourceWorld;
import pear.resourceworld.model.RWPermission;

public class TeleportManager {
    private final PearResourceWorld plugin;
    private final Map<UUID, BukkitTask> activeDelays = new HashMap<>();
    private final Map<UUID, BukkitTask> activeSearches = new HashMap<>();
    private final List<Material> blacklistedBlockTypes = new ArrayList<>();

    private int rtpRange;
    private int tpDelay;
    private int signTpDelay;
    private boolean bypassDelayPerm;
    private boolean preventTpInLiquid;

    public TeleportManager(PearResourceWorld plugin) {
        this.plugin = plugin;
    }

    public void load() {
        FileConfiguration config = plugin.getConfig();

        rtpRange = config.getInt("teleport-range");
        tpDelay = config.getInt("teleport-delay");
        signTpDelay = config.getInt("signs-teleport-delay");
        bypassDelayPerm = config.getBoolean("bypass-delay-permission");
        preventTpInLiquid = config.getBoolean("safe-location-check.prevent-liquid-blocks");

        blacklistedBlockTypes.clear();

        config.getStringList("safe-location-check.block-blacklist").forEach(s -> {
            Material material = Material.matchMaterial(s);

            if (material == null) {
                plugin.logWarn("Invalid material: " + s);
            } else {
                blacklistedBlockTypes.add(material);
                plugin.debugLog("Added blacklisted block type: " + material.name());
            }
        });
    }

    public BukkitTask addActiveDelay(UUID playerUUID, BukkitTask task) {
        return activeDelays.put(playerUUID, task);
    }

    public BukkitTask addActiveSearch(UUID playerUUID, BukkitTask task) {
        return activeSearches.put(playerUUID, task);
    }

    public boolean canBypassDelay(Player player) {
        return bypassDelayPerm && player.hasPermission(RWPermission.TP_DELAY_BYPASS.get());
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

    public void stopTeleportTasks(UUID playerUUID) {
        endTeleportDelay(playerUUID);
        endLocationSearch(playerUUID);
    }

    public void endTeleportDelay(UUID playerUUID) {
        BukkitTask task = activeDelays.remove(playerUUID);

        if (task != null && !task.isCancelled()) {
            task.cancel();
            plugin.debugLog("End teleport delay");
        }
    }

    public void endLocationSearch(UUID playerUUID) {
        BukkitTask task = activeSearches.remove(playerUUID);

        if (task != null && !task.isCancelled()) {
            task.cancel();
            plugin.debugLog("End safe location search");
        }
    }

    public boolean isSearchActive(UUID playerUUID) {
        return activeSearches.containsKey(playerUUID);
    }

    public boolean isDelayActive(UUID playerUUID) {
        return activeDelays.containsKey(playerUUID);
    }

    public boolean isLocationSafe(Location loc) {
        World world = loc.getWorld();
        Block feetBlock = world.getBlockAt(loc);

        if (!feetBlock.isEmpty() || !feetBlock.getRelative(BlockFace.UP).isEmpty()) {
            return false;
        }

        Block belowBlock = feetBlock.getRelative(BlockFace.DOWN);

        if (preventTpInLiquid && belowBlock.isLiquid()) {
            return false;
        }

        return !blacklistedBlockTypes.contains(belowBlock.getType());
    }

    public boolean removeActiveDelay(UUID playerUUID) {
        return activeDelays.remove(playerUUID) != null;
    }
}
