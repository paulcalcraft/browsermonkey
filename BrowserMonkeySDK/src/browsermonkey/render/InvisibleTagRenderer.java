package browsermonkey.render;

import browsermonkey.document.*;
import java.text.AttributedCharacterIterator.Attribute;
import java.util.Map;

/**
 * Does not render anything into the document for passed nodes or their
 * children. Used e.g. to hide the contents of script tags.
 * @author Paul Calcraft
 */
public class InvisibleTagRenderer extends TagRenderer {
    public InvisibleTagRenderer(Linkable linker) {
        super(linker);
    }

    @Override
    public void render(Renderer renderer, TagDocumentNode tag, LayoutRenderNode parent, Map<Attribute, Object> formatting) {
        // Do nothing.
    }
}