package buythedip.auxiliary;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class LoggerSingleton {

    private static volatile Logger logger;

    public static Logger getInstance() {

        if (logger == null) {
            synchronized (LoggerSingleton.class) {
                if (logger == null) {
                    logger = LogManager.getLogger(LoggerSingleton.class);
                }
            }
        }

        return logger;
    }

}
