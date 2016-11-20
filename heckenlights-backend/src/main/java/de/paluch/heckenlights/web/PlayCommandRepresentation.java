package de.paluch.heckenlights.web;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonFormat;

import de.paluch.heckenlights.model.PlayStatus;
import lombok.Data;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 * @since 02.12.13 18:15
 */
@XmlRootElement(name = "playCommand")
@XmlAccessorType(XmlAccessType.NONE)
@Data
public class PlayCommandRepresentation {

    @XmlAttribute(name = "id")
    String id;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @XmlElement(name = "createdTime")
    Date createdTime;

    @XmlElement(name = "created")
    Date created;

    @XmlElement(name = "trackName")
    String trackName;

    @XmlElement(name = "fileName")
    String fileName;

    @XmlElement(name = "playStatus")
    PlayStatus playStatus;

    @XmlElement(name = "duration")
    int duration;

    @XmlElement(name = "externalSessionId")
    String externalSessionId;

    @XmlElement(name = "submissionHost")
    String submissionHost;

    @XmlElement(name = "exception")
    String exception;

    @XmlElementWrapper(name = "captures")
    @XmlElement(name = "capture")
    List<PlayCaptureRepresentation> captures = new ArrayList<>();

    @XmlElement(name = "timeToStart")
    int timeToStart;

    @XmlElement(name = "remaining")
    int remaining;

    public void setCreated(Date created) {

        this.createdTime = created;
        this.created = created;
    }
}
