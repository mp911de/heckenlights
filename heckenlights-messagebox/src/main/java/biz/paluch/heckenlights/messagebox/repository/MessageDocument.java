package biz.paluch.heckenlights.messagebox.repository;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.Data;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
@Document(collection = "Message")
@Data
public class MessageDocument {

    @Id
    String id;

    @Field
    String message;

    @Indexed
    @Field
    boolean processed;
}
