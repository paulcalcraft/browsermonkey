package browsermonkey.render;

import java.awt.*;
import java.text.AttributedString;
import java.util.ArrayList;
import javax.swing.*;

/**
 * Represents a block level element that can contain text and other RenderNodes.
 * @author Paul Calcraft
 */
public class LayoutRenderNode extends RenderNode {
    private GroupLayout layout;
    private GroupLayout.ParallelGroup horizontalGroup;
    private GroupLayout.SequentialGroup verticalGroup;
    private TextRenderNode currentTextNode;
    private int currentLinespaceDistance = 0;
    private boolean hasPreviousComponent = false;
    private boolean centred;

    public LayoutRenderNode(Linkable linker) {
        this(linker, false);
    }

    public LayoutRenderNode(Linkable linker, boolean centred) {
        super(linker);
        this.centred = centred;

        // Create the layout for this layout node.
        layout = new GroupLayout(this);
        this.setLayout(layout);

        // This block uses parallel layout to keep child component widths
        // in sync and sequential layout to keep vertical positions contiguous.

        horizontalGroup = layout.createParallelGroup(centred ? GroupLayout.Alignment.CENTER : GroupLayout.Alignment.LEADING);
        layout.setHorizontalGroup(horizontalGroup);

        verticalGroup = layout.createSequentialGroup();
        layout.setVerticalGroup(verticalGroup);

        // If centred, add an invisible JComponent that can push the width to
        // maximum, so things can be centred in the whole available space.
        if (centred) {
            // Anonymous subclass, as JComponent is abstract.
            JComponent widthSpacer = new JComponent() {};
            horizontalGroup.addComponent(widthSpacer, 0, 0, Short.MAX_VALUE);
            verticalGroup.addComponent(widthSpacer, 0, 0, 0);
        }
    }

    @Override
    public void setZoomLevel(float zoomLevel) {
        // For each RenderNode in components, set the zoom level.
        for (Component component : getComponents()) {
            if (component instanceof RenderNode) {
                RenderNode node = (RenderNode)component;
                node.setZoomLevel(zoomLevel);
            }
        }
    }

    @Override
    public void extractTextInto(ArrayList<AttributedString> text) {
        // (Potentially recursively) call extract on all child RenderNodes.
        for (Component component : getComponents()) {
            if (component instanceof RenderNode) {
                ((RenderNode)component).extractTextInto(text);
            }
        }
    }

    /**
     * Sets the padding for this layout node.
     * @param left
     * @param right
     * @param top
     * @param bottom
     */
    public void setPadding(int left, int right, int top, int bottom) {
        // If there's any horizontal padding...
        if (left > 0 || right > 0) {
            // Create a new padded container.
            GroupLayout.SequentialGroup paddedContainer = layout.createSequentialGroup();
            // Add the appropriate gaps surrounding the existing horizontal
            // group of components.
            if (left > 0)
                paddedContainer.addGap(left);

            paddedContainer.addGroup(horizontalGroup);

            if (right > 0)
                paddedContainer.addGap(right);

            // Set this new padded group as the layout node's horizontal group.
            layout.setHorizontalGroup(paddedContainer);
        }
        // Otherwise, ensure the unpadded group is the horizontal group.
        else
            layout.setHorizontalGroup(horizontalGroup);

        // Do the same for vertical padding.
        if (top > 0 || bottom > 0) {
            GroupLayout.SequentialGroup paddedContainer = layout.createSequentialGroup();
            if (top > 0)
                paddedContainer.addGap(top);
            paddedContainer.addGroup(verticalGroup);
            if (bottom > 0)
                paddedContainer.addGap(bottom);

            layout.setVerticalGroup(paddedContainer);
        }
        else
            layout.setVerticalGroup(verticalGroup);
    }

