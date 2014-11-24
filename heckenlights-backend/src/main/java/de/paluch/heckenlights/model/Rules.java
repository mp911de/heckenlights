package de.paluch.heckenlights.model;

import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.collect.Lists;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Rules {

    private String timezone;
    private TimeUnit timeunit;
    private Rule.Action defaultAction;

    @XmlElement(name = "rule")
    private List<Rule> rules = Lists.newArrayList();

    public List<Rule> getRules() {
        return rules;
    }

    public void setRules(List<Rule> rules) {
        this.rules = rules;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public TimeUnit getTimeunit() {
        return timeunit;
    }

    public void setTimeunit(TimeUnit timeunit) {
        this.timeunit = timeunit;
    }

    public Rule.Action getDefaultAction() {
        return defaultAction;
    }

    public void setDefaultAction(Rule.Action defaultAction) {
        this.defaultAction = defaultAction;
    }
}
