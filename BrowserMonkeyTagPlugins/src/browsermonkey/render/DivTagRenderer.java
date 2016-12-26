package browsermonkey.render;

import browsermonkey.document.*;
import java.text.AttributedCharacterIterator.Attribute;
import java.util.*;

/**
 * Renders unstyled div tags from div TagDocumentNodes.
 * @author Paul Calcraft
 */
public class DivTagRenderer extends TagRenderer {
    public DivTagRenderer(Linkable linker) {
        super(linker);
    }

    @Override
    public void render(Renderer renderer, TagDocumentNode tag, LayoutRenderNode parent, Map<Attribute, Object> formatting) {
        // Create a layout node div and render content into it.
        // This is not true div support, but allows websites with divs to at
        // least have their content separated by them.
        LayoutRenderNode div = new LayoutRenderNode(linker, false);
        for (DocumentNode child : tag.getChildren())
            renderer.render(child, div, formatting);
        parent.addNode(div, LayoutRenderNode.WidthBehaviour.Maximal);
    }
}