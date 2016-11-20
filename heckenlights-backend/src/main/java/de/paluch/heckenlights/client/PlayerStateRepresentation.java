package de.paluch.heckenlights.client;

import java.util.Date;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import lombok.Data;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 * @since 30.11.13 19:27
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class PlayerStateRepresentation {

    boolean running;
    Date started;
    Date estimatedEnd;
    int estimatedSecondsToPlay;
    PlayerStateTrackRepresentation track;
}
