package browsermonkey.render;

/**
 * Handles links in web pages.
 * @author prtc20
 */
public class DocumentLinker implements Linkable {
    private DocumentPanel documentPanel;

    /**
     * Creates a new <code>DocumentLinker</code> and links it to a
     * <code>DocumentPanel</code>.
     * @param documentPanel The <code>DocumentPanel</code> to be linked to
     */
    public DocumentLinker(DocumentPanel documentPanel) {
        this.documentPanel = documentPanel;
    }

    /**
     * Code that executes when links are clicked. Follows a link to the supplied
     * path.
     * @param path Path supplied to follow the link to
     */
    public void followLink(String path) {
        documentPanel.load(path);
    }
}