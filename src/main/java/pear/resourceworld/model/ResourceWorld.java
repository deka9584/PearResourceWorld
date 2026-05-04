package pear.resourceworld.model;

import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.World.Environment;

public class ResourceWorld {
    private String name;
    private double borderSize;
    private Environment environment;
    private World world;

    public ResourceWorld(String name, Environment environment) {
        this.name = name;
        this.environment = environment;
    }

    public String getName() {
        return name;
    }

    public double getBorderSize() {
        return borderSize;
    }

    public void setBorderSize(double size) {
        this.borderSize = size;
        updateWorldBorder();
    }

    public Environment getEnvironment() {
        return environment;
    }

    public World getWorld() {
        return world;
    }

    public void setWorld(World w) {
        world = w;
    }

    public boolean updateWorldBorder() {
        if (world == null) {
            return false;
        }

        WorldBorder wb = world.getWorldBorder();

        wb.setCenter(0, 0);
        wb.setSize(borderSize);
        return true;
    }

    public boolean updateWorldFlags(ResourceWorldSettings settings) {
        if (world == null) {
            return false;
        }

        world.setPVP(settings.getPVP());
        world.setDifficulty(settings.getDifficulty());
        world.setKeepSpawnInMemory(settings.getKeepSpawnInMemory());

        world.setGameRule(GameRule.KEEP_INVENTORY, settings.getKeepInventory());
        world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, !settings.getAlwaysDay());

        return true;
    }
}
