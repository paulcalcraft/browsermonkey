package browsermonkey;

/**
 * Main method used to initialise the program.
 * @author Paul Calcraft
 */
public class Main {
    /**
     * Sets up the GUI and allows you to specify a file to be run straight away.
     * @param args Enter a filepath here to load the browser with that file
     */
    public static void main(String[] args) {
        GUI browser = new GUI();
        String initialFile = ".";
        if (args.length > 0) {
            initialFile = args[0];
        }
        browser.loadFile(initialFile);
        browser.setVisible(true);
    }
}