package browsermonkey.render;

import browsermonkey.document.*;
import java.text.AttributedCharacterIterator.Attribute;
import java.util.*;

/**
 * Sets the title of the document when a title tag is "rendered".
 * @author Paul Calcraft
 */
public class TitleTagRenderer extends TagRenderer {
    public TitleTagRenderer(Linkable linker) {
        super(linker);
    }
    
    @Override
    public void render(Renderer renderer, TagDocumentNode tag, LayoutRenderNode parent, Map<Attribute, Object> formatting) {
        // There should be just a single TextDocumentNode as the child of the
        // title tag, but to be safe, concatenate every text node found as a
        // child.
        String title = "";
        for (DocumentNode node : tag.getChildren()) {
            if (!(node instanceof TextDocumentNode))
                continue; // Should never happen if tree built correctly.
            title += ((TextDocumentNode)node).getText();
        }

        // Then set the title via the renderer.
        renderer.setTitle(title);
    }
}