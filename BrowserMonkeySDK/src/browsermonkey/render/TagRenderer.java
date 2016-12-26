package browsermonkey.render;

import java.util.Map;
import browsermonkey.document.TagDocumentNode;
import java.text.AttributedCharacterIterator.Attribute;

/**
 * Provides a base for TagRenderers that render TagDocumentNodes into
 * RenderNodes.
 * @author Paul Calcraft
 */
public abstract class TagRenderer {
    protected Linkable linker;

    public TagRenderer(Linkable linker) {
        this.linker = linker;
    }
    
    /**
     * Renders the given tag into the parent, taking into account the current
     * formatting. Uses the given renderer for utility methods and rendering
     * children.
     * @param renderer
     * @param tag
     * @param parent
     * @param formatting
     */
    public abstract void render(Renderer renderer, TagDocumentNode tag, LayoutRenderNode parent, Map<Attribute,Object> formatting);
}
