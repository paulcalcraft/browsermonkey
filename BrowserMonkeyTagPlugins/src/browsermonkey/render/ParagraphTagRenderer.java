package browsermonkey.render;

import browsermonkey.document.*;
import java.text.AttributedCharacterIterator.Attribute;
import java.util.*;

/**
 * Renders paragraph tags from paragraph TagDocumentNodes.
 * @author Paul Calcraft
 */
public class ParagraphTagRenderer extends TagRenderer {
    public ParagraphTagRenderer(Linkable linker) {
        super(linker);
    }

    @Override
    public void render(Renderer renderer, TagDocumentNode tag, LayoutRenderNode parent, Map<Attribute, Object> formatting) {
        // Ensure there's a gap of at least one line break before the paragraph.
        parent.ensureLinespaceDistance(1);

        // Render the paragraph's contents into the current parent.
        for (DocumentNode child : tag.getChildren())
            renderer.render(child, parent, formatting);

        // Ensure there's a gap of at least one line break after the paragraph.
        parent.ensureLinespaceDistance(1);
    }
}