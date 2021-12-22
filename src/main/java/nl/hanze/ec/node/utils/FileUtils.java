package nl.hanze.ec.node.utils;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileUtils {
    private static final Logger logger = LogManager.getLogger(FileUtils.class);

    /**
     * Reads a file from the resource folder
     *
     * @param path location of the file in the resource folder
     * @return content of file
     */
    public synchronized static String readFromResources(String path) {
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();

        try {
            if (classLoader.getResource(path) == null) {
                return "";
            }

            File file = new File(classLoader.getResource(path).getFile());

            if (file.exists()) {
                return new String(Files.readAllBytes(file.toPath()));
            }
        } catch (IOException e) {
            logger.debug("Could not find file '" + path + "' in resources folder");
        }

        return "";
    }

    public synchronized static boolean writeToResources(String path, Object object) {
        try {
            path = "src/main/resources/secret" + path;
            File file = new File(path);

            if (!file.createNewFile()) {
                return false;
            }

            FileWriter myWriter = new FileWriter(path);
            myWriter.write(object.toString());
            myWriter.close();
        } catch (IOException e) {
            logger.debug("Could not write file '" + path + "' to resources folder");
        }

        return true;
    }
}
