package de.paluch.heckenlights.repositories;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.gridfs.GridFSFile;
import de.paluch.heckenlights.client.MidiRelayClient;
import de.paluch.heckenlights.model.EnqueueModel;
import de.paluch.heckenlights.model.PlayCommand;
import de.paluch.heckenlights.model.PlayStatus;
import org.apache.log4j.Logger;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.gridfs.GridFsOperations;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.io.ByteArrayInputStream;
import java.util.Date;
import java.util.List;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 * @since 28.11.13 22:22
 */
@Component
public class PlayCommandRepositoryService
{

    private Logger log = Logger.getLogger(getClass());

    @Inject
    private PlayCommandRepository playCommandRepository;

    @Inject
    private MidiRelayClient client;

    @Inject
    private GridFsOperations gridFsOperations;

    private final static int COMMAND_OVERHEAD_SEC = 5;

    public ObjectId createFile(String fileName, String contentType, byte[] content, String parentId)
    {
        // audio/midi
        DBObject metadata = new BasicDBObject();
        metadata.put("parentId", parentId);
        metadata.put("created", new Date());
        GridFSFile fsFile = gridFsOperations.store(new ByteArrayInputStream(content), fileName, contentType, metadata);

        return (ObjectId) fsFile.getId();
    }

    public int estimateTimeToPlayQueue()
    {
        List<PlayCommand> queuedCommands = playCommandRepository.findByPlayStatusOrderByCreatedAsc(PlayStatus.ENQUEUED);
        int result = 0;

        for (PlayCommand queuedCommand : queuedCommands)
        {
            result += queuedCommand.getDuration() + COMMAND_OVERHEAD_SEC;
        }

        List<PlayCommand> playing = playCommandRepository.findByPlayStatusOrderByCreatedAsc(PlayStatus.PLAYING);

        if (!playing.isEmpty())
        {
            if (playing.size() > 1)
            {
                log.warn("Found " + playing.size() + " PlayCommands in state PLAYING");
            }

            PlayCommand playCommand = playing.get(0);
            String playingTrackId = client.getCurrentPlayId();
            if (playingTrackId != null && playingTrackId.equals(playCommand.getId()))
            {
                int remainingSeconds = client.getRemainingTime();
                result += remainingSeconds;
            }

        }

        return result;
    }

    public void storeEnqueueRequest(EnqueueModel enqueue, ObjectId fileReference)
    {

        PlayCommand command = new PlayCommand();

        command.setAttachedFile(fileReference);
        command.setCreated(enqueue.getCreated());
        command.setDuration(enqueue.getDuration());
        command.setId(enqueue.getCommandId());
        command.setPlayStatus(PlayStatus.ENQUEUED);
        command.setSubmissionHost(enqueue.getSubmissionHost());
        command.setExternalSessionId(enqueue.getExternalSessionId());
        command.setTrackName(enqueue.getTrackName());

        playCommandRepository.save(command);

    }
}
