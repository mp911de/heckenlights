package de.paluch.heckenlights.repositories;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 * @since 28.11.13 21:10
 */

@Document(collection = "State")
@Data
@EqualsAndHashCode(of = "id")
public class StateDocument {

    @Id
    private String id;

    @Field
    private boolean online;

    @Field
    private boolean queueOpen;

    @Field
    private boolean queueProcessorActive;
}
