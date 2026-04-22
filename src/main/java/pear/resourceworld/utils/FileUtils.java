package pear.resourceworld.utils;

import java.io.File;

public class FileUtils {
    public static boolean existDirectory(String parentPath, String dirName) {
        File file = new File(parentPath, dirName);
        return file.exists() && file.isDirectory();
    }

    public static boolean deleteDirectory(File directory) {
        if (directory.exists() && directory.isDirectory()) {
            File[] files = directory.listFiles();

            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteDirectory(file);
                    } else {
                        file.delete();
                    }
                }
            }

            return directory.delete();
        }

        return false;
    }
}
