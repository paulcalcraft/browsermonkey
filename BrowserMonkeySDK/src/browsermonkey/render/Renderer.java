package browsermonkey.render;

import java.util.*;
import browsermonkey.document.*;
import browsermonkey.utility.*;
import java.text.AttributedCharacterIterator.Attribute;
import java.awt.font.*;
import java.io.*;
import java.lang.reflect.*;
import java.net.*;

/**
 * Renders documents from DocumentNode trees.
 * @author Paul Calcraft
 */
public class Renderer {
    // The current context of the document, used for loading linked resources.
    private URL documentContext;
    // The map of TagRenderer objects for rendering TagDocumentNodes.
    private Map<String, TagRenderer> rendererMap;
    private Linkable linker;
    // The title of the document rendered.
    private String title = null;
    // The heading numbering for the document.
    private ArrayList<Integer> headingNumbering;
    // Whether a conformance error has been discovered during rendering.
    private boolean foundConformanceError;
    private final TagRenderer unrecognisedTagRenderer;
    // The default set of attributes for text formatting.
    public static final Map<Attribute,Object> DEFAULT_FORMATTING;
    // Standard indent (used for things like blockquote, list indentation).
    public static final String STANDARD_INDENT =
            "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";

    // Statically build the default formatting attribute map as Times New Roman
    // 12pt.
    static {
        DEFAULT_FORMATTING = new HashMap<Attribute,Object>();
        DEFAULT_FORMATTING.put(TextAttribute.SIZE, 12f);
        DEFAULT_FORMATTING.put(TextAttribute.FAMILY, "Times New Roman");
    }

    /**
     * Constructs a new Renderer with the specified linker and loads all the
     * TagRenderer objects.
     * @param linker
     */
    public Renderer(Linkable linker) {
        this.linker = linker;
        headingNumbering = new ArrayList<Integer>();
        // Render unrecognised tags with the TransparentTagRenderer to render
        // their contents into the existing parent.
        unrecognisedTagRenderer = new TransparentTagRenderer(linker);
        loadRenderers();
    }

    /**
     * Gets a String representing a heading in the document for the specified
     * level.
     * @param headingLevel the zero-based heading level
     * @return a String representing the current level in this document
     */
    public String getHeadingString(int headingLevel) {
        // If the level is beyond the current heading numbering, add 1s between
        // the deepest heading, and this new level.
        if (headingLevel >= headingNumbering.size()) {
            for (int i = headingNumbering.size(); i <= headingLevel; i++)
                headingNumbering.add(1);
        }
        // Else, increment the heading number at this level and remove any
        // sub-level headings.
        else {
            headingNumbering.set(headingLevel, headingNumbering.get(headingLevel)+1);
            for (int i = headingNumbering.size()-1; i > headingLevel; i--)
                headingNumbering.remove(i);
        }

        // Build the String to return.
        StringBuilder headingString = new StringBuilder();

        for (Integer i : headingNumbering) {
            headingString.append(i);
            headingString.append('.');
        }

        return headingString.toString();
    }

    /**
     * Sets the state of the renderer to indicate a conformance error has been
     * found.
     */
    public void foundConformanceError() {
        foundConformanceError = true;
    }

    /**
     * Returns whether the document seems conformant after rendering.
     * @return
     */
    public boolean isConformant() {
        return !foundConformanceError;
    }

