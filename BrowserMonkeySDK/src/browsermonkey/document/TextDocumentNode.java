package browsermonkey.document;

/**
 * Represents a block of text.
 * @author Paul Calcraft
 */
public class TextDocumentNode extends DocumentNode {
    private String text;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    /**
     * Constructs a new <code>TextDocumentNode</code> with the specified text.
     * @param text
     */
    public TextDocumentNode(String text) {
        this.text = text;
    }

    @Override
    public String toDebugString() {
        // Encloses the text in square brackets so whitespace nodes are visible.
        return '['+text+']';
    }
}