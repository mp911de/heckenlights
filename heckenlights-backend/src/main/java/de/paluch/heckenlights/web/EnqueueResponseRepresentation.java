package de.paluch.heckenlights.web;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import de.paluch.heckenlights.model.PlayStatus;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 * @since 28.11.13 21:57
 */
@XmlRootElement(name = "enqueued")
@XmlAccessorType(XmlAccessType.NONE)
@Data
@NoArgsConstructor
public class EnqueueResponseRepresentation {

    @XmlAttribute(name = "enqueuedCommandId")
    String enqueuedCommandId;

    @XmlElement(name = "playStatus")
    PlayStatus playStatus;

    @XmlElement(name = "message")
    String message;

    @XmlElement(name = "trackName")
    String trackName;

    @XmlElement(name = "durationToPlay")
    int durationToPlay;

    public EnqueueResponseRepresentation(PlayStatus playStatus, String message) {
        this.playStatus = playStatus;
        this.message = message;
    }
}
