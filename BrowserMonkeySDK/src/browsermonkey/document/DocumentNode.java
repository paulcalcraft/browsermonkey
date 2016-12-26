package browsermonkey.document;

import java.util.*;

/**
 * Provides default implementation for a <code>DocumentNode</code>'s children
 * handling.
 * @author Paul Calcraft
 */
public abstract class DocumentNode {
    List<DocumentNode> children;

    /**
     * Constructs a <code>DocumentNode</code> with no children.
     */
    public DocumentNode() {
        children = new ArrayList<DocumentNode>();
    }

    /**
     * Constructs a <code>DocumentNode</code> with the list of children
     * provided as arguments.
     * For example: <code>new DocumentNode(child1, child2);</code>
     * @param children
     */
    public DocumentNode(DocumentNode... children) {
        this.children = new ArrayList<DocumentNode>(Arrays.asList(children));
    }

    public List<DocumentNode> getChildren() {
        return children;
    }

    public void addChild(DocumentNode child){
        children.add(child);
    }

    /**
     * Returns a debug pseudo-HTML representation of the node and its children.
     * @return
     */
    public abstract String toDebugString();
}