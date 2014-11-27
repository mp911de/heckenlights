package biz.paluch.heckenlights.messagebox.repository;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
@Document(collection = "DisplayCount")
public class DisplayCountDocument {

    @Id
    private String id;

    private int advertising;
    private int tweets;
    private int title;
    private int messages;

    public DisplayCountDocument() {
    }

    public DisplayCountDocument(String id, int advertising, int tweets, int title) {
        this.id = id;
        this.advertising = advertising;
        this.tweets = tweets;
        this.title = title;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

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
