package nl.hanze.ec.node.utils;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;

public class FileUtils {
    private static final Logger logger = LogManager.getLogger(FileUtils.class);

    /**
     * Read a file from the resource folder
     *
     * @param path location of the file in the resource folder
     * @return content of file
     */
    public static String readFromResources(String path) {
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();

        try {
            File file = new File(classLoader.getResource(path).getFile());

            if (file.exists()) {
                return new String(Files.readAllBytes(file.toPath()));
            }
        } catch (Exception e) {
            logger.debug("Could not find file '" + path + "' in resources folder");
        }

        return "";
    }
}
