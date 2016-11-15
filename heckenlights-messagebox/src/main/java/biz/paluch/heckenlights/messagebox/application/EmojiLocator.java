package biz.paluch.heckenlights.messagebox.application;

import java.awt.*;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
public class EmojiLocator {

    private File basePath = new File(System.getProperty("user.dir"));

    public boolean exists(int codepoint) {
        File file = getFile(codepoint);
        return file.exists();
    }

    public Image getEmoji(int codepoint) throws IOException {
        return ImageIO.read(getFile(codepoint));
    }

    private File getFile(int codepoint) {
        String filename = createFileName(codepoint);
        return new File(basePath, "emoji/" + filename);
    }

    private String createFileName(int codepoint) {
        return Integer.toHexString(codepoint).toLowerCase() + ".png";
    }

}
