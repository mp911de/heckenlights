package de.paluch.heckenlights.web;

import java.util.Date;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 * @since 02.12.13 18:16
 */
@XmlAccessorType(XmlAccessType.NONE)
@Data
public class PlayCaptureRepresentation {

    @XmlAttribute(name = "id")
    int id;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @XmlElement(name = "created")
    Date created;
}
