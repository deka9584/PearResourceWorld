package pear.resourceworld.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import pear.resourceworld.PearResourceWorld;
import pear.resourceworld.managers.MessagesFileManager;

public class ResourceWorldCommand implements CommandExecutor, TabCompleter {
    private static final String[] SUBCOMMANDS = {
        "spawn"
    };

    private final PearResourceWorld plugin;
    private final MessagesFileManager messagesFm;
    
    public ResourceWorldCommand(PearResourceWorld plugin) {
        this.plugin = plugin;
        this.messagesFm = plugin.getMessagesFileManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;

            if (!player.hasPermission(command.getPermission())) {
                player.sendMessage(messagesFm.getMessage("no-permission"));
                return false;
            }

            if (args.length == 0) {
                plugin.getTeleportHelper().teleportToRwOverworld(player);
                return true;
            }

            switch (args[0]) {
                case "spawn":
                    if (!player.hasPermission(command.getPermission() + ".spawn")) {
                        player.sendMessage(messagesFm.getMessage("no-permission"));
                        return false;
                    }

                    if (!plugin.getResourceWorldsManager().isResourceWorld(player.getWorld())) {
                        player.sendMessage(messagesFm.getMessage("not-in-resource-world-self"));
                        return true;
                    }

                    plugin.getTeleportHelper().teleportToSpawn(player);
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
        List<String> completeSubCommand = new ArrayList<>();

        if (args.length == 1) {
            return StringUtil.copyPartialMatches(args[0], Arrays.asList(SUBCOMMANDS), completeSubCommand);
        }

        return completeSubCommand;
    }
}
