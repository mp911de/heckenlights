package de.paluch.heckenlights.application;

import de.paluch.heckenlights.model.DurationExceededException;
import de.paluch.heckenlights.model.EnqueueModel;
import de.paluch.heckenlights.model.EnqueueResult;
import de.paluch.heckenlights.model.PlayStatus;
import de.paluch.heckenlights.repositories.PlayCommandRepositoryService;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.UUID;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 * @since 28.11.13 21:47
 */
@Component
public class Enqueue
{
    @Inject
    private PlayCommandRepositoryService playCommandRepositoryService;

    private final static int MINIMAL_DURATION_SEC = 10;
    private final static int MAXIMAL_DURATION_SEC = 300;

    public EnqueueResult enqueue(EnqueueModel enqueue)
            throws IOException, InvalidMidiDataException, DurationExceededException
    {

        int durationInSecs = getDuration(enqueue);
        validateDuration(durationInSecs);

        enqueue.setDuration(durationInSecs);

        String id = UUID.randomUUID().toString();
        int timeToPlay = playCommandRepositoryService.estimateTimeToPlayQueue();
        ObjectId fileReference =
                playCommandRepositoryService.createFile(enqueue.getFileName(), "audio/midi", enqueue.getContent(), id);

        EnqueueResult result = new EnqueueResult();

        result.setDurationToPlay(timeToPlay);
        result.setCommandId(id);
        enqueue.setPlayStatus(PlayStatus.ENQUEUED);

        playCommandRepositoryService.storeEnqueueRequest(enqueue, fileReference);

        return result;
    }

    private void validateDuration(int durationInSecs) throws DurationExceededException
    {
        if (durationInSecs < MINIMAL_DURATION_SEC)
        {
            throw new DurationExceededException(
                    "Duration " + durationInSecs + " too short, min duration is: " + MINIMAL_DURATION_SEC);
        }

        if (durationInSecs > MAXIMAL_DURATION_SEC)
        {
            throw new DurationExceededException(
                    "Duration " + durationInSecs + " too long, min duration is: " + MAXIMAL_DURATION_SEC);
        }
    }

    private int getDuration(EnqueueModel enqueue) throws IOException, InvalidMidiDataException
    {
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(enqueue.getContent()))
        {
            Sequence sequence = MidiSystem.getSequence(byteArrayInputStream);

            // Create a sequencer for the sequence
            Sequencer sequencer = MidiSystem.getSequencer();
            sequencer.open();
            sequencer.setSequence(sequence);

            int durationInSecs = (int) (sequencer.getMicrosecondLength() / 1000000.0);
            return durationInSecs;

        } catch (MidiUnavailableException e)
        {
            return -1;
        }
    }
}
