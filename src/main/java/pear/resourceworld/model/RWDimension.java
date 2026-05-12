package pear.resourceworld.model;

import org.bukkit.World.Environment;

public enum RWDimension {
    OVERWORLD(Environment.NORMAL),
    NETHER(Environment.NETHER),
    END(Environment.THE_END);

    private final Environment environment;

    private RWDimension(Environment environment) {
        this.environment = environment;
    }

    public Environment getEnvironment() {
        return environment;
    }

    public static RWDimension getByName(String name) {
        for (RWDimension rwd : RWDimension.values()) {
            if (rwd.name().equalsIgnoreCase(name)) {
                return rwd;
            }
        }

        return null;
    }
}
