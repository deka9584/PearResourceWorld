package pear.resourceworld.utils;

import java.lang.reflect.Method;

import org.bukkit.World;

public class NMSWorldUtils {
    public static boolean generateEndExitPortal(World endWorld) {
        try {
            Object craftWorld = endWorld;
            Method getHandleMethod = craftWorld.getClass().getMethod("getHandle");
            Object worldServer = getHandleMethod.invoke(craftWorld);

            Method getWorldProviderMethod = worldServer.getClass().getMethod("getWorldProvider");
            Object worldProvider = getWorldProviderMethod.invoke(worldServer);

            Method getBattleMethod = worldProvider.getClass().getMethod("o");
            Object battle = getBattleMethod.invoke(worldProvider);

            if (battle == null) {
                return false;
            }

            // TO DO: call "private boolean i()" to check if portal is already generated

            Method generatePortalMethod = battle.getClass().getDeclaredMethod("a", boolean.class);
            generatePortalMethod.setAccessible(true);
            generatePortalMethod.invoke(battle, true);
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return false;
    }
}
