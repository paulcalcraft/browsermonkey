package browsermonkey.render;

import browsermonkey.document.*;
import java.text.AttributedCharacterIterator.Attribute;
import java.awt.font.*;
import java.util.*;

/**
 * Renders italics tags from italics TagDocumentNodes.
 * @author Daniel Cooper
 */
public class ItalicsTagRenderer extends TagRenderer {
    public ItalicsTagRenderer(Linkable linker) {
        super(linker);
    }
    
    @Override
    public void render(Renderer renderer, TagDocumentNode tag, LayoutRenderNode parent, Map<Attribute, Object> formatting) {
        // Add oblique posture (italics) to formatting.
        Map<Attribute, Object> newFormatting = (Map<Attribute, Object>)((HashMap)formatting).clone();
        newFormatting.put(TextAttribute.POSTURE, TextAttribute.POSTURE_OBLIQUE);
        
        // Render all children into the same parent using the Renderer and the
        // new italics formatting.
        for (DocumentNode child : tag.getChildren())
            renderer.render(child, parent, newFormatting);
    }
}