package browsermonkey.render;

import browsermonkey.document.*;
import browsermonkey.utility.BrowserMonkeyLogger;
import java.util.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.font.TextAttribute;
import java.io.File;
import java.net.*;
import java.text.*;
import java.util.ArrayList;
import javax.swing.*;

/**
 * A GUI component for laying out the JComponents to render a
 * <code>DocumentNode</code> tree.
 * @author Paul Calcraft
 */
public class DocumentPanel extends JPanel {
    private Document document;
    private String requestURL;
    private String title;
    private GroupLayout layout;
    private GroupLayout.ParallelGroup horizontalGroup;
    private GroupLayout.SequentialGroup verticalGroup;
    private float zoomLevel = 1.0f;
    private RenderNode rootRenderNode;
    private Renderer renderer;
    private URL context;
    private LoaderThread currentLoaderThread = null;

    /**
     * Getter for the String that holds the title of the page.
     * @return String containing the page title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Constructs a <code>DocumentPanel</code> by initialising the layout groups.
     */
    public DocumentPanel() {
        this.setBackground(Color.white);

        try {
            context = new File(System.getProperty("user.dir")).toURI().toURL();
        } catch (MalformedURLException ex) {
            context = null;
        }

        layout = new GroupLayout(this);
        this.setLayout(layout);

        GroupLayout.SequentialGroup horizontalIndentLayout = layout.createSequentialGroup();
        horizontalIndentLayout.addGap(8);
        horizontalGroup = layout.createParallelGroup(GroupLayout.Alignment.LEADING);
        horizontalIndentLayout.addGroup(horizontalGroup);
        horizontalIndentLayout.addGap(8);
        layout.setHorizontalGroup(horizontalIndentLayout);

        GroupLayout.SequentialGroup verticalIndentLayout = layout.createSequentialGroup();
        verticalIndentLayout.addGap(8);
        verticalGroup = layout.createSequentialGroup();
        verticalIndentLayout.addGroup(verticalGroup);
        verticalIndentLayout.addGap(8);
        layout.setVerticalGroup(verticalIndentLayout);

        renderer = new Renderer(new DocumentLinker(this));
    }
    
    private class LoaderThread extends SwingWorker<Void, Integer> {
        private String path;
        private boolean absolute;

        public LoaderThread(String path, boolean absolute) {
            this.path = path;
            this.absolute = absolute;
        }
        
        @Override
        protected Void doInBackground() {
            if (absolute) {
                try {
                    context = new File(System.getProperty("user.dir")).toURI().toURL();
                } catch (MalformedURLException ex) {
                    context = null;
                }
                context = null;
            }

            document = new Document(path, context);
            if (path.startsWith("t "))
                document.loadTest(path.substring(2));
            else {
                document.load();
                context = document.getURL();
            }

            /*
             * DEBUG, DISABLED
             * Store the html output into the clipboard for debug.
            try {
                java.awt.datatransfer.Clipboard systemClipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                java.awt.datatransfer.Transferable transferableText = new java.awt.datatransfer.StringSelection(document.getNodeTree().toDebugString());
                systemClipboard.setContents(transferableText, null);
            } catch (IllegalStateException ex) {
                BrowserMonkeyLogger.warning("Couldn't write debug parse information to clipboard.");
            }
            */
            
            rootRenderNode = renderer.renderRoot(document.getNodeTree(), zoomLevel, context);

            removeAll();
            verticalGroup.addComponent(rootRenderNode);
            horizontalGroup.addComponent(rootRenderNode);

            title = renderer.getTitle();

            changed();
            revalidate();
            repaint();

            Thread.yield();
            
            if (document.getError() != 0) {
                BrowserMonkeyLogger.status("Could not retrieve document.");
            }
            else if (document.isConformant() && renderer.isConformant())
                BrowserMonkeyLogger.status("Done, page appears to conform to the specification.");
            else
                BrowserMonkeyLogger.status("Done, page does not conform to the specification. See log file for details.");

            currentLoaderThread = null;

            return null;
        }
    }

    /**
     * Loads the page at a given path.
     * @param path Path to load the page from
     */
    public void load(String path) {
        load(path, false);
    }

    /**
     * Allows loading of an absolute path.
     * @param path Path to load page from
     * @param absolute Whether the path is absolute
     */
    public void load(String path, boolean absolute) {
        requestURL = path;
        if (currentLoaderThread != null)
            currentLoaderThread.cancel(true);
        currentLoaderThread = new LoaderThread(path, absolute);
        currentLoaderThread.execute();
    }

    /**
     * Getter for retreieving the URL of the current page as a string.
     * @return URL of the current page as string
     */
    public String getAddress() {
        URL url = document.getURL();
        if (url == null)
            return requestURL;
        return url.toString();
    }

    /**
     * Used to control the zoom level of a page, sets it to a supplied level.
     * @param zoomLevel The level to set the zoom to
     */
    public void setZoomLevel(float zoomLevel) {
        rootRenderNode.setZoomLevel(zoomLevel);
        this.zoomLevel = zoomLevel;
        revalidate();
        repaint();
    }

    /**
     * Used to perform searches on a page. Searches the page for a supplied term
     * then highlights all instances of that term on the page and reloads it to
     * show the highlights. Updates the foundStatus variable to allow output if
     * no items could be found.
     * @param term Term to be searched for as a string.
     */
    public void setSearch(String term) {
        ArrayList<AttributedString> textRanges = new ArrayList<AttributedString>();
        Map<AttributedCharacterIterator.Attribute, Object> highlightAttributes = new HashMap<AttributedCharacterIterator.Attribute, Object>();
        highlightAttributes.put(TextAttribute.BACKGROUND, new Color(0x38D878));
        rootRenderNode.extractTextInto(textRanges);
        int resultCount = Searcher.highlightSearchTerm(textRanges.toArray(new AttributedString[textRanges.size()]), term, highlightAttributes);
        String foundStatus;
        switch (resultCount) {
            case 0:
                foundStatus = "Could not find \""+term+"\" in the document.";
                break;
            case 1:
                foundStatus = "Found \""+term+"\" once in the document.";
                break;
            default:
                foundStatus = "Found \""+term+"\" "+resultCount+" times in the document.";
        }
        BrowserMonkeyLogger.status(foundStatus);
        revalidate();
        repaint();
    }

    private ArrayList<ChangeListener> changeListeners = new ArrayList<ChangeListener>();

    /**
     * Adds a ChangeListener to the panel.
     * @param listener
     */
    public void addChangeListener(ChangeListener listener) {
        changeListeners.add(listener);
    }

    /**
     * Removes a ChangeListener from the panel.
     * @param listener
     */
    public void removeChangeListener(ChangeListener listener) {
        changeListeners.remove(listener);
    }

    /**
     * Alerts the ChangeListeners that the panel has changed.
     */
    protected void changed() {
        for (ChangeListener listener : changeListeners)
            listener.stateChanged(new ChangeEvent(this));
    }
}