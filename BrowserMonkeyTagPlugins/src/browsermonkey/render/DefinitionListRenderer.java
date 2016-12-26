package browsermonkey.render;

import browsermonkey.document.*;
import java.text.AttributedCharacterIterator.Attribute;
import java.util.*;

/**
 * Renders definition list tags from a dl TagDocumentNode and its children.
 * @author Paul Calcraft
 */
public class DefinitionListRenderer extends TagRenderer {
    public DefinitionListRenderer(Linkable linker) {
        super(linker);
    }

    @Override
    public void render(Renderer renderer, TagDocumentNode tag, LayoutRenderNode parent, Map<Attribute, Object> formatting) {
        // Make space for the list above.
        parent.ensureLinespaceDistance(1);

        // For each child...
        for (DocumentNode itemNode : tag.getChildren()) {
            // If not a tag node, render normally.
            if (!(itemNode instanceof TagDocumentNode)) {
                renderer.render(itemNode, parent, formatting);
                continue;
            }
            // If dd, create a layout node and pad with an indent text node to
            // the left.
            if (((TagDocumentNode)itemNode).getType().equals("dd")) {
                LayoutRenderNode itemLayoutNode = new LayoutRenderNode(linker);
                itemLayoutNode.addNodePadding(renderer.constructIndentTextNode(formatting), null);

                // Render dt into padded layout.
                renderer.render(itemNode, itemLayoutNode, formatting);

                // Add to list.
                parent.addNode(itemLayoutNode, LayoutRenderNode.WidthBehaviour.Maximal);
            }
            // Else render normally, but if dt ensure we're on a new line.
            else {
                if (((TagDocumentNode)itemNode).getType().equals("dt"))
                    parent.ensureNewLine();
                renderer.render(itemNode, parent, formatting);
            }
        }

        // Make space for the list below.
        parent.ensureLinespaceDistance(1);
    }
}