package pear.resourceworld.utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class NMSWorldUtils {
    public static boolean generateEndExitPortal(World endWorld, boolean setDragonKilled) {
        try {
            Object craftWorld = endWorld;
            Method getHandleMethod = craftWorld.getClass().getMethod("getHandle");
            Object worldServer = getHandleMethod.invoke(craftWorld);

            Object battle = null;
            boolean hasWorldProvider = false;

            for (Method m : worldServer.getClass().getMethods()) {
                if (m.getName().equals("getWorldProvider")) {
                    hasWorldProvider = true;
                }

                if (m.getParameterCount() == 0 && m.getReturnType().getSimpleName().equals("EnderDragonBattle")) {
                    battle = m.invoke(worldServer);
                    break;
                }
            }

            if (hasWorldProvider && battle == null) {
                Method getWorldProviderMethod = worldServer.getClass().getMethod("getWorldProvider");
                Object worldProvider = getWorldProviderMethod.invoke(worldServer);

                for (Method m : worldProvider.getClass().getMethods()) {
                    if (m.getParameterCount() == 0 && m.getReturnType().getSimpleName().equals("EnderDragonBattle")) {
                        battle = m.invoke(worldProvider);
                        break;
                    }
                }
            }

            if (battle == null) {
                return false;
            }

            if (setDragonKilled) {
                for (Field f : battle.getClass().getDeclaredFields()) {
                    if (f.getType() == boolean.class) {
                        String name = f.getName().toLowerCase();
    
                        if (name.equals("dragonkilled") || name.equals("k") ||
                            name.equals("previouslykilled") || name.equals("l")) {
                            f.setAccessible(true);
                            f.setBoolean(battle, true);
                        }
                    }
                }
            }

            for (Method m : battle.getClass().getDeclaredMethods()) {
                String name = m.getName().toLowerCase();

                if (name.equalsIgnoreCase("generateExitPortal") || name.equals("a")) {
                    if (m.getParameterCount() == 1 && m.getParameterTypes()[0] == boolean.class) {
                        m.setAccessible(true);
                        m.invoke(battle, true);
                        break;
                    }
                }
            }

            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return false;
    }

    public static boolean skipEndCredits(Player player) {
        try {
            String version = getServerVersion();
            Object craftPlayer = player;

            Method getHandle = craftPlayer.getClass().getMethod("getHandle");
            Object entityPlayer = getHandle.invoke(craftPlayer);

            Field pcField = entityPlayer.getClass().getField("playerConnection");
            Object playerConnection = pcField.get(entityPlayer);

            Class<?> packetClass = Class.forName(
                "net.minecraft.server." + version +
                ".PacketPlayInClientCommand"
            );

            Class<?> enumClass = Class.forName(
                "net.minecraft.server." + version +
                ".PacketPlayInClientCommand$EnumClientCommand"
            );

            Object performRespawn = Enum.valueOf((Class<Enum>) enumClass, "PERFORM_RESPAWN");

            Constructor<?> ctor = packetClass.getConstructor(enumClass);
            Object packet = ctor.newInstance(performRespawn);

            for (Method m : playerConnection.getClass().getMethods()) {
                String name = m.getName();

                if (name.equals("sendPacket") || name.equals("a")) {
                    if (m.getParameterCount() == 1 && m.getParameterTypes()[0] == packetClass) {
                        m.invoke(playerConnection, packet);
                        break;
                    }
                }
            }

            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return false;
    }

    public static String getServerVersion() {
        return Bukkit.getServer()
            .getClass()
            .getPackage()
            .getName()
            .split("\\.")[3];
    }
}
