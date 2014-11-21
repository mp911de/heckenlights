package de.paluch.heckenlights.repositories;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 * @since 28.11.13 21:10
 */

@Document(collection = "State")
public class StateDocument {
    @Id
    private String id;

    @Field
    private boolean online;

    @Field
    private boolean queueOpen;

    @Field
    private boolean queueProcessorActive;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public boolean isQueueOpen() {
        return queueOpen;
    }

    public void setQueueOpen(boolean queueOpen) {
        this.queueOpen = queueOpen;
    }

    public boolean isQueueProcessorActive() {
        return queueProcessorActive;
    }

    public void setQueueProcessorActive(boolean queueProcessorActive) {
        this.queueProcessorActive = queueProcessorActive;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof StateDocument))
            return false;

        StateDocument that = (StateDocument) o;

        if (id != null ? !id.equals(that.id) : that.id != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
