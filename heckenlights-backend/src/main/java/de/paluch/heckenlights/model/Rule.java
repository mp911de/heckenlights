package de.paluch.heckenlights.model;

import java.time.DayOfWeek;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

import com.google.common.collect.Sets;

import lombok.Data;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@Data
public class Rule {

    @XmlElementWrapper(name = "days")
    @XmlElement(name = "day")
    private Set<DayOfWeek> days = Sets.newHashSet();

    private String from;
    private String to;

    private Long maxPlaylistPlayedDuration;
    private Long minLightsOnDuration;
    private Boolean queueIsEmpty;
    private Action action;

    @XmlElement(name = "reset")
    private Set<Counter> reset = Sets.newHashSet();

    public Set<DayOfWeek> getDays() {
        return days;
    }

    public void setDays(Set<DayOfWeek> days) {
        this.days = days;
    }

    public int getHourFrom() {
        if (from != null && from.indexOf(':') > -1) {
            return Integer.parseInt(from.split("\\:")[0]);
        }

        return -1;
    }

    public int getHourTo() {
        if (to != null && to.indexOf(':') > -1) {
            return Integer.parseInt(to.split("\\:")[0]);
        }

        return -1;
    }

    public int getMinuteFrom() {
        if (from != null && from.indexOf(':') > -1) {
            return Integer.parseInt(from.split("\\:")[1]);
        }

        return -1;
    }

    public int getMinuteTo() {
        if (to != null && to.indexOf(':') > -1) {
            return Integer.parseInt(to.split("\\:")[1]);
        }

        return -1;
    }

    public static enum Action {
        OFFLINE, LIGHTS_ON, LIGHTS_OFF, PLAYLIST, PLAYLIST_AUTO_ENQEUE;
    }

    public static enum Counter {
        PlaylistPlayedDuration, LightsOnDuration;
    }
}
