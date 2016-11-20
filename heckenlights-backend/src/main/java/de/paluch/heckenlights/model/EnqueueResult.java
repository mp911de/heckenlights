package de.paluch.heckenlights.model;

import lombok.Data;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 * @since 01.12.13 21:08
 */
@Data
public class EnqueueResult {

    String commandId;
    int durationToPlay;
    String exception;
    String trackName;
}
