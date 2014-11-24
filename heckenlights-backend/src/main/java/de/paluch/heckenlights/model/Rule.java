package de.paluch.heckenlights.model;

import java.time.DayOfWeek;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

import com.google.common.collect.Sets;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
@XmlAccessorType(XmlAccessType.FIELD)
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

    public Long getMaxPlaylistPlayedDuration() {
        return maxPlaylistPlayedDuration;
    }

    public void setMaxPlaylistPlayedDuration(Long maxPlaylistPlayedDuration) {
        this.maxPlaylistPlayedDuration = maxPlaylistPlayedDuration;
    }

    public Boolean getQueueIsEmpty() {
        return queueIsEmpty;
    }

    public void setQueueIsEmpty(Boolean queueIsEmpty) {
        this.queueIsEmpty = queueIsEmpty;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    public Long getMinLightsOnDuration() {
        return minLightsOnDuration;
    }

    public void setMinLightsOnDuration(Long minLightsOnDuration) {
        this.minLightsOnDuration = minLightsOnDuration;
    }

    public Set<Counter> getReset() {
        return reset;
    }

    public void setReset(Set<Counter> reset) {
        this.reset = reset;
    }

    public static enum Action {
        OFFLINE, LIGHTS_ON, LIGHTS_OFF, PLAYLIST, PLAYLIST_AUTO_ENQEUE;
    }

    public static enum Counter {
        PlaylistPlayedDuration, LightsOnDuration;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer();
        sb.append(getClass().getSimpleName());
        sb.append(" [from='").append(from).append('\'');
        sb.append(", to='").append(to).append('\'');
        sb.append(", maxPlaylistPlayedDuration=").append(maxPlaylistPlayedDuration);
        sb.append(", minLightsOnDuration=").append(minLightsOnDuration);
        sb.append(", reset=").append(reset);
        sb.append(", action=").append(action);
        sb.append(", queueIsEmpty=").append(queueIsEmpty);
        sb.append(']');
        return sb.toString();
    }
}
