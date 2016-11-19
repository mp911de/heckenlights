package biz.paluch.heckenlights.messagebox.model;

import java.util.Date;

import javax.xml.bind.annotation.XmlRootElement;

import lombok.Data;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
@XmlRootElement
@Data
public class TweetSummary {

    long id;
    String sender;
    String message;
    boolean processed;
    Date received;
}
