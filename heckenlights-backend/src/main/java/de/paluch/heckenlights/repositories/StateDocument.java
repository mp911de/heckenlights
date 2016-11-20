package de.paluch.heckenlights.repositories;

import org.springframework.data.mongodb.core.mapping.Document;

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

    String id;
    boolean online;
    boolean queueOpen;
    boolean queueProcessorActive;
}
