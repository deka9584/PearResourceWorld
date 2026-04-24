package pear.resourceworld.commands;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import pear.resourceworld.PearResourceWorld;
import pear.resourceworld.managers.MessagesFileManager;

public class ResourceWorldCommand implements CommandExecutor, TabCompleter {
    private final PearResourceWorld plugin;
    private final MessagesFileManager messagesFm;
    
    public ResourceWorldCommand(PearResourceWorld plugin) {
        this.plugin = plugin;
        this.messagesFm = plugin.getMessagesFileManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            if (!sender.hasPermission(command.getPermission())) {
                sender.sendMessage(messagesFm.getMessage("no-permission"));
                return false;
            }

            if (sender instanceof Player) {
                Player player = (Player) sender;
                plugin.getTeleportHelper().teleportPlayerToResourceWorld(player);
                return true;
            }
        }

        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return null;
    }

    
    
}
