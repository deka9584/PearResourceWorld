package pear.resourceworld.listeners;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.boss.BossBar;
import org.bukkit.boss.DragonBattle;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import pear.resourceworld.PearResourceWorld;
import pear.resourceworld.managers.ResourceWorldsManager;
import pear.resourceworld.model.RWDimension;
import pear.resourceworld.model.ResourceWorld;
import pear.resourceworld.utils.WorldUtils;

public class DragonRespawnListener implements Listener {
    private final PearResourceWorld plugin;
    private final ResourceWorldsManager rwManager;

    public DragonRespawnListener(PearResourceWorld plugin) {
        this.plugin = plugin;
        this.rwManager = plugin.getResourceWorldsManager();
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        World world = player.getWorld();

        if (!rwManager.isResourceWorld(world) || world.getEnvironment() != Environment.THE_END) {
            return;
        }

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            ItemStack item = event.getItem();
            Block block = event.getClickedBlock();

            if (item != null && block != null && item.getType() == Material.END_CRYSTAL) {
                boolean preventCrystalPlace = (
                    rwManager.getResourceWorldSettings().getPreventDragonRespawn() &&
                    block.getType() == Material.BEDROCK &&
                    WorldUtils.hasRelativeBlockType(block, Material.BEDROCK)
                );
    
                if (preventCrystalPlace) {
                    event.setCancelled(true);

                    player.sendMessage(
                        plugin.getMessagesFileManager().getMessage("dragon-respawn-disabled")
                    );

                    plugin.debugLog("Prevented placing crystal on bedrock from player: " + player.getName());
                }
            }
        }
    }

    @EventHandler
    public void onEntitySpawn(EntitySpawnEvent event) {
        World world = event.getEntity().getWorld();

        if (!rwManager.isResourceWorld(world)) {
            return;
        }

        boolean preventDragonRespawn = rwManager.getResourceWorldSettings().getPreventDragonRespawn();

        if (preventDragonRespawn && event.getEntityType() == EntityType.ENDER_DRAGON) {
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                ResourceWorld rwEnd = rwManager.getResourceWorld(RWDimension.END);
                World endWorld = rwEnd != null ? rwEnd.getWorld() : null;

                if (endWorld == null) {
                    return;
                }

                DragonBattle battle = world.getEnderDragonBattle();

                if (battle == null) {
                    return;
                }

                EnderDragon dragon = battle.getEnderDragon();
                BossBar bossBar = battle.getBossBar();

                if (bossBar != null) {
                    bossBar.setVisible(false);
                }

                if (dragon != null) {
                    dragon.remove();
                    plugin.debugLog("Removed ender dragon from world: " + endWorld.getName());
                }

                plugin.getRwPortalHelper().activateEndExitPortal(endWorld);
            }, 2L);
        }
    }
}
