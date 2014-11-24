package de.paluch.heckenlights.rest;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import de.paluch.heckenlights.model.PlayStatus;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 * @since 28.11.13 21:57
 */
@XmlRootElement(name = "enqueued")
@XmlAccessorType(XmlAccessType.NONE)
public class EnqueueResponseRepresentation {

    @XmlAttribute(name = "enqueuedCommandId")
    private String enqueuedCommandId;

    @XmlElement(name = "playStatus")
    private PlayStatus playStatus;

    @XmlElement(name = "message")
    private String message;

    @XmlElement(name = "trackName")
    private String trackName;

    @XmlElement(name = "durationToPlay")
    private int durationToPlay;

    public EnqueueResponseRepresentation() {
    }

    public EnqueueResponseRepresentation(PlayStatus playStatus, String message) {
        this.playStatus = playStatus;
        this.message = message;
    }

    public String getEnqueuedCommandId() {
        return enqueuedCommandId;
    }

    public void setEnqueuedCommandId(String enqueuedCommandId) {
        this.enqueuedCommandId = enqueuedCommandId;
    }

    public PlayStatus getPlayStatus() {
        return playStatus;
    }

    public void setPlayStatus(PlayStatus playStatus) {
        this.playStatus = playStatus;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getDurationToPlay() {
        return durationToPlay;
    }

    public void setDurationToPlay(int durationToPlay) {
        this.durationToPlay = durationToPlay;
    }

    public String getTrackName() {
        return trackName;
    }

    public void setTrackName(String trackName) {
        this.trackName = trackName;
    }
}
