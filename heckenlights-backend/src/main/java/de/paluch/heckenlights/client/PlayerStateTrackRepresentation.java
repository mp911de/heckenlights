package de.paluch.heckenlights.client;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import lombok.Data;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 * @since 01.12.13 10:27
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class PlayerStateTrackRepresentation {

    private String id;
    private String sequenceName;
    private String fileName;
    private int duration;
}
