package biz.paluch.heckenlights.messagebox.application;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import org.slf4j.LoggerFactory;

import biz.paluch.heckenlights.messagebox.model.Text;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
class Renderer {

    public static final int FONT_SIZE = 33;
    public static final int TEXT_BASELINE = 25;
    private static String FONT_NAME = "assets/Roboto-Medium.ttf";
    private static Font FONT;
    private EmojiLocator emojiLocator = new EmojiLocator();
    private Color fg;

    static {
        try {
            FONT = Font.createFont(Font.TRUETYPE_FONT, new File(FONT_NAME));
        } catch (IOException | FontFormatException e) {
            LoggerFactory.getLogger(Renderer.class).warn(e.getMessage(), e);
        }
    }

    public Renderer(Color fg) {
        this.fg = fg;
    }

    int getWidth(java.util.List<String> parts) {

        BufferedImage bufferedImage = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = bufferedImage.createGraphics();
        try {
            return runGraphics(0, parts, graphics);
        } finally {
            graphics.dispose();

        }
    }

    void render(int offsetX, java.util.List<String> parts, Graphics2D graphics2D) {
        runGraphics(offsetX, parts, graphics2D);
    }

    int runGraphics(int offsetX, java.util.List<String> parts, Graphics2D graphics2D) {
        int size = FONT_SIZE;
        double width = offsetX;

        Font font = FONT.deriveFont(Font.PLAIN, size);
        graphics2D.setFont(font);
        graphics2D.setPaint(fg);

        for (String part : parts) {

            StringPartIterator it = new StringPartIterator(part.toCharArray());
            while (it.hasNext()) {
                Text textSegment = it.next();

                if (textSegment.isEmoji()) {

                    width += drawEmoji(graphics2D, width, textSegment);
                }

                if (!textSegment.isEmoji()) {

                    width += drawString(graphics2D, width, font, textSegment);
                }
            }

        }
        return (int) width;
    }

    private double drawString(Graphics2D graphics2D, double x, Font font, Text textSegment) {
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics2D.drawString(textSegment.getText(), (int) x, TEXT_BASELINE);
        Rectangle2D stringBounds = font.getStringBounds(textSegment.getText(), graphics2D.getFontRenderContext());
        return stringBounds.getWidth();
    }

    private double drawEmoji(Graphics2D graphics2D, double x, Text textSegment) {
        if (emojiLocator.exists(textSegment.getCodepoint())) {
            try {
                Image pi-messagebox/messagebox-controller/config.iniimage = emojiLocator.getEmoji(textSegment.getCodepoint());

                // Assume emoji have a fixed size of 32px
                int size = 32;

                graphics2D.drawImage(image, (int) (x + 1), 0, size, size, null, null);
                return size + 4;
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }
        return 0;
    }
}
