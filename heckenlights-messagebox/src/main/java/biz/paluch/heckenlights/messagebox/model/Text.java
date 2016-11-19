package biz.paluch.heckenlights.messagebox.model;

import lombok.Data;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
@Data
public class Text {

    String text;
    boolean emoji;
    int codepoint;

    public Text(String text, boolean emoji) {
        this.setText(text);
        this.setEmoji(emoji);
    }

    public Text(String text, boolean emoji, int codepoint) {
        this.setText(text);
        this.setEmoji(emoji);
        this.setCodepoint(codepoint);
    }
}
