package biz.paluch.heckenlights.messagebox.application;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import biz.paluch.heckenlights.messagebox.model.Text;

public class StringPartIteratorTest {
    @Test
    public void simple() throws Exception {
        StringPartIterator iterator = new StringPartIterator("this is my simple text");
        assertThat(iterator.hasNext()).isTrue();

        Text text = iterator.next();
        assertThat(text.isEmoji()).isFalse();
        assertThat(text.getText()).isEqualTo("this is my simple text");

        assertThat(iterator.hasNext()).isFalse();
    }

    @Test
    public void oneEmoji() throws Exception {
        StringPartIterator iterator = new StringPartIterator("\uD83D\uDE04");
        assertThat(iterator.hasNext()).isTrue();

        Text text = iterator.next();
        assertThat(text.isEmoji()).isTrue();
        assertThat(text.getCodepoint()).isEqualTo(128516);
        assertThat(text.getText()).isEqualTo("\uD83D\uDE04");

        assertThat(iterator.hasNext()).isFalse();
    }

    @Test
    public void mixed() throws Exception {
        StringPartIterator iterator = new StringPartIterator("mixed \uD83D\uDE04 text");
        assertThat(iterator.hasNext()).isTrue();

        Text text1 = iterator.next();
        assertThat(text1.isEmoji()).isFalse();
        assertThat(text1.getText()).isEqualTo("mixed ");

        Text text2 = iterator.next();
        assertThat(text2.isEmoji()).isTrue();
        assertThat(text2.getText()).isEqualTo("\uD83D\uDE04");

        Text text3 = iterator.next();
        assertThat(text3.isEmoji()).isFalse();
        assertThat(text3.getText()).isEqualTo(" text");
    }
}