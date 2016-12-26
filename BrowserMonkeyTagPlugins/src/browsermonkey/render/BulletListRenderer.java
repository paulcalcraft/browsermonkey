package browsermonkey.render;

/**
 * Renders unordered lists from an ul TagDocumentNode and its children using
 * ListTagRenderer as a base.
 * @author Paul Calcraft
 */
public class BulletListRenderer extends ListTagRenderer{
    public BulletListRenderer(Linkable linker) {
        super(linker);
    }

    /**
     * Returns a (non-breaking) space indent and a bullet point for every
     * element.
     * @param index ignored - the index of the element in the list
     * @return
     */
    @Override
    protected String getListElementText(int index) {
        return Renderer.STANDARD_INDENT+"•&nbsp;";
    }
}