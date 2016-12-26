package browsermonkey.render;

import browsermonkey.document.*;
import java.awt.font.TextAttribute;
import java.text.AttributedCharacterIterator.Attribute;
import java.util.*;

/**
 * Renders heading tags from heading TagDocumentNodes.
 * @author Paul Calcraft
 */
public class HeadingTagRenderer extends TagRenderer {
    public HeadingTagRenderer(Linkable linker) {
        super(linker);
    }

    @Override
    public void render(Renderer renderer, TagDocumentNode tag, LayoutRenderNode parent, Map<Attribute, Object> formatting) {
        // Make headings bold.
        Map<Attribute, Object> newFormatting = (Map<Attribute, Object>)((HashMap)formatting).clone();
        newFormatting.put(TextAttribute.WEIGHT, TextAttribute.WEIGHT_BOLD);

        // Make sure there's a line space after the previous element.
        parent.ensureLinespaceDistance(1);

        // Get the heading number from the tag type, e.g. 3 from "h3".
        int headingLevel = Integer.parseInt(tag.getType().substring(1)) - 1;

        // Create layout node for the heading.
        LayoutRenderNode headingTextLayoutNode = new LayoutRenderNode(linker);
        // Get the heading text according to the renderer and pad the node.
        TextRenderNode headingNumberNode = new TextRenderNode(linker);
        headingNumberNode.addText(renderer.getHeadingString(headingLevel)+"&nbsp;", newFormatting);
        headingTextLayoutNode.addNodePadding(headingNumberNode, null);

        // Render all children into the padded layout node using the Renderer
        // with the bold formatting.
        for (DocumentNode child : tag.getChildren())
            renderer.render(child, headingTextLayoutNode, newFormatting);

        parent.addNode(headingTextLayoutNode, LayoutRenderNode.WidthBehaviour.Maximal);

        // Make sure there's a line space before the next element.
        parent.ensureLinespaceDistance(1);
    }
}