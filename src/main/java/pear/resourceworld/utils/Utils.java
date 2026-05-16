package pear.resourceworld.utils;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import org.bukkit.Location;

import net.md_5.bungee.api.ChatColor;

public class Utils {
    public static int getRandomInt(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }

    public static boolean isSamePosition(Location loc1, Location loc2) {
        if (loc1 == null || loc2 == null || loc1.getWorld() != loc2.getWorld()) {
            return false;
        }

        return loc1.getX() == loc2.getX()
            && loc1.getY() == loc2.getY()
            && loc1.getZ() == loc2.getZ();
    }

    public static String translateColorCodes(String s) {
        return s != null ? ChatColor.translateAlternateColorCodes('&', s) : null;
    }

    public static List<String> translateColorCodesList(List<String> stringList) {
        return stringList.stream()
            .map(s -> translateColorCodes(s))
            .collect(Collectors.toList());
    }
}