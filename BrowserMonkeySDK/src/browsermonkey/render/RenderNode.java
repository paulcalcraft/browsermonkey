package browsermonkey.render;

import java.text.AttributedString;
import java.util.ArrayList;
import javax.swing.*;

/**
 * Provides a base for the nodes used to render the document.
 * @author Paul Calcraft
 */
public abstract class RenderNode extends JComponent {
    protected Linkable linker;

    /**
     * Provides a base constructor that stores the linker for the document.
     * @param linker
     */
    public RenderNode(Linkable linker) {
        this.linker = linker;
    }

    /**
     * Updates the node to the specified zoom level. Does nothing by default.
     * @param zoomLevel the level of zoom as a non-negative decimal (1 = 100%)
     */
    public void setZoomLevel(float zoomLevel) {}

    /**
     * Extracts any AttributedStrings in the node and its children into the
     * provided ArrayList. Should perform a depth-first traversal.
     * @param text
     */
    public void extractTextInto(ArrayList<AttributedString> text) {}
}
