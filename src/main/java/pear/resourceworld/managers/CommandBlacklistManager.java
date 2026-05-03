package pear.resourceworld.managers;

import java.util.List;

import pear.resourceworld.PearResourceWorld;

public class CommandBlacklistManager {
    private final PearResourceWorld plugin;

    private boolean enabled;
    private List<String> blacklist;

    public CommandBlacklistManager(PearResourceWorld plugin) {
        this.plugin = plugin;
    }

    public void load() {
        enabled = plugin.getConfig().getBoolean("enable-command-blacklist");
        blacklist = plugin.getConfig().getStringList("command-blacklist");
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isBlacklisted(String command) {
        if (blacklist == null) {
            return false;
        }

        if (command.startsWith("/")) {
            command = command.replaceFirst("/", "");
        }

        String commandName = command.split(" ")[0];
        String[] parts = commandName.split(":");
        return blacklist.contains(parts[parts.length - 1]);
    }
}
