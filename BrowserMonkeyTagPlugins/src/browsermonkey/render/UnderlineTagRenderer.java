package browsermonkey.render;

import browsermonkey.document.*;
import java.text.AttributedCharacterIterator.Attribute;
import java.awt.font.*;
import java.util.*;

/**
 *
 * @author Paul Calcraft
 */
public class UnderlineTagRenderer extends TagRenderer {
    public UnderlineTagRenderer(Linkable linker) {
        super(linker);
    }
    
    @Override
    public void render(Renderer renderer, TagDocumentNode tag, LayoutRenderNode parent, Map<Attribute, Object> formatting) {
        // Clone the map and add the attribute for underline.
        Map<Attribute, Object> newFormatting = (Map<Attribute, Object>)((HashMap)formatting).clone();
        newFormatting.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);

        // Render all children into the same parent using the Renderer and the
        // underline formatting.
        for (DocumentNode child : tag.getChildren())
            renderer.render(child, parent, newFormatting);
    }
}