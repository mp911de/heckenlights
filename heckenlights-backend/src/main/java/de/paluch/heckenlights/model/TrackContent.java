package de.paluch.heckenlights.model;

import lombok.Data;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
@Data
public class TrackContent {

    String id;
    String filename;
    byte[] content;
}
