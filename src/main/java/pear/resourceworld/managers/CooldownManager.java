package pear.resourceworld.managers;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import pear.resourceworld.PearResourceWorld;

public class CooldownManager {
    private final PearResourceWorld plugin;
    private final Map<UUID, Long> cooldowns = new HashMap<>();

    private boolean bypassCooldownPerm;
    private long tpCooldownMillis;
    private BukkitTask cleanupCooldownsTask;

    public CooldownManager(PearResourceWorld plugin) {
        this.plugin = plugin;
    }

    public void load() {
        bypassCooldownPerm = plugin.getConfig().getBoolean("bypass-cooldown-permission");
        tpCooldownMillis = plugin.getConfig().getInt("teleport-cooldown") * 1000L;

        if (cleanupCooldownsTask != null && !cleanupCooldownsTask.isCancelled()) {
            cleanupCooldownsTask.cancel();
        }

        if (tpCooldownMillis > 0) {
            cleanupCooldownsTask = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
                long now = System.currentTimeMillis();

                if (cooldowns.entrySet().removeIf(entry -> now - entry.getValue() > tpCooldownMillis)) {
                    plugin.debugLog("Cooldowns cleaned up");
                }
            }, 20L * 60 * 60, 20L * 60 * 60);
        }
    }

    public void addTpCooldown(UUID playerUUID) {
        if (tpCooldownMillis > 0) {
            cooldowns.put(playerUUID, System.currentTimeMillis());
        }
    }

    public boolean canBypassCooldown(Player player) {
        return bypassCooldownPerm && player.hasPermission("pearresourceworld.tp.cooldown.bypass");
    }

    public int getTpRemainingSeconds(Player player) {
        if (canBypassCooldown(player)) {
            return 0;
        }

        UUID playerUUID = player.getUniqueId();

        if (cooldowns.containsKey(playerUUID)) {
            long now = System.currentTimeMillis();
            long lastUse = cooldowns.get(playerUUID);
            
            if (now - lastUse < tpCooldownMillis) {
                return (int) ((tpCooldownMillis - (now - lastUse)) / 1000);
            }
        }

        return 0;
    }
}
