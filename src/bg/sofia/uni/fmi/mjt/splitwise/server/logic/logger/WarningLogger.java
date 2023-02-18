package bg.sofia.uni.fmi.mjt.splitwise.server.logic.logger;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class WarningLogger {
    public static final String FILE_PATH = "warnings.log";
    private static final String LOGGER_NAME = "WarningLogger";

    private static Logger logger;

    public static void log(String message) {
        if (logger == null) {
            logger = createLogger();
        }

        logger.warning(message);
    }

    private static Logger createLogger() {
        Logger logger = Logger.getLogger(LOGGER_NAME);

        logger.setUseParentHandlers(false);
        logger.setLevel(Level.WARNING);

        FileHandler fh;

        try {
            fh = new FileHandler(FILE_PATH, true);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not create logger", e);
        }

        logger.addHandler(fh);

        SimpleFormatter formatter = new SimpleFormatter();
        fh.setFormatter(formatter);

        return logger;
    }
}
