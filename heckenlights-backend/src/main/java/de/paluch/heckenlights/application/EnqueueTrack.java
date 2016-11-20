package de.paluch.heckenlights.application;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.UUID;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.midi.Track;

import org.bson.types.ObjectId;
import org.springframework.stereotype.Component;

import com.google.common.collect.ImmutableList;
import com.google.common.io.Closer;

import de.paluch.heckenlights.model.DurationExceededException;
import de.paluch.heckenlights.model.EnqueueRequest;
import de.paluch.heckenlights.model.EnqueueResult;
import de.paluch.heckenlights.model.OfflineException;
import de.paluch.heckenlights.model.PlayCommandSummary;
import de.paluch.heckenlights.model.PlayStatus;
import de.paluch.heckenlights.model.QuotaExceededException;
import de.paluch.heckenlights.model.RuleState;
import de.paluch.heckenlights.repositories.PlayCommandService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 * @since 28.11.13 21:47
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class EnqueueTrack {

    public static final String CONTENT_TYPE = "audio/midi";

    private static Sequencer sequencer;

    private final static int MINIMAL_DURATION_SEC = 10;
    private final static int MAXIMAL_DURATION_SEC = 300;
    private final static int QUOTA = 10;
    private final static int LIMIT_ENEUQUED = 20;
    private final static int QUOTA_MINUTES = 30;

    @NonNull
    PlayCommandService playCommandService;

    @NonNull
    RuleState ruleState;

    @NonNull
    IsQueueOpen isQueueOpen;

    @NonNull
    GetOnlineState getOnlineState;

    public EnqueueResult enqueueWithQuotaCheck(EnqueueRequest enqueue)
            throws IOException, InvalidMidiDataException, DurationExceededException, QuotaExceededException, OfflineException {

        int count = playCommandService.getEnquedCommandCount(enqueue.getExternalSessionId(), enqueue.getSubmissionHost(),
                QUOTA_MINUTES);
        if (count > QUOTA) {
            throw new QuotaExceededException(
                    "Quota limit of " + QUOTA + " for " + QUOTA_MINUTES + " exceeded by " + (count - QUOTA));
        }

        List<PlayCommandSummary> enqueuedCommands = playCommandService
                .getListByPlayStatusOrderByCreated(ImmutableList.of(PlayStatus.ENQUEUED), 100);
        if (enqueuedCommands.size() > LIMIT_ENEUQUED) {
            throw new QuotaExceededException("Queue limit of " + LIMIT_ENEUQUED + " exceeded by " + (count - LIMIT_ENEUQUED));
        }

        if (!getOnlineState.isOnline()) {
            throw new OfflineException("System is offline");
        }

        if (!isQueueOpen.isQueueOpen()) {
            throw new OfflineException("Queue closed");
        }

        log.info("Enqueuing " + enqueue.getFileName() + " from " + enqueue.getExternalSessionId() + "/"
                + enqueue.getSubmissionHost());

        return enqueueImpl(enqueue);
    }

    public EnqueueResult populate(EnqueueRequest enqueue)
            throws IOException, InvalidMidiDataException, DurationExceededException {
        log.info("Populating Queue with " + enqueue.getFileName());
        return enqueueImpl(enqueue);
    }

    private EnqueueResult enqueueImpl(EnqueueRequest enqueue)
            throws IOException, InvalidMidiDataException, DurationExceededException {
        Closer closer = Closer.create();
        try {

            Sequence sequence = getSequence(closer, enqueue.getContent());
            int durationInSecs = getDuration(sequence);
            validateDuration(durationInSecs);

            enqueue.setDuration(durationInSecs);

            String id = UUID.randomUUID().toString();
            int timeToPlay = playCommandService.estimateTimeToPlayQueue();
            ObjectId fileReference = playCommandService.createFile(enqueue.getFileName(), CONTENT_TYPE, enqueue.getContent(),
                    id);

            enqueue.setTrackName(TrackNameUtil.getSequenceName(sequence).orElse(null));
            enqueue.setPlayStatus(PlayStatus.ENQUEUED);
            enqueue.setCommandId(id);

            playCommandService.storeEnqueueRequest(enqueue, fileReference);

            EnqueueResult result = new EnqueueResult();
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
            throw new DurationExceededException(
                    "Duration " + durationInSecs + " too short, min duration is: " + MINIMAL_DURATION_SEC);
        }

        if (durationInSecs > MAXIMAL_DURATION_SEC) {
            throw new DurationExceededException(
                    "Duration " + durationInSecs + " too long, min duration is: " + MAXIMAL_DURATION_SEC);
        }
    }

    private int getDuration(Sequence sequence) throws IOException, InvalidMidiDataException {
        try {
            // Create a sequencer for the sequence

            Sequencer sequencer = getSequencer();
            synchronized (this) {
                sequencer.setSequence(sequence);
                return (int) (sequencer.getMicrosecondLength() / 1000000.0);
            }

        } catch (MidiUnavailableException e) {
            log.warn(e.getMessage(), e);
            return -1;
        }
    }

    private Sequencer getSequencer() throws MidiUnavailableException {
        Sequencer sequencer = EnqueueTrack.sequencer;

        if (sequencer == null) {
            sequencer = MidiSystem.getSequencer();
            if (!sequencer.isOpen()) {
                sequencer.open();
            }
            EnqueueTrack.sequencer = sequencer;
        }
        return sequencer;
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
