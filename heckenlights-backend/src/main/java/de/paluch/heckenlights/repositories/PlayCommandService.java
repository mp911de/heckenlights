package de.paluch.heckenlights.repositories;

import com.google.common.collect.Lists;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSFile;
import de.paluch.heckenlights.client.MidiRelayClient;
import de.paluch.heckenlights.client.PlayerStateRepresentation;
import de.paluch.heckenlights.model.EnqueueModel;
import de.paluch.heckenlights.model.PlayCommandSummaryModel;
import de.paluch.heckenlights.model.PlayStatus;
import org.apache.log4j.Logger;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsOperations;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 * @since 28.11.13 22:22
 */
@Component
public class PlayCommandService
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
        List<PlayCommandDocument> queuedCommands =
                playCommandRepository.findByPlayStatusOrderByCreatedAsc(PlayStatus.ENQUEUED);
        int result = 0;

        for (PlayCommandDocument queuedCommand : queuedCommands)
        {
            result += queuedCommand.getDuration() + COMMAND_OVERHEAD_SEC;
        }

        List<PlayCommandDocument> playing = playCommandRepository.findByPlayStatusOrderByCreatedAsc(PlayStatus.PLAYING);

        if (!playing.isEmpty())
        {
            if (playing.size() > 1)
            {
                log.warn("Found " + playing.size() + " PlayCommands in state PLAYING");
            }

            PlayCommandDocument playCommandDocument = playing.get(0);
            String playingTrackId = client.getCurrentPlayId();
            if (playingTrackId != null && playingTrackId.equals(playCommandDocument.getId()))
            {
                int remainingSeconds = client.getRemainingTime();
                result += remainingSeconds;
            }

        }

        return result;
    }

    public void storeEnqueueRequest(EnqueueModel enqueue, ObjectId fileReference)
    {

        PlayCommandDocument command = new PlayCommandDocument();

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

    public List<PlayCommandSummaryModel> getListByPlayStatusOrderByCreated(List<PlayStatus> states, int limit)
    {

        List<PlayCommandDocument> documents = new ArrayList<>();

        for (PlayStatus playStatus : states)
        {
            documents.addAll(playCommandRepository.findByPlayStatusOrderByCreatedAsc(playStatus));
        }

        PlayerStateRepresentation state = client.getState();

        int timeToStart = 0;
        List<PlayCommandSummaryModel> result = Lists.newArrayList();
        for (PlayCommandDocument playCommandDocument : documents)
        {
            PlayCommandSummaryModel summaryModel = toSummaryModel(playCommandDocument);
            int trackTimeToPlay = playCommandDocument.getDuration();

            summaryModel.setTimeToStart(timeToStart);
            if (state.getTrack() != null)
            {
                if (summaryModel.getId().equals(state.getTrack().getId()))
                {
                    trackTimeToPlay = state.getEstimatedSecondsToPlay();
                }
            }

            timeToStart += trackTimeToPlay;

            summaryModel.setCaptures(getDateOfFiles(playCommandDocument.getCaptures()));

            result.add(summaryModel);
            if (result.size() > limit)
            {
                break;
            }
        }

        return result;

    }

    public PlayCommandSummaryModel getPlayCommand(String id)
    {
        PlayCommandDocument playCommandDocument = playCommandRepository.findOne(id);
        if (playCommandDocument != null)
        {
            PlayCommandSummaryModel summaryModel = toSummaryModel(playCommandDocument);
            summaryModel.setCaptures(getDateOfFiles(playCommandDocument.getCaptures()));
            return summaryModel;
        }

        return null;
    }

    private List<Date> getDateOfFiles(List<ObjectId> objectIds)
    {
        List<Date> result = Lists.newArrayList();

        for (ObjectId objectId : objectIds)
        {
            GridFSDBFile file = gridFsOperations.findOne(new Query(Criteria.where("_id").is(objectId)));

            result.add(file.getUploadDate());
        }

        return result;
    }

    private PlayCommandSummaryModel toSummaryModel(PlayCommandDocument from)
    {
        PlayCommandSummaryModel result = new PlayCommandSummaryModel();

        result.setCreated(from.getCreated());
        result.setDuration(from.getDuration());
        result.setException(from.getException());
        result.setExternalSessionId(from.getExternalSessionId());
        result.setId(from.getId());
        result.setPlayStatus(from.getPlayStatus());
        result.setSubmissionHost(from.getSubmissionHost());
        result.setTrackName(from.getTrackName());

        return result;
    }

}
