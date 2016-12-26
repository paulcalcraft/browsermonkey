package browsermonkey.render;

import browsermonkey.document.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.text.AttributedCharacterIterator.Attribute;
import java.util.*;
import javax.imageio.*;

/**
 *
 * @author Paul Calcraft
 */
public class ImageTagRenderer extends TagRenderer {
    public ImageTagRenderer(Linkable linker) {
        super(linker);
    }

    @Override
    public void render(Renderer renderer, TagDocumentNode tag, LayoutRenderNode parent, Map<Attribute, Object> formatting) {
        String src = tag.getAttribute("src");
        // If no source attribute, don't render.
        if (src == null)
            return;

        // Retrieve link from formatting (null if none).
        String hrefValue = (String)formatting.get(TextRenderNode.HREF_ATTRIBUTE);

        // Attempt to load the resource as a byte array from the source URL.
        byte[] imageResource = renderer.loadResource(src);
        // Construct a new image node and add it.
        ImageRenderNode img = new ImageRenderNode(linker, imageResource, hrefValue);
        parent.addNode(img, LayoutRenderNode.WidthBehaviour.Maximal);
    }

    /**
     * RenderNode for an image (if not loaded, shows red X).
     */
    private static class ImageRenderNode extends RenderNode {
        private Image image;
        private static Image redX;
        private String link;

        // Static block to load the red x image from an embedded resource GIF.
        static {
            try {
                InputStream stream = ImageRenderNode.class.getResourceAsStream("/resources/redx.gif");
                if (stream != null) {
                    redX = ImageIO.read(stream);
                    stream.close();
                }
            } catch (IOException ex) {
                redX = null;
            }
        }

        public ImageRenderNode(Linkable linker, byte[] imageResource, String link) {
            super(linker);

            this.link = link;

            if (imageResource == null)
                image = null;
            else {
                try {
                    image = ImageIO.read(new ByteArrayInputStream(imageResource));
                } catch (IOException ex) {
                    image = null;
                }
            }

            if (link != null) {
                // Add click listener for linking.
                addMouseListener(new MouseListener() {
                    public void mouseClicked(MouseEvent e) {
                        click();
                    }

                    public void mouseEntered(MouseEvent e) {}
                    public void mouseExited(MouseEvent e) {}
                    public void mousePressed(MouseEvent e) {}
                    public void mouseReleased(MouseEvent e) {}
                });
            }
        }

        /**
         * Update the image size according to zoom.
         */
        private void updateSizes(float zoom) {
            int width = 0;
            int height = 0;
            
            if (image != null) {
                // Scale up the image by the zoom.
                width = Math.round(image.getWidth(null)*zoom);
                height = Math.round(image.getHeight(null)*zoom);
            }
            else if (redX != null) {
                // Use default size for red x image.
                width = Math.round(redX.getWidth(null));
                height = Math.round(redX.getHeight(null));
            }

            // Update the layout sizes.
            Dimension size = new Dimension(width, height);

            setMinimumSize(size);
            setMaximumSize(size);

            // Call for layout to be recalculated.
            revalidate();
        }

        @Override
        public void setZoomLevel(float zoomLevel) {
            updateSizes(zoomLevel);
        }

        @Override
        public void paint(Graphics g) {
            // If the image is loaded, draw it, else draw the red X if it is
            // loaded.
            Image drawImage;
            if (image != null)
                drawImage = image;
            else if (redX != null)
                drawImage = redX;
            else
                return;

            g.drawImage(drawImage, 0, 0, getWidth(), getHeight(), null);
        }

        private void click() {
            // Use the linker associated with this node to follow the link.
            linker.followLink(link);
        }
    }
}