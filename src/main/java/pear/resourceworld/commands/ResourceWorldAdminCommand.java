package pear.resourceworld.commands;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.util.StringUtil;

import net.md_5.bungee.api.ChatColor;
import pear.resourceworld.PearResourceWorld;
import pear.resourceworld.managers.MessagesFileManager;
import pear.resourceworld.managers.ResourceWorldsManager;

public class ResourceWorldAdminCommand implements CommandExecutor, TabCompleter {
    private static final String[] subcommands = {
        "tp",
        "reset",
        "kickall",
        "time",
        "help"
    };

    private final PearResourceWorld plugin;
    private final MessagesFileManager messagesFm;
    private final ResourceWorldsManager rwManager;

    public ResourceWorldAdminCommand(PearResourceWorld plugin) {
        this.plugin = plugin;
        this.messagesFm = plugin.getMessagesFileManager();
        this.rwManager = plugin.getResourceWorldsManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player || sender instanceof ConsoleCommandSender) {
            if (!sender.hasPermission(command.getPermission())) {
                sender.sendMessage(messagesFm.getMessage("no-permission"));
                return false;
            }

            if (args.length == 0) {
                sendHelp(sender);
                return false;
            }

            switch (args[0].toLowerCase()) {
                case "tp":
                    if (!sender.hasPermission(command.getPermission() + ".tp")) {
                        sender.sendMessage(messagesFm.getMessage("no-permission"));
                        return false;
                    }

                    if (sender instanceof Player) {
                        Player player = (Player) sender;

                        if (!rwManager.isResourceWorldReady()) {
                            sender.sendMessage(messagesFm.getMessage("reset-still-in-progress"));
                            return false;
                        }

                        if (rwManager.isResourceWorld(player.getWorld())) {
                            sender.sendMessage(messagesFm.getMessage("already-resource-world-self"));
                            return false;
                        }

                        if (!rwManager.teleportPlayerToResourceWorld(player)) {
                            sender.sendMessage(messagesFm.getMessage("teleport-failed"));
                            return false;
                        }

                        sender.sendMessage(messagesFm.getMessage("teleport-to-resource-world"));
                        return true;
                    }

                    sender.sendMessage(messagesFm.getMessage("command-player-only"));
                    return false;

                case "reset":
                    if (!sender.hasPermission(command.getPermission() + ".reset")) {
                        sender.sendMessage(messagesFm.getMessage("no-permission"));
                        return false;
                    }

                    rwManager.resetWorlds();
                    return true;

                case "kickall":
                    if (!sender.hasPermission(command.getPermission() + ".kickall")) {
                        sender.sendMessage(messagesFm.getMessage("no-permission"));
                        return false;
                    }

                    rwManager.kickAllFromResourceWorld();
                    sender.sendMessage(messagesFm.getMessage("kicked-all-players-from-resource-world"));
                    return true;

                case "time":
                    if (!sender.hasPermission(command.getPermission() + ".time")) {
                        sender.sendMessage(messagesFm.getMessage("no-permission"));
                        return false;
                    }

                    int resetInterval = plugin.getConfig().getInt("reset-interval", 0);
                    boolean isAutoReset = resetInterval > 0 && plugin.getConfig().getBoolean("auto-reset");
                    LocalDate lastReset = plugin.getDataFileManager().getLastReset();

                    sender.sendMessage(messagesFm.getMessage("show-reset-time")
                        .replaceAll("%lastReset%", lastReset.toString())
                        .replaceAll("%nextReset%", isAutoReset ? lastReset.plusDays(resetInterval).toString() : "N/A")
                    );

                    return true;
                
                case "help":
                    sendHelp(sender);
                    return true;

                default:
                    sender.sendMessage(messagesFm.getMessage("invalid-subcommand")
                        .replaceAll("%helpcmd%", "/" + command.getName() + " help")
                    );
                    return false;
            }
        }

        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completeSubCommand = new ArrayList<>();

        if (args.length == 1) {
            return StringUtil.copyPartialMatches(args[0], Arrays.asList(subcommands), completeSubCommand);
        }

        return completeSubCommand;
    }

    private void sendHelp(CommandSender sender) {
        PluginDescriptionFile desc = plugin.getDescription();
        String authors = String.join(", ", desc.getAuthors());
        sender.sendMessage(ChatColor.GREEN + desc.getName() + " v." + desc.getVersion() + " by " + authors);
        sender.sendMessage("Subcommands: " + String.join(", ", subcommands));
    }
}