    /**
     * Constructs a new indent text node with the specified formatting.
     * @param formatting
     * @return
     */
    public TextRenderNode constructIndentTextNode(Map<Attribute,Object> formatting) {
        TextRenderNode result = new TextRenderNode(linker);
        result.addText(STANDARD_INDENT, formatting);
        return result;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Load a resource from the specified path for the current document context.
     * @param path
     * @return
     */
    public byte[] loadResource(String path) {
        // We don't care about what response we get, just return the data (or
        // null).
        int[] response = new int[1];
        return IOUtility.readFile(IOUtility.getURL(path, documentContext), response);
    }

    // Loads the set of TagRenderers to render TagDocumentNodes.
    private void loadRenderers() {
        rendererMap = new HashMap<String, TagRenderer>();

        Properties rendererMapProperties = new Properties();
        try {
            // Load the properties file to map tag type strings to TagRenderer
            // classes.
            FileInputStream in = new FileInputStream("tagRenderers.properties");
            rendererMapProperties.load(in);

            // Find all .jar files in the plugins directory.
            File pluginsDirectory = new File("plugins/");
       
            String[] pluginJARs = pluginsDirectory.list(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return (name.endsWith(".jar"));
                }
            });

            // Build a URL array for the .jar files and a String for logging.
            String jarFiles = "";
            URL[] urls = new URL[pluginJARs.length];
            for (int i = 0; i < pluginJARs.length; i++) {
                urls[i] = new File("plugins/"+pluginJARs[i]).toURI().toURL();
                jarFiles += "\""+urls[i]+"\"";
                if (i != pluginJARs.length-1)
                    jarFiles += ", ";
            }

            // Log the list of .jar files we're using to load renderers from.
            BrowserMonkeyLogger.info("Loading tag plugins from: "+jarFiles);

            // Construct a new ClassLoader for found URLs.
            ClassLoader pluginClassLoader = new URLClassLoader(urls);

            // Retrieve the set of mappings from the loaded properties file.
            Set<Map.Entry<Object, Object>> rendererMappings = rendererMapProperties.entrySet();

            // Load and construct TagRenderer objects for all mapped renderers.
            // While counting how many there are and how many loaded.
            int rendererCount = 0;
            int loadedCount = 0;

            for (Map.Entry<Object, Object> entry : rendererMappings) {
                rendererCount++;
                try {
                    // Load the class from the .jar files with the specified
                    // name in the properties.
                    Class rendererClass = pluginClassLoader.loadClass(entry.getValue().toString());
                    // Create the constructor signature parameters.
                    Class parameterTypes[] = new Class[] { Linkable.class };
                    // Get the constructor object.
                    Constructor ctor = rendererClass.getConstructor(parameterTypes);
                    // Create a new instance using the current linker.
                    Object arguments[] = new Object[] { this.linker };
                    Object newInstance = ctor.newInstance(arguments);
                    // Cast to a TagRenderer.
                    TagRenderer tagRenderer = TagRenderer.class.cast(newInstance);

                    // If everything worked, add the renderer to the map for the
                    // tag type specified in the properties entry.
                    if (tagRenderer != null) {
                        rendererMap.put(entry.getKey().toString(), tagRenderer);
                        loadedCount++;
                    }
                    
                } catch (ClassNotFoundException ex) {
                    BrowserMonkeyLogger.warning("TagRenderer class could not be found in the plugins folder.");
                } catch (Exception ex) {
                    BrowserMonkeyLogger.warning("TagRenderer class could not be instantiated: "+ex);
                }
            }

            BrowserMonkeyLogger.info(loadedCount+"/"+rendererCount+" TagRenderers loaded successfully.");
        } catch (IOException ex) {
            BrowserMonkeyLogger.notice("Could not read tagRenderers.properties: "+ex);
        }
    }

    /**
     * Render a new document with the specified root node, zoom level, and
     * context for resource loading.
     * @param root
     * @param zoom
     * @param documentContext
     * @return a new LayoutRenderNode that is the document body
     */
    public LayoutRenderNode renderRoot(DocumentNode root, float zoom, URL documentContext) {
        this.documentContext = documentContext;

        // Reset all document properties.
        headingNumbering.clear();
        title = null;
        foundConformanceError = false;

        // Create the body root node.
        LayoutRenderNode renderRoot = new LayoutRenderNode(linker);
        // Render the root into it, specifying default formatting.
        render(root, renderRoot, DEFAULT_FORMATTING);
        // Set the zoom level.
        renderRoot.setZoomLevel(zoom);
        
        return renderRoot;
    }

    /**
     * Renders the given node into the specified parent layout node with the
     * specified formatting.
     * @param node
     * @param parent
     * @param formatting
     */
    public void render(DocumentNode node, LayoutRenderNode parent, Map<Attribute,Object> formatting) {
        // If text node, add the text to the parent's text accumulation.
        if (node instanceof TextDocumentNode) {
            TextDocumentNode textNode = (TextDocumentNode)node;
            parent.getTextNode().addText(textNode.getText(), formatting);
        }
        // Else render the node with the appropriate TagRenderer.
        else {
            TagDocumentNode tagNode = (TagDocumentNode)node;
            getTagRenderer(tagNode).render(this, tagNode, parent, formatting);
        }
    }

    // Gets the TagRenderer instance for the tag node from the mapping.
    // If there isn't one, uses the unrecognisedTagRenderer, as set in the
    // constructor.
    private TagRenderer getTagRenderer(TagDocumentNode tagNode) {
        TagRenderer renderer = rendererMap.get(tagNode.getType());
        if (renderer == null)
            return unrecognisedTagRenderer;
        return renderer;
    }
}