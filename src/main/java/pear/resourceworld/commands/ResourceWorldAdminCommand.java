package pear.resourceworld.commands;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import pear.resourceworld.PearResourceWorld;
import pear.resourceworld.managers.MessagesFileManager;
import pear.resourceworld.managers.ResourceWorldsManager;

public class ResourceWorldAdminCommand implements CommandExecutor, TabCompleter {
    private static String[] subcommands = {
        "tp",
        "reset"
    };

    private final PearResourceWorld plugin;
    private final MessagesFileManager messagesFm;
    private final ResourceWorldsManager resourceWorldsManager;

    public ResourceWorldAdminCommand(PearResourceWorld plugin) {
        this.plugin = plugin;
        this.messagesFm = plugin.getMessagesFileManager();
        this.resourceWorldsManager = plugin.getResourceWorldsManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player || sender instanceof ConsoleCommandSender) {
            if (!sender.hasPermission(command.getPermission())) {
                sender.sendMessage(messagesFm.getMessage("no-permission"));
                return false;
            }

            if (args.length == 0) {
                sender.sendMessage("Subcommands: " + String.join(",", subcommands));
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

                        if (resourceWorldsManager.isInResourceWorld(player)) {
                            sender.sendMessage(messagesFm.getMessage("already-resource-world-self"));
                            return false;
                        }

                        if (!resourceWorldsManager.teleportPlayerToResourceWorld(player)) {
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

                    resourceWorldsManager.resetWorlds();
                    return true;

                case "kickall":
                    if (!sender.hasPermission(command.getPermission() + ".kickall")) {
                        sender.sendMessage(messagesFm.getMessage("no-permission"));
                        return false;
                    }

                    resourceWorldsManager.kickAllFromResourceWorld();
                    sender.sendMessage(messagesFm.getMessage("kicked-all-players-from-resource-world"));
                    return true;

                default:
                    sender.sendMessage(messagesFm.getMessage("invalid-subcommand"));
                    return false;
            }
        }

        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        // TODO Auto-generated method stub
        return null;
    }
}
