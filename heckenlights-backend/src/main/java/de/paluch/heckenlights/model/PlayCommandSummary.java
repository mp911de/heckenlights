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

    String id;
    Date created;
    String trackName;
    String fileName;
    PlayStatus playStatus;
    int duration;
    String externalSessionId;
    String submissionHost;
    String exception;
    List<Date> captures = new ArrayList<>();
    int timeToStart;
    int remaining;
}
