package browsermonkey.render;

import browsermonkey.document.*;
import java.text.AttributedCharacterIterator.Attribute;
import java.awt.font.*;
import java.util.*;

/**
 * Renders bold tags from bold TagDocumentNodes.
 * @author Paul Calcraft
 */
public class BoldTagRenderer extends TagRenderer {
    public BoldTagRenderer(Linkable linker) {
        super(linker);
    }
    
    @Override
    public void render(Renderer renderer, TagDocumentNode tag, LayoutRenderNode parent, Map<Attribute, Object> formatting) {
        // Clone and add bold weight to the formatting.
        Map<Attribute, Object> newFormatting = (Map<Attribute, Object>)((HashMap)formatting).clone();
        newFormatting.put(TextAttribute.WEIGHT, TextAttribute.WEIGHT_BOLD);
        // Render all children into the same parent using the Renderer and the
        // new bold formatting.
        for (DocumentNode child : tag.getChildren())
            renderer.render(child, parent, newFormatting);
    }
}