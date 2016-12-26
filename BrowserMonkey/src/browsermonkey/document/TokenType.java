package browsermonkey.document;

/**
 * Enum used to track whether a given <code>Token</code> is a Tag token or a
 * Text token.
 * @author Lawrence Dine
 */
public enum TokenType {
    /**
     * For <code>Token</code> that contain text.
     */
    TEXT,
    /**
     * For <code>Token</code> that contain a tag.
     */
    TAG
}
