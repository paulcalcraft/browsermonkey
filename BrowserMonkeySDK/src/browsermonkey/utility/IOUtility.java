package browsermonkey.utility;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * Provides static methods for resolving addresses into URLs and reading files
 * locally or from the internet.
 * @author Paul Calcraft
 */
public class IOUtility {
    // Tries to find an appropriate index file for a given local directory.
    private static File getIndexFile(File directory) {
        // Use FilenameFilter to select index.htm and index.html files.
        String[] indexFiles = directory.list(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.equalsIgnoreCase("index.html") || name.equalsIgnoreCase("index.htm");
            }
        });
        // Return the first one we find, else null.
        if (indexFiles.length > 0)
            return new File(indexFiles[0]);
        return null;
    }

    // Given a local valid file path, return a URL object for it.
    private static URL resolveFile(File file) {
        try {
            if (file.isDirectory())
                file = getIndexFile(file);
            else if (!file.exists())
                file = null;

            if (file != null) {
                return file.toURI().toURL();
            }
            else
                return null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Given an address and the context (referrer URL/current directory), try to
     * create a URL object.
     * @param path
     * @param context
     * @return
     */
    public static URL getURL(String path, URL context) {
        URL result;
        // If there's no context, first try to parse as a local file.
        if (context == null) {
            result = resolveFile(new File(path));
            if (result != null)
                return result;
        }

        // Try to use standard URL handling to obtain the new URL.
        try {
            result = new URL(context, path);
        } catch (MalformedURLException ex) {
            return null;
        }

        // If we've found a file, try to resolve it in case it's a directory.
        if (result.getProtocol().equals("file")) {
            try {
                result = resolveFile(new File(result.toURI().getPath()));
            } catch (Exception e) {
                result =  null;
            }
        }

        return result;
    }

    /**
     * Tries to read a URL (local or internet) into a byte array and returns the
     * data, or null if there was an error. The error code (e.g. 404) is stored
     * in an out parameter.
     * @param url the URL to attempt to open from
     * @param outErrorCode pseudo out parameter, should always have one element
     * @return
     */
    public static byte[] readFile(URL url, int[] outErrorCode) {
        if (url == null) {
            // URL not found/parsed.
            outErrorCode[0] = 404;
            return null;
        }
        
        InputStream urlStream;
        URLConnection connection;
        ArrayList<Byte> data = new ArrayList<Byte>();
        try {
            connection = url.openConnection();
            // Pretend we're Mozilla so websites don't think we're an automated
            // bot or anything.
            connection.setRequestProperty("User-agent", "Mozilla/5.0");
            // Get the input stream from the connection.
            urlStream = connection.getInputStream();

            // Log to the status bar that we're loading the file.
            BrowserMonkeyLogger.status("Loading "+url.toString());
            // Yield to give the UI thread a chance to update.
            Thread.yield();

            // Read all bytes from the stream into our ArrayList.
            int b;
            while ((b = urlStream.read()) != -1)
                data.add((byte)b);

        } catch (SocketTimeoutException ex) {
            // If timeout, set error code to 408.
            outErrorCode[0] = 408;
            return null;
        }
        catch (IOException ex) {
            // If any other, assume file not found.
            outErrorCode[0] = 404;
            return null;
        }
        try {
            urlStream.close();
        } catch (IOException ex) {
            // Can't close, needn't be reported.
        }

        // Copy to a new byte array for returning.
        byte[] byteData = new byte[data.size()];
        for (int i = 0; i < byteData.length; i++)
            byteData[i] = data.get(i);

        return byteData;
    }
}