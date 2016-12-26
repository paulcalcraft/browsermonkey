package browsermonkey.render;

/**
 * Renders ordered lists from an ol TagDocumentNode and its children using
 * ListTagRenderer as a base.
 * @author Paul Calcraft
 */
public class OrderedListRenderer extends ListTagRenderer{
    public OrderedListRenderer(Linkable linker) {
        super(linker);
    }

    /**
     * Returns a (non-breaking) space indent and the list item index.
     * @param index the zero-based index of the element in the list
     * @return
     */
    @Override
    protected String getListElementText(int index) {
        // Add 1 to index as it is zero-based.
        return Renderer.STANDARD_INDENT+(index+1)+".&nbsp;";
    }
}