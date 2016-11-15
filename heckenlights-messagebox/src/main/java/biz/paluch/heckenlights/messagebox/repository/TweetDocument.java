package biz.paluch.heckenlights.messagebox.repository;

import java.util.Date;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
@Document(collection = "Tweet")
@Data
public class TweetDocument {

    @Id
    private long id;

    @Field
    private String sender;

    @Field
    private String message;

    @Indexed
    @Field
    private boolean processed;

    @Field
    private Date received;
}
