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

    private long id;
    private String sender;
    private String message;
    private boolean processed;
    private Date received;
}
