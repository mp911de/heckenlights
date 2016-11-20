package de.paluch.heckenlights.repositories;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import de.paluch.heckenlights.model.PlayStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 * @since 28.11.13 21:10
 */

@CompoundIndexes({
        @CompoundIndex(name = "PlayCommand_session_host", def = "{externalSessionId: 1, submissionHost: 1, created: 1}") })
@Document(collection = "PlayCommand")
@Data
@EqualsAndHashCode(of = "id")
public class PlayCommandDocument {

    @Id
    String id;

    @Indexed
    Date created;
    String trackName;

    @Indexed
    PlayStatus playStatus;

    ObjectId attachedFile;
    int duration;
    String externalSessionId;
    String submissionHost;
    String exception;
    String fileName;
    List<ObjectId> captures = new ArrayList<>();
}
