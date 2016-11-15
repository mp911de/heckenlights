package biz.paluch.heckenlights.messagebox.model;

import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
@Document
@Data
public class DisplayCount {

    private int advertising;
    private int tweets;
    private int title;
    private int messages;
}
