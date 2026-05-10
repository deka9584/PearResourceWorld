package pear.resourceworld.model;

public enum GuiType {
    ADMIN("admin-gui"),
    CONFIRM_RESET("confirm-reset-gui");

    private final String configKey;

    private GuiType(String configKey) {
        this.configKey = configKey;
    }

    public String getConfigKey() {
        return configKey;
    }

    public static GuiType getByName(String name) {
        for (GuiType type : GuiType.values()) {
            if (type.name().equalsIgnoreCase(name)) {
                return type;
            }
        }

        return null;
    }
}
