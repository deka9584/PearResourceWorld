package pear.resourceworld.utils;

import java.util.concurrent.ThreadLocalRandom;

import net.md_5.bungee.api.ChatColor;

public class Utils {
    public static int getRandomInt(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }

    public static String translateColorCodes(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }
}
