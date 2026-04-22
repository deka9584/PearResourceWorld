package pear.resourceworld.listeners;

import org.bukkit.World;
import org.bukkit.boss.DragonBattle;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;

import pear.resourceworld.PearResourceWorld;
import pear.resourceworld.managers.ResourceWorldsManager;
import pear.resourceworld.model.ResourceWorldSettings;

public class EntitySpawnListener implements Listener {
    private final PearResourceWorld plugin;
    private final ResourceWorldsManager rwManager;

    public EntitySpawnListener(PearResourceWorld plugin) {
        this.plugin = plugin;
        this.rwManager = plugin.getResourceWorldsManager();
    }

    @EventHandler(ignoreCancelled = true)
    public void onDragonSpawn(CreatureSpawnEvent event) {
        if (event.getEntityType() != EntityType.ENDER_DRAGON) {
            return;
        }

        World world = event.getEntity().getWorld();
        
        if (!rwManager.isResourceWorld(world)) {
            return;
        }

        plugin.debugLog("Server attempt to spawn dragon: " + event.getSpawnReason().name());

        ResourceWorldSettings rwSettings = rwManager.getResourceWorldSettings();

        if (rwSettings != null && rwSettings.getDisableDragonBattle()) {
            DragonBattle dragonBattle = world.getEnderDragonBattle();

            if (dragonBattle != null) {
                dragonBattle.getBossBar().setVisible(false);
            }

            event.setCancelled(true);

            // TO DO: Reset dragon battle

            plugin.getRwPortalHelper().activateEndExitPortal(world);
            plugin.getLogger().info("Prevent dragon spawn on resource world");
        }
    }
}
