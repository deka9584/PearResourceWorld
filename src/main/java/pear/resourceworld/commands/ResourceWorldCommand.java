package pear.resourceworld.commands;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import pear.resourceworld.PearResourceWorld;

public class ResourceWorldCommand implements CommandExecutor, TabCompleter {
    private final PearResourceWorld plugin;
    
    public ResourceWorldCommand(PearResourceWorld plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return null;
    }

    
    
}
