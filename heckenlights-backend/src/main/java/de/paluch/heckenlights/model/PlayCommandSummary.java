package de.paluch.heckenlights.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import lombok.Data;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 * @since 02.12.13 18:20
 */
@Data
public class PlayCommandSummary {

    private String id;
    private Date created;
    private String trackName;
    private String fileName;
    private PlayStatus playStatus;
    private int duration;
    private String externalSessionId;
    private String submissionHost;
    private String exception;
    private List<Date> captures = new ArrayList<>();
    private int timeToStart;
    private int remaining;
}
