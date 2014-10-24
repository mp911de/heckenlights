package de.paluch.heckenlights.application;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;
import javax.sound.midi.InvalidMidiDataException;

import de.paluch.heckenlights.model.PlayCommandSummary;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import de.paluch.heckenlights.client.MidiRelayClient;
import de.paluch.heckenlights.client.PlayerStateRepresentation;
import de.paluch.heckenlights.model.DurationExceededException;
import de.paluch.heckenlights.model.TrackContent;
import de.paluch.heckenlights.repositories.PlayCommandService;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 * @since 28.11.13 21:47
 */
@Component
public class ProcessQueue {

    private Logger log = Logger.getLogger(getClass());

    @Inject
    private MidiRelayClient client;

    @Inject
    private PlayCommandService playCommandService;

    @Inject
    private PopulateQueue populateQueue;

    public void processQueue() throws IOException, InvalidMidiDataException, DurationExceededException {

        PlayerStateRepresentation state = client.getState();
        if (state == null) {
            log.warn("Received null state");
            return;
        }

        if (state.isRunning()) {
            return;
        }

        List<PlayCommandSummary> commands = playCommandService.getEnquedCommands();

        if (commands.isEmpty()) {
            populateQueue.populateQueue();
        } else {
            PlayCommandSummary playCommand = commands.get(0);
            TrackContent trackContent = playCommandService.getTrackContent(playCommand.getId());
            log.info("Triggering play of " + trackContent.getFilename() + ", duration " + playCommand.getDuration()
                    + " secs submitted by " + playCommand.getSubmissionHost());
            client.play(trackContent.getId(), trackContent.getFilename(), trackContent.getContent());
            playCommandService.setStateExecuted(trackContent.getId());
        }
    }
}
