package pear.resourceworld.model;

public enum RWPermission {
    ADMIN("pearresourceworld.admin"),
    ADMIN_TP("pearresourceworld.admin.tp"),
    ADMIN_TPSPAWN("pearresourceworld.admin.tpspawn"),
    ADMIN_RESET("pearresourceworld.admin.reset"),
    ADMIN_KICKALL("pearresourceworld.admin.kickall"),
    ADMIN_TIME("pearresourceworld.admin.time"),
    TP("pearresourceworld.tp"),
    TP_SPAWN("pearresourceworld.tp.spawn"),
    TP_COOLDOWN_BYPASS("pearresourceworld.tp.cooldown.bypass"),
    TP_DELAY_BYPASS("pearresourceworld.tp.delay.bypass"),
    SIGNS_CREATE("pearresourceworld.signs.create"),
    SIGNS_USE("pearresourceworld.signs.use"),
    SIGNS_BREAK("pearresourceworld.signs.break");

    private final String node;

    private RWPermission(String node) {
        this.node = node;
    }

    public String get() {
        return node;
    }
}
