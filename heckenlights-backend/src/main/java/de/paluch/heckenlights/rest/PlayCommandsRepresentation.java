package de.paluch.heckenlights.rest;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 * @since 02.12.13 18:15
 */
@XmlRootElement(name = "playCommands")
@XmlAccessorType(XmlAccessType.NONE)
public class PlayCommandsRepresentation {
    @XmlElement(name = "playCommand")
    private List<PlayCommandRepresentation> playCommands = new ArrayList<>();

    @XmlElement(name = "online")
    private boolean online;

    @XmlElement(name = "queueOpen")
    private boolean queueOpen;

    public List<PlayCommandRepresentation> getPlayCommands() {
        return playCommands;
    }

    public void setPlayCommands(List<PlayCommandRepresentation> playCommands) {
        this.playCommands = playCommands;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public boolean isQueueOpen() {
        return queueOpen;
    }

    public void setQueueOpen(boolean queueOpen) {
        this.queueOpen = queueOpen;
    }
}
