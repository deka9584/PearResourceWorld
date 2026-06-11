package pear.resourceworld.managers;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.entity.Player;

import pear.resourceworld.PearResourceWorld;
import pear.resourceworld.model.RWPermission;

public class CooldownManager {
    private final PearResourceWorld plugin;
    private final Map<UUID, Long> tpCooldowns = new HashMap<>();

    private boolean bypassCooldownPerm;
    private long tpCooldownMillis;

    public CooldownManager(PearResourceWorld plugin) {
        this.plugin = plugin;
    }

    public void load() {
        bypassCooldownPerm = plugin.getConfig().getBoolean("bypass-cooldown-permission");
        tpCooldownMillis = plugin.getConfig().getInt("teleport-cooldown") * 1000L;
    }

    public void addTpCooldown(UUID playerUUID) {
        if (tpCooldownMillis > 0) {
            tpCooldowns.put(playerUUID, System.currentTimeMillis() + tpCooldownMillis);
        }
    }

    public boolean canBypassTpCooldown(Player player) {
        return bypassCooldownPerm && player.hasPermission(RWPermission.TP_COOLDOWN_BYPASS.get());
    }

    public int getTpRemainingSeconds(Player player) {
        if (!canBypassTpCooldown(player)) {
            Long expiry = tpCooldowns.get(player.getUniqueId());

            if (expiry != null) {
                long remaining = expiry - System.currentTimeMillis();

                if (remaining > 0) {
                    return (int) Math.ceil(remaining / 1000);
                }
            }
        }

        return 0;
    }

    public boolean removeTpCooldown(UUID playerUUID, boolean ignoreExpiry) {
        Long expiry = tpCooldowns.get(playerUUID);

        if (expiry == null) {
            return false;
        }

        if (ignoreExpiry || expiry <= System.currentTimeMillis()) {
            tpCooldowns.remove(playerUUID);
            return true;
        }

        return false;
    }
}
