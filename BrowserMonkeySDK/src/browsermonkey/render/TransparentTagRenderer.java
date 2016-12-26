package browsermonkey.render;

import browsermonkey.document.*;
import java.text.AttributedCharacterIterator.Attribute;
import java.util.Map;

/**
 * Renders a TagDocumentNode's children into the document in place; the effect
 * of the node itself is transparent.
 * @author Paul Calcraft
 */
public class TransparentTagRenderer extends TagRenderer {
    public TransparentTagRenderer(Linkable linker) {
        super(linker);
    }

    @Override
    public void render(Renderer renderer, TagDocumentNode tag, LayoutRenderNode parent, Map<Attribute, Object> formatting) {
        for (DocumentNode child : tag.getChildren())
            renderer.render(child, parent, formatting);
    }
}