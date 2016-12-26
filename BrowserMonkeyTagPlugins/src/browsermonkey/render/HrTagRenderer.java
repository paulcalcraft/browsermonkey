package browsermonkey.render;

import browsermonkey.document.*;
import java.text.AttributedCharacterIterator.Attribute;
import java.awt.*;
import java.util.*;

/**
 * Renders horizontal rules from hr TagDocumentNodes.
 * @author Paul Calcraft
 */
public class HrTagRenderer extends TagRenderer {
    public HrTagRenderer(Linkable linker) {
        super(linker);
    }
    
    @Override
    public void render(Renderer renderer, TagDocumentNode tag, LayoutRenderNode parent, Map<Attribute, Object> formatting) {
        // Construct a horizontal rule.
        HrRenderNode hrNode = new HrRenderNode(linker);
        // Add it with growing width so it fills the full width.
        parent.addNode(hrNode, LayoutRenderNode.WidthBehaviour.Grow);
    }

    /**
     * RenderNode for a horizontal rule.
     */
    private static class HrRenderNode extends RenderNode {
        public HrRenderNode(Linkable linker) {
            super(linker);
            // May be as thin as 0 width, and as wide as "infinity".
            // Should always be 15 pixels high.
            this.setMinimumSize(new Dimension(0, 15));
            this.setMaximumSize(new Dimension(Short.MAX_VALUE, 15));
        }

        @Override
        public void paint(Graphics g) {
            g.setColor(Color.black);
            // Get the current size.
            Dimension size = this.getSize();
            // Draw a line vertically centred with the current width.
            g.drawLine(0, size.height/2, size.width, size.height/2);
        }        
    }
}