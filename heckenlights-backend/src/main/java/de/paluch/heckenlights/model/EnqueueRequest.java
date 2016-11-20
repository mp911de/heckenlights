package de.paluch.heckenlights.model;

import java.util.Date;

import lombok.Data;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 * @since 28.11.13 21:49
 */
@Data
public class EnqueueRequest {

    String commandId;
    Date created;
    String trackName;
    String fileName;
    PlayStatus playStatus;
    byte[] content;
    String externalSessionId;
    String submissionHost;
    int duration;
}
