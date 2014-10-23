package de.paluch.heckenlights.application;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.UUID;

import javax.inject.Inject;
import javax.sound.midi.*;

import org.apache.log4j.Logger;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Component;

import com.google.common.io.Closer;

import de.paluch.heckenlights.EnqueueS;
import de.paluch.heckenlights.model.*;
import de.paluch.heckenlights.repositories.PlayCommandService;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 * @since 28.11.13 21:47
 */
@Component
public class Enqueue {
    private Logger log = Logger.getLogger(getClass());

    @Inject
    private PlayCommandService playCommandService;

    private final static int MINIMAL_DURATION_SEC = 10;
    private final static int MAXIMAL_DURATION_SEC = 300;
    private final static int QUOTA = 10;
    private final static int QUOTA_MINUTES = 30;

    public EnqueueResultModel enqueueWithQuotaCheck(EnqueueModel enqueue) throws IOException, InvalidMidiDataException,
            DurationExceededException, QuotaExceededException {

        int count = playCommandService.getEnquedCommandCount(enqueue.getExternalSessionId(), enqueue.getSubmissionHost(),
                QUOTA_MINUTES);
        if (count > QUOTA) {
            throw new QuotaExceededException("Quota limit of " + QUOTA + " for " + QUOTA_MINUTES + " exceeded by "
                    + (count - QUOTA));
        }

        return enqueue(enqueue);
    }

    public EnqueueResultModel enqueue(EnqueueModel enqueue) throws IOException, InvalidMidiDataException,
            DurationExceededException {
        Closer closer = Closer.create();
        try {

            log.info("Enqueuing " + enqueue.getFileName() + " from " + enqueue.getExternalSessionId() + "/"
                    + enqueue.getSubmissionHost());
            Sequence sequence = getSequence(closer, enqueue.getContent());
            int durationInSecs = getDuration(sequence);
            validateDuration(durationInSecs);

            enqueue.setDuration(durationInSecs);

            String id = UUID.randomUUID().toString();
            int timeToPlay = playCommandService.estimateTimeToPlayQueue();
            ObjectId fileReference = playCommandService.createFile(enqueue.getFileName(), "audio/midi", enqueue.getContent(),
                    id);

            enqueue.setTrackName(new EnqueueS().getSequenceName(sequence));
            enqueue.setPlayStatus(PlayStatus.ENQUEUED);
            enqueue.setCommandId(id);

            playCommandService.storeEnqueueRequest(enqueue, fileReference);

            EnqueueResultModel result = new EnqueueResultModel();
            result.setDurationToPlay(timeToPlay);
            result.setCommandId(enqueue.getCommandId());
            result.setTrackName(enqueue.getTrackName());

            return result;
        } finally {
            closer.close();
        }
    }

    private Sequence getSequence(Closer closer, byte[] content) throws InvalidMidiDataException, IOException {
        Sequence sequence = MidiSystem.getSequence(closer.register(new ByteArrayInputStream(content)));
        return sequence;
    }

    private void validateDuration(int durationInSecs) throws DurationExceededException {
        if (durationInSecs < MINIMAL_DURATION_SEC) {
            throw new DurationExceededException("Duration " + durationInSecs + " too short, min duration is: "
                    + MINIMAL_DURATION_SEC);
        }

        if (durationInSecs > MAXIMAL_DURATION_SEC) {
            throw new DurationExceededException("Duration " + durationInSecs + " too long, min duration is: "
                    + MAXIMAL_DURATION_SEC);
        }
    }

    private int getDuration(Sequence sequence) throws IOException, InvalidMidiDataException {
        try {
            // Create a sequencer for the sequence
            Sequencer sequencer = MidiSystem.getSequencer();
            sequencer.open();
            sequencer.setSequence(sequence);

            int durationInSecs = (int) (sequencer.getMicrosecondLength() / 1000000.0);
            return durationInSecs;

        } catch (MidiUnavailableException e) {
            return -1;
        }
    }

    protected String getSequenceName(Sequence sequence) {
        for (Track track : sequence.getTracks()) {
            for (int i = 0; i < track.size(); i++) {
                MidiEvent midiEvent = track.get(i);
                if (midiEvent.getMessage() instanceof MetaMessage) {
                    MidiMessageDetail detail = new MidiMessageDetail(midiEvent.getMessage());

                    if (detail.getT2() == 3 || detail.getT2() == 6) {
                        try {
                            if (detail.getBytes()[0] == -1) {
                                return new String(detail.getBytes(), 3, detail.getBytes().length - 3, "ASCII");
                            }

                            return new String(detail.getBytes(), "ASCII");
                        } catch (UnsupportedEncodingException e) {
                        }
                    }

                }
            }
        }

        return null;
    }
}
