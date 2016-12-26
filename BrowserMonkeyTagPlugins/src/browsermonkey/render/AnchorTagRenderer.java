package browsermonkey.render;

import browsermonkey.document.*;
import java.text.AttributedCharacterIterator.Attribute;
import java.awt.*;
import java.awt.font.*;
import java.util.*;

/**
 * Renders anchor tags from anchor TagDocumentNodes.
 * @author Paul Calcraft
 */
public class AnchorTagRenderer extends TagRenderer {
    public AnchorTagRenderer(Linkable linker) {
        super(linker);
    }
    
    @Override
    public void render(Renderer renderer, TagDocumentNode tag, LayoutRenderNode parent, Map<Attribute, Object> formatting) {
        Map<Attribute, Object> newFormatting = formatting;
        String href = tag.getAttribute("href");
        if (href != null) {
            // If there is an attribute for href, clone the formatting.
            newFormatting = (Map<Attribute, Object>)((HashMap)formatting).clone();
            // Add blue foreground colour attribute.
            newFormatting.put(TextAttribute.FOREGROUND, Color.blue);
            // Add underline.
            newFormatting.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
            // Add attribute to store the href for later lookup.
            newFormatting.put(TextRenderNode.HREF_ATTRIBUTE, href);
        }

        // Render all children into the same parent using the Renderer and the
        // appropriate formatting.
        for (DocumentNode child : tag.getChildren())
            renderer.render(child, parent, newFormatting);
    }
}