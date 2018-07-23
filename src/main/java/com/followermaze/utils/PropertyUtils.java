package com.followermaze.utils;

import com.followermaze.Application;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Property file reader.
 */
public class PropertyUtils {
    private static final String CONFIG_FILENAME = "config.properties";
    private static final Logger logger = Logger.getLogger(PropertyUtils.class);

    public static Properties getProperties() {
        try (InputStream input = Application.class.getClassLoader().getResourceAsStream(CONFIG_FILENAME)) {
            logger.info("Reading properties file");
            Properties prop = new Properties();
            if (input == null) {
                logger.error("Unable to find file: "+CONFIG_FILENAME);
            }
            //load a properties file from class path, inside static method
            prop.load(input);
            return prop;

        } catch (IOException ex) {
            logger.error("Error reading configuration file");
        }
        return null;
    }
}
