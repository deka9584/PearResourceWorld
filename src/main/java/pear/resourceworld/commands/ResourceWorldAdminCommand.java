package pear.resourceworld.commands;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
import pear.resourceworld.model.RWDimension;
import pear.resourceworld.model.RWPermission;

public class ResourceWorldAdminCommand implements CommandExecutor, TabCompleter {
    private static final String[] SUBCOMMANDS = {
        "tp",
        "tpspawn",
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
            if (!sender.hasPermission(RWPermission.ADMIN.get())) {
                sender.sendMessage(messagesFm.getNoPermissionMessage());
                return false;
            }

            if (args.length == 0) {
                sendHelp(sender);
                return false;
            }

            switch (args[0].toLowerCase()) {
                case "tp":
                    if (!sender.hasPermission(RWPermission.ADMIN_TP.get())) {
                        sender.sendMessage(messagesFm.getNoPermissionMessage());
                        return false;
                    }

                    return handleTpCommand(sender, args);

                case "tpspawn":
                    if (!sender.hasPermission(RWPermission.ADMIN_TPSPAWN.get())) {
                        sender.sendMessage(messagesFm.getNoPermissionMessage());
                        return false;
                    }

                    return handleTpSpawnCommand(sender, args);

                case "reset":
                    if (!sender.hasPermission(RWPermission.ADMIN_RESET.get())) {
                        sender.sendMessage(messagesFm.getNoPermissionMessage());
                        return false;
                    }

                    rwManager.resetWorlds();
                    return true;

                case "kickall":
                    if (!sender.hasPermission(RWPermission.ADMIN_KICKALL.get())) {
                        sender.sendMessage(messagesFm.getNoPermissionMessage());
                        return false;
                    }

                    rwManager.kickAllFromResourceWorld();
                    sender.sendMessage(messagesFm.getMessage("kicked-all-players-from-resource-world"));
                    return true;

                case "time":
                    if (!sender.hasPermission(RWPermission.ADMIN_TIME.get())) {
                        sender.sendMessage(messagesFm.getNoPermissionMessage());
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
                    sender.sendMessage(messagesFm.getMessage("invalid-admin-subcommand")
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
            return StringUtil.copyPartialMatches(args[0], Arrays.asList(SUBCOMMANDS), completeSubCommand);
        }

        switch (args[0].toLowerCase()) {
            case "tp":
                if (args.length == 2) {
                    return null;
                }

                if (args.length == 3) {
                    Set<String> dimNames = rwManager.getEnabledDimensions().stream()
                        .map(dim -> dim.name())
                        .collect(Collectors.toSet());

                    return StringUtil.copyPartialMatches(args[2], dimNames, completeSubCommand);
                }

                break;

            case "tpspawn":
                if (args.length == 2) {
                    return null;
                }

                break;
            
            default:
                break;
        }

        return completeSubCommand;
    }

    private boolean handleTpCommand(CommandSender sender, String[] args) {
        Player player;
        RWDimension dim = RWDimension.OVERWORLD;

        if (args.length == 1) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(messagesFm.getMessage("command-player-only"));
                return false;
            }

            player = (Player) sender;
        } else {
            player = plugin.getServer().getPlayer(args[1]);

            if (args.length > 2) {
                dim = RWDimension.getFromName(args[2]);
            }
        }

        if (player == null) {
            sender.sendMessage(messagesFm.getMessage("player-not-found"));
            return false;
        }

        if (dim == null || !rwManager.getEnabledDimensions().contains(dim)) {
            sender.sendMessage(messagesFm.getMessage("dimension-not-found"));
            return false;
        }

        if (!rwManager.isResourceWorldReady()) {
            sender.sendMessage(messagesFm.getMessage("reset-still-in-progress"));
            return false;
        }

        if (!rwManager.teleportPlayerToResourceWorld(player, dim)) {
            sender.sendMessage(messagesFm.getMessage("teleport-failed"));
            return false;
        }

        sender.sendMessage(messagesFm.getMessage("teleport-success"));
        return true;
    }

    private boolean handleTpSpawnCommand(CommandSender sender, String[] args) {
        Player player;

        if (args.length == 1) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(messagesFm.getMessage("command-player-only"));
                return false;
            }

            player = (Player) sender;
        } else {
            player = plugin.getServer().getPlayer(args[1]);
        }

        if (player == null) {
            sender.sendMessage(messagesFm.getMessage("player-not-found"));
            return false;
        }

        if (!player.teleport(rwManager.getSpawnWorld().getSpawnLocation())) {
            sender.sendMessage(messagesFm.getMessage("teleport-failed"));
            return false;
        }

        sender.sendMessage(messagesFm.getMessage("teleport-success"));
        return true;
    }

    private void sendHelp(CommandSender sender) {
        PluginDescriptionFile desc = plugin.getDescription();
        String authors = String.join(", ", desc.getAuthors());
        sender.sendMessage(ChatColor.GREEN + desc.getName() + " v." + desc.getVersion() + " by " + authors);
        sender.sendMessage("Subcommands: " + String.join(", ", SUBCOMMANDS));
    }
}
