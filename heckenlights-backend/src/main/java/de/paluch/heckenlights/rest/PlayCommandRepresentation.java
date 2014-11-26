package de.paluch.heckenlights.rest;

import com.fasterxml.jackson.annotation.JsonFormat;
import de.paluch.heckenlights.model.PlayStatus;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 * @since 02.12.13 18:15
 */
@XmlRootElement(name = "playCommand")
@XmlAccessorType(XmlAccessType.NONE)
public class PlayCommandRepresentation {
    @XmlAttribute(name = "id")
    private String id;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @XmlElement(name = "createdTime")
    private Date createdTime;

    @XmlElement(name = "created")
    private Date created;

    @XmlElement(name = "trackName")
    private String trackName;

    @XmlElement(name = "fileName")
    private String fileName;

    @XmlElement(name = "playStatus")
    private PlayStatus playStatus;

    @XmlElement(name = "duration")
    private int duration;

    @XmlElement(name = "externalSessionId")
    private String externalSessionId;

    @XmlElement(name = "submissionHost")
    private String submissionHost;

    @XmlElement(name = "exception")
    private String exception;

    @XmlElementWrapper(name = "captures")
    @XmlElement(name = "capture")
    private List<PlayCaptureRepresentation> captures = new ArrayList<>();

    @XmlElement(name = "timeToStart")
    private int timeToStart;

    @XmlElement(name = "remaining")
    private int remaining;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {

        this.createdTime = created;
        this.created = created;
    }

    public String getTrackName() {
        return trackName;
    }

    public void setTrackName(String trackName) {
        this.trackName = trackName;
    }

    public PlayStatus getPlayStatus() {
        return playStatus;
    }

    public void setPlayStatus(PlayStatus playStatus) {
        this.playStatus = playStatus;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getExternalSessionId() {
        return externalSessionId;
    }

    public void setExternalSessionId(String externalSessionId) {
        this.externalSessionId = externalSessionId;
    }

    public String getSubmissionHost() {
        return submissionHost;
    }

    public void setSubmissionHost(String submissionHost) {
        this.submissionHost = submissionHost;
    }

    public String getException() {
        return exception;
    }

    public void setException(String exception) {
        this.exception = exception;
    }

    public List<PlayCaptureRepresentation> getCaptures() {
        return captures;
    }

    public void setCaptures(List<PlayCaptureRepresentation> captures) {
        this.captures = captures;
    }

    public int getTimeToStart() {
        return timeToStart;
    }

    public void setTimeToStart(int timeToStart) {
        this.timeToStart = timeToStart;
    }

    public int getRemaining() {
        return remaining;
    }

    public void setRemaining(int remaining) {
        this.remaining = remaining;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
