package biz.paluch.heckenlights.messagebox.client.midirelay;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 * @since 01.12.13 10:27
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@NoArgsConstructor
public class PlayerStateTrackRepresentation {

    String id;
    String sequenceName;
    String fileName;
    int duration;

    public PlayerStateTrackRepresentation(String sequenceName, String fileName) {
        this.sequenceName = sequenceName;
        this.fileName = fileName;
    }

}
