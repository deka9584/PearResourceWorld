package pear.resourceworld.listeners;

import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;

import pear.resourceworld.PearResourceWorld;

public class WorldLoadListener implements Listener {
    private final PearResourceWorld plugin;

    public WorldLoadListener(PearResourceWorld plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onWorldLoad(WorldLoadEvent event) {
        World world = event.getWorld();

        if (!plugin.getResourceWorldsManager().isResourceWorld(world)) {
            return;
        }

        // If dragon battle is disabled create the end spawn portal
        if (world.getEnvironment() == Environment.THE_END && plugin.getRwPortalHelper().isDragonBattleDisabled()) {
            plugin.getRwPortalHelper().createEndSpawnPortal(world);
        }
    }
}
