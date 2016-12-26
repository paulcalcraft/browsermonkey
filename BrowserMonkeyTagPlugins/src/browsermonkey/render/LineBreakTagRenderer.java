package browsermonkey.render;

import browsermonkey.document.*;
import java.text.AttributedCharacterIterator.Attribute;
import java.util.*;

/**
 * Renders a line break.
 * @author Paul Calcraft
 */
public class LineBreakTagRenderer extends TagRenderer {
    public LineBreakTagRenderer(Linkable linker) {
        super(linker);
    }

    @Override
    public void render(Renderer renderer, TagDocumentNode tag, LayoutRenderNode parent, Map<Attribute, Object> formatting) {
        // Add a hard line break to the parent container.
        parent.addHardLineBreak();
    }
}