package biz.paluch.heckenlights.messagebox.model;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
public class DisplayCount {

    private int advertising;
    private int tweets;
    private int title;
    private int messages;

    public int getAdvertising() {
        return advertising;
    }

    public void setAdvertising(int advertising) {
        this.advertising = advertising;
    }

    public int getTweets() {
        return tweets;
    }

    public void setTweets(int tweets) {
        this.tweets = tweets;
    }

    public int getTitle() {
        return title;
    }

    public void setTitle(int title) {
        this.title = title;
    }

    public int getMessages() {
        return messages;
    }

    public void setMessages(int messages) {
        this.messages = messages;
    }
}
