package biz.paluch.heckenlights.messagebox.application;

import biz.paluch.heckenlights.messagebox.model.Text;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
class StringPartIterator implements Iterator<Text> {

    private static List<int[]> EMOJI_CODEPOINTS = new ArrayList<int[]>() {
        {
            add(new int[] { 0x1F601, 0x1F64F });
            add(new int[] { 0x2702, 0x27B0 });
            add(new int[] { 0x1F680, 0x1F6C0 });
            add(new int[] { 0x24C2, 0x1F251 });

            add(new int[] { 0x00A9 });
            add(new int[] { 0x00AE });
            add(new int[] { 0x2194, 0x2199 });
            add(new int[] { 0x21A9, 0x21AA });
            add(new int[] { 0x231A, 0x23F3 });
            add(new int[] { 0x25AA, 0x26FD });
            add(new int[] { 0x1F004, 0x1F636 });

            add(new int[] { 0x1F600, 0x1F636 });
            add(new int[] { 0x1F681, 0x1F6C5 });
            add(new int[] { 0x1F30D, 0x1F567 });
        }
    };

    private char[] chars;
    private String string;
    private int position;

    public StringPartIterator(String string)
    {
        this.string = string;
        this.chars = string.toCharArray();
                
    }

    StringPartIterator(char[] chars) {
        this.chars = chars;
        this.string = new String(chars);
    }

    @Override
    public boolean hasNext() {

        return position < chars.length;
    }

    @Override
    public Text next() {

        String value = "";
        int startPosition = position;
        for (int i = startPosition; i < chars.length;) {

            int codepoint = string.codePointAt(i);

            char c = chars[i];

            if (isEmoji(codepoint) && i + 1 < chars.length) {
                if (i == startPosition) {
                    value += c;
                    value += chars[i + 1];
                    position = i + 2;

                    return new Text(value, true, codepoint);

                } else {
                    break;
                }
            }

            i += Character.charCount(codepoint);
            value += c;
            position = i;
        }

        return new Text(value, false);
    }

    private boolean isEmoji(int codepoint) {

        for (int[] startStop : EMOJI_CODEPOINTS) {
            if (startStop.length == 1 && startStop[0] == codepoint) {
                return true;
            }

            if (startStop.length == 2 && startStop[0] <= codepoint && startStop[1] >= codepoint) {
                return true;
            }

        }
        return false;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
}
