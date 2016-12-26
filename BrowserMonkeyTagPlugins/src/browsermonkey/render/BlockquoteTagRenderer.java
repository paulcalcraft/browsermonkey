package browsermonkey.render;

import browsermonkey.document.*;
import java.text.AttributedCharacterIterator.Attribute;
import java.util.*;

/**
 * Renders block quote tags from block quote TagDocumentNodes.
 * @author Paul Calcraft
 */
public class BlockquoteTagRenderer extends TagRenderer {
    public BlockquoteTagRenderer(Linkable linker) {
        super(linker);
    }

    @Override
    public void render(Renderer renderer, TagDocumentNode tag, LayoutRenderNode parent, Map<Attribute, Object> formatting) {
        // Create a div layout node to act as the block.
        LayoutRenderNode div = new LayoutRenderNode(linker);
        // Create left and right indent nodes.
        TextRenderNode leftNode = renderer.constructIndentTextNode(formatting);
        TextRenderNode rightNode = renderer.constructIndentTextNode(formatting);
        // Pad the block with the indent nodes.
        div.addNodePadding(leftNode, rightNode);

        // Render all children into the block div using the Renderer and the
        // existing formatting.
        for (DocumentNode child : tag.getChildren())
            renderer.render(child, div, formatting);

        // Ensure there is at least one line space from the previous element.
        parent.ensureLinespaceDistance(1);
        // Add the div to the rendering parent.
        parent.addNode(div, LayoutRenderNode.WidthBehaviour.Maximal);
        // Ensure there is at least one line space before the next element.
        parent.ensureLinespaceDistance(1);
    }
}