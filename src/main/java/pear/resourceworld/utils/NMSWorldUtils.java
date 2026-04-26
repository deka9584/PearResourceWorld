package pear.resourceworld.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.bukkit.World;

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
}
