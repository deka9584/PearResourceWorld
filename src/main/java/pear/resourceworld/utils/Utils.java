package pear.resourceworld.utils;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import net.md_5.bungee.api.ChatColor;

public class Utils {
    public static int getRandomInt(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }

    public static String translateColorCodes(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }

    public static List<String> translateColorCodesList(List<String> stringList) {
        return stringList.stream()
            .map(s -> translateColorCodes(s))
            .collect(Collectors.toList());
    }
}