    /**
     * Pads the layout node horizontally with the given RenderNodes.
     * @param leftNode the node to use as padding on the left, or null
     * @param rightNode the node to use as padding on the right, or null
     */
    public void addNodePadding(RenderNode leftNode, RenderNode rightNode) {
        // If no padding nodes provided, return.
        if (leftNode == null && rightNode == null)
            return;

        // Create new horizontal group for padding and vertical group for
        // syncing the padding nodes' heights in sync with the content.
        GroupLayout.SequentialGroup paddingHorizontalContainer = layout.createSequentialGroup();
        GroupLayout.ParallelGroup verticalOverlapper = layout.createParallelGroup(GroupLayout.Alignment.LEADING);
        verticalOverlapper.addGroup(verticalGroup);

        // Add the appropriate nodes to the padding group with the existing
        // horizontal group in between.

        if (leftNode != null) {
            paddingHorizontalContainer.addComponent(leftNode);
            verticalOverlapper.addComponent(leftNode);
        }
        
        paddingHorizontalContainer.addGroup(horizontalGroup);

        if (rightNode != null) {
            paddingHorizontalContainer.addComponent(rightNode);
            verticalOverlapper.addComponent(rightNode);
        }

        // Set the layout to use our new groups.
        layout.setHorizontalGroup(paddingHorizontalContainer);
        layout.setVerticalGroup(verticalOverlapper);
    }

    /**
     * Determines how the layout node will calculate the maximum width.
     */
    public enum WidthBehaviour {
        /**
         * Maximum width will be set to the node's preferred size.
         */
        Minimal,
        /**
         * Maximum width will be set to the node's default maximum width.
         */
        Maximal,
        /**
         * The width will grow to the space given by the layout node.
         */
        Grow
    }

    /**
     * Adds a node to this layout node in the correct place and with the
     * specified width resize behaviour.
     * @param node
     * @param widthBehaviour how the node's width should be treated
     */
    public void addNode(RenderNode node, WidthBehaviour widthBehaviour) {
        // If we're not adding the text node itself, and the node is in the
        // middle of accumulating contiguous text, break.
        if (node != currentTextNode)
            ensureNewLine();

        // If we have a previous component (and therefore something we wish to
        // ensure linespace distance from) add a line space for each required
        // space.
        if (hasPreviousComponent)
            for (int i = 0; i < currentLinespaceDistance; i++)
                addLineSpace(false);

        // We're adding a node, so we do have a previous component.
        hasPreviousComponent = true;
        // Reset line space distance.
        currentLinespaceDistance = 0;

        // Calculate what the maximum layout width should be based on the width
        // behaviour parameter.
        int widthMax = GroupLayout.DEFAULT_SIZE;
        if (widthBehaviour == WidthBehaviour.Minimal)
            widthMax = GroupLayout.PREFERRED_SIZE;
        else if (widthBehaviour == WidthBehaviour.Grow)
            widthMax = Short.MAX_VALUE;

        // Add the component to the layout groups.
        verticalGroup.addComponent(node, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE);
        // Use CENTER alignment if we're a centred node.
        horizontalGroup.addComponent(node, centred ? GroupLayout.Alignment.CENTER : GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, widthMax);
    }

    /**
     * Get the current text node for this node's text accumulation.
     * @return
     */
    public TextRenderNode getTextNode() {
        if (currentTextNode == null) {
            // If there isn't one, create a new one and add it.
            // Used the centred boolean from this layout.
            currentTextNode = new TextRenderNode(linker, centred);
            addNode(currentTextNode, WidthBehaviour.Maximal);
        }
        return currentTextNode;
    }

    /**
     * Guarantees that there will be at least the specified distance between the
     * last component and the next one added.
     * @param distance the number of lines of spacing required
     */
    public void ensureLinespaceDistance(int distance) {
        ensureNewLine();
        currentLinespaceDistance = Math.max(currentLinespaceDistance, distance);
    }

    /**
     * Adds a hard line break.
     */
    public void addHardLineBreak() {
        // If there is no current text accumulation, add a line space as a new
        // node.
        if (currentTextNode == null)
            addLineSpace(true);
        // Else break the current text accumulation to act as a line break.
        else
            ensureNewLine();
    }

    // Adds a line space to to the node.
    private void addLineSpace(boolean addAsNode) {
        // Create a defaulty formatted text node for this line space.
        TextRenderNode lineSpace = new TextRenderNode(linker);
        lineSpace.addText("&nbsp;", Renderer.DEFAULT_FORMATTING);

        // If adding as a node, use the addNode method.
        if (addAsNode) {
            addNode(lineSpace, WidthBehaviour.Minimal);
        }
        // Else add directly to the layout groups.
        else {
            verticalGroup.addComponent(lineSpace);
            horizontalGroup.addComponent(lineSpace);
        }
    }

    /**
     * Ensures any further additions to this node will not appear on the same
     * line.
     */
    public void ensureNewLine() {
        // Break the current text node so new text will be added to a new text
        // node on a new line.
        currentTextNode = null;
    }
}