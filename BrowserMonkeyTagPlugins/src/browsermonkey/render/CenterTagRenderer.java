package browsermonkey.render;

import browsermonkey.document.*;
import java.text.AttributedCharacterIterator.Attribute;
import java.util.*;

/**
 * Renders center tags from center TagDocumentNodes.
 * @author Paul Calcraft
 */
public class CenterTagRenderer extends TagRenderer {
    public CenterTagRenderer(Linkable linker) {
        super(linker);
    }

    @Override
    public void render(Renderer renderer, TagDocumentNode tag, LayoutRenderNode parent, Map<Attribute, Object> formatting) {
        // Create a div layout node to act as the block, with centred = true.
        LayoutRenderNode div = new LayoutRenderNode(linker, true);
        // Render all children into the block div using the Renderer and the
        // existing formatting.
        for (DocumentNode child : tag.getChildren())
            renderer.render(child, div, formatting);

        // Grow the div so it centres in the middle of all available space.
        parent.addNode(div, LayoutRenderNode.WidthBehaviour.Grow);
    }
}