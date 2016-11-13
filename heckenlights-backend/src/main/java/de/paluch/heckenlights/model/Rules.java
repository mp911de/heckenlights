package de.paluch.heckenlights.model;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.Data;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@Data
public class Rules {

    private String timezone;
    private TimeUnit timeunit;
    private Rule.Action defaultAction;

    @XmlElement(name = "rule")
    private List<Rule> rules = new ArrayList<>();
}
