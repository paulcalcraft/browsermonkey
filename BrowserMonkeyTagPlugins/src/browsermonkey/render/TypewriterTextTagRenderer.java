package browsermonkey.render;

import browsermonkey.document.*;
import java.text.AttributedCharacterIterator.Attribute;
import java.awt.font.*;
import java.util.*;

/**
 * Renders typewriter text from tt TagDocumentNodes.
 * @author Paul Calcraft
 */
public class TypewriterTextTagRenderer extends TagRenderer {
    public TypewriterTextTagRenderer(Linkable linker) {
        super(linker);
    }

    @Override
    public void render(Renderer renderer, TagDocumentNode tag, LayoutRenderNode parent, Map<Attribute, Object> formatting) {
        // Clone the map and add attributes for 10pt monospaced font.
        Map<Attribute, Object> newFormatting = (Map<Attribute, Object>)((HashMap)formatting).clone();
        newFormatting.put(TextAttribute.FAMILY, "Monospaced");
        newFormatting.put(TextAttribute.SIZE, 10);

        // Render all children into the same parent using the Renderer and the
        // new formatting.
        for (DocumentNode child : tag.getChildren())
            renderer.render(child, parent, newFormatting);
    }
}