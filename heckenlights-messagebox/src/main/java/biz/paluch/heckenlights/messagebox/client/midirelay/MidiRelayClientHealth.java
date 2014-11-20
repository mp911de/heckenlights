package biz.paluch.heckenlights.messagebox.client.midirelay;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.stereotype.Component;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
@Component
public class MidiRelayClientHealth extends AbstractHealthIndicator {
    @Autowired
    private MidiRelayClient midiRelayClient;

    @Override
    protected void doHealthCheck(Health.Builder builder) throws Exception {
        PlayerStateRepresentation state = midiRelayClient.getState();
        if (state == null) {
            builder.down();
        } else {
            builder.up();
            if (state.getStarted() != null) {
                builder.withDetail("started", state.getStarted());
            }
            if (state.getStarted() != null) {
                builder.withDetail("estimatedEnd", state.getEstimatedEnd());
            }
            if (state.getTrack() != null) {
                PlayerStateTrackRepresentation track = state.getTrack();
                if (track.getFileName() != null) {
                    builder.withDetail("fileName", track.getFileName());
                }
            }

        }
    }
}
