package biz.paluch.heckenlights.messagebox.model;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
public class Text {

    private String text;
    private boolean emoji;
    private int codepoint;

    public Text(String text, boolean emoji) {
        this.setText(text);
        this.setEmoji(emoji);
    }

    public Text(String text, boolean emoji, int codepoint) {
        this.setText(text);
        this.setEmoji(emoji);
        this.setCodepoint(codepoint);
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isEmoji() {
        return emoji;
    }

    public void setEmoji(boolean emoji) {
        this.emoji = emoji;
    }

    public int getCodepoint() {
        return codepoint;
    }

    public void setCodepoint(int codepoint) {
        this.codepoint = codepoint;
    }
}
