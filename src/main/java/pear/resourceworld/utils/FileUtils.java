package pear.resourceworld.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.stream.Stream;

public class FileUtils {
    public static boolean copyDirectory(Path source, Path target) {
        try (Stream<Path> stream = Files.walk(source)) {
            stream.forEach(path -> {
                try {
                    Path relative = source.relativize(path);
                    Path destination = target.resolve(relative);

                    if (Files.isDirectory(path)) {
                        Files.createDirectories(destination);
                    } else {
                        Files.copy(path, destination, StandardCopyOption.REPLACE_EXISTING);
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            });

            return true;
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return false;
    }

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
