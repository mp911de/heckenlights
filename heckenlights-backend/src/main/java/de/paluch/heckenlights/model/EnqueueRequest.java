package de.paluch.heckenlights.model;

import java.util.Date;

import lombok.Data;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 * @since 28.11.13 21:49
 */
@Data
public class EnqueueRequest {

    private String commandId;
    private Date created;
    private String trackName;
    private String fileName;
    private PlayStatus playStatus;
    private byte[] content;
    private String externalSessionId;
    private String submissionHost;
    private int duration;
}
