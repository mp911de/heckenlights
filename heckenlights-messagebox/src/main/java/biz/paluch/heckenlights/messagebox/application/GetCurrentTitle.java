package biz.paluch.heckenlights.messagebox.application;

import biz.paluch.heckenlights.messagebox.client.midirelay.MidiRelayClient;
import biz.paluch.heckenlights.messagebox.client.midirelay.PlayerStateRepresentation;
import biz.paluch.heckenlights.messagebox.client.midirelay.PlayerStateTrackRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.inject.Inject;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
@Service
public class GetCurrentTitle {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Inject
    private MidiRelayClient midiRelayClient;

    public String getCurrentTitle() {

        try {
            PlayerStateRepresentation state = midiRelayClient.getState();

            if (state != null && state.isRunning() && state.getTrack() != null) {
                PlayerStateTrackRepresentation track = state.getTrack();
                if (StringUtils.hasText(track.getFileName()) && StringUtils.hasText(track.getSequenceName())) {
                    return track.getFileName().trim() + "/" + track.getSequenceName();
                }

                if (StringUtils.hasText(track.getFileName())) {
                    return track.getFileName().trim();
                }

                if (StringUtils.hasText(track.getSequenceName())) {
                    return track.getSequenceName().trim();
                }
            }

        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
        }
        return null;
    }
}
