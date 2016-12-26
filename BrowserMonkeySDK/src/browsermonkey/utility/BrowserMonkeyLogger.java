package browsermonkey.utility;

import java.util.logging.*;

/**
 * Provides a set of static methods for logging functionality.
 * @author Daniel Cooper, Paul Calcraft
 */
public class BrowserMonkeyLogger {
    private static Logger logger = Logger.getLogger("uk.ac.sussex.browsermonkey");
    private static Logger alertLogger = Logger.getLogger("uk.ac.sussex.browsermonkey.alert");
    private static boolean logOpened = false;

    // Attempts to get a file based LogHandler for the log file.
    private static FileHandler getFile() {
        try {
            FileHandler fh = new FileHandler("BrowserMonkey.log", true);
            fh.setFormatter(new SimpleFormatter());
            return fh;
        } catch (Exception x) {
            return null;
        }
    }

    /**
     * Adds a handler for alerts.
     * @param handler the LogHandler that wants to subscribe to alerts
     */
    public static void addAlertHandler(Handler handler) {
        if (handler == null)
            return;

        try {
            alertLogger.addHandler(handler);
        } catch (SecurityException ex) {
            warning("New alert handler of type "+handler.getClass()+" could not be added due to a Security Exception: "+ex);
        }
    }

    // Tries to open the log file for logging, if it is not already.
    // Returns false if this fails and the log file is not open at the end of
    // this method.
    private static boolean ensureOpen() {
        if (logOpened)
            return true;
        
        FileHandler file = getFile();

        if (file != null) {
            logOpened = true;
            logger.addHandler(file);
            logger.setLevel(Level.ALL);
            
            return true;
        }

        alertLogger.warning("Couldn't open log file.");
        return false;
    }

    /**
     * Submits a status message to the alert logger and main logger.
     * @param status
     */
    public static void status(String status) {
        alertLogger.info(status);
        if (ensureOpen())
            logger.info(status);
    }

    /**
     * Submits a notice to the alert logger and main logger.
     * Notices are handled by a GUI notification dialog in the BrowserMonkey
     * application.
     * @param notice
     */
    public static void notice(String notice) {
        alertLogger.warning(notice);
        if (ensureOpen())
            logger.warning(notice);
    }
    
    /**
     * Logs a conformance issue to the log file.
     * @param string
     */
    public static void conformance(String string) {
        if (ensureOpen())
            logger.warning(string);
    }
    
    /**
     * Logs a warning to the log file.
     * @param warning
     */
    public static void warning(String warning) {
        if (ensureOpen())
            logger.warning(warning);
    }

    /**
     * Logs an information message to the log file.
     * @param info
     */
    public static void info(String info) {
        if (ensureOpen())
            logger.info(info);
    }
}