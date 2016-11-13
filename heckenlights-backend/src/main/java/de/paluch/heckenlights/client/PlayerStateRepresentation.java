package de.paluch.heckenlights.client;

import lombok.Data;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import java.util.Date;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 * @since 30.11.13 19:27
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class PlayerStateRepresentation {

    private boolean running;
    private Date started;
    private Date estimatedEnd;
    private int estimatedSecondsToPlay;
    private PlayerStateTrackRepresentation track;
}
