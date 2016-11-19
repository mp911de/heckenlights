package biz.paluch.heckenlights.messagebox.repository;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
@Document(collection = "DisplayCount")
@Data
@NoArgsConstructor
public class DisplayCountDocument {

    @Id
    String id;

    int advertising;
    int tweets;
    int title;
    int messages;

    public DisplayCountDocument(String id, int advertising, int tweets, int title) {
        this.id = id;
        this.advertising = advertising;
        this.tweets = tweets;
        this.title = title;
    }
}
