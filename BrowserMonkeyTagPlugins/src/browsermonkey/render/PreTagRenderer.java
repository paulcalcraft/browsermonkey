package browsermonkey.render;

import browsermonkey.document.*;
import java.text.AttributedCharacterIterator.Attribute;
import java.awt.font.*;
import java.util.*;

/**
 *
 * @author Paul Calcraft
 */
public class PreTagRenderer extends TagRenderer {
    public PreTagRenderer(Linkable linker) {
        super(linker);
    }

    @Override
    public void render(Renderer renderer, TagDocumentNode tag, LayoutRenderNode parent, Map<Attribute, Object> formatting) {
        // Use smaller monospace font for preformatted text.
        Map<Attribute, Object> newFormatting = (Map<Attribute, Object>)((HashMap)formatting).clone();
        newFormatting.put(TextAttribute.FAMILY, "Monospaced");
        newFormatting.put(TextAttribute.SIZE, 10);

        // Ensure there's a gap of at least one line break before the pre.
        parent.ensureLinespaceDistance(1);

        // Render all children into the same parent using the Renderer and the
        // new formatting.
        for (DocumentNode child : tag.getChildren())
            renderer.render(child, parent, newFormatting);

        // Ensure there's a gap of at least one line break after the pre.
        parent.ensureLinespaceDistance(1);
    }
}