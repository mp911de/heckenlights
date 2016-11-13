package de.paluch.heckenlights.rest;

import com.fasterxml.jackson.annotation.JsonFormat;
import de.paluch.heckenlights.model.PlayStatus;
import lombok.Data;

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
@Data
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

    public void setCreated(Date created) {

        this.createdTime = created;
        this.created = created;
    }
}
