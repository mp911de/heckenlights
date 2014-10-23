package de.paluch.heckenlights.repositories;

import static org.springframework.data.mongodb.core.query.Criteria.*;
import static org.springframework.data.mongodb.core.query.Query.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import com.google.common.cache.Cache;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.bson.types.ObjectId;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsOperations;
import org.springframework.stereotype.Component;

import com.google.common.collect.ImmutableList;
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
import de.paluch.heckenlights.model.TrackContentModel;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 * @since 28.11.13 22:22
 */
@Component
public class PlayCommandService {

    private Logger log = Logger.getLogger(getClass());

    @Inject
    private PlayCommandRepository playCommandRepository;

    @Inject
    private MidiRelayClient client;

    @Inject
    private GridFsOperations gridFsOperations;

    private final static int COMMAND_OVERHEAD_SEC = 5;

    public ObjectId createFile(String fileName, String contentType, byte[] content, String parentId) {
        // audio/midi
        DBObject metadata = new BasicDBObject();
        metadata.put("parentId", parentId);
        metadata.put("created", new Date());
        GridFSFile fsFile = gridFsOperations.store(new ByteArrayInputStream(content), fileName, contentType, metadata);

        return (ObjectId) fsFile.getId();
    }

    public int estimateTimeToPlayQueue() {
        List<PlayCommandDocument> queuedCommands = playCommandRepository.findByPlayStatusOrderByCreatedAsc(PlayStatus.ENQUEUED);
        int result = 0;

        for (PlayCommandDocument queuedCommand : queuedCommands) {
            result += queuedCommand.getDuration() + COMMAND_OVERHEAD_SEC;
        }

        PlayerStateRepresentation state = client.getState();
        if (state != null && state.isRunning()) {
            result += state.getEstimatedSecondsToPlay();
        }

        return result;
    }

    public void storeEnqueueRequest(EnqueueModel enqueue, ObjectId fileReference) {

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

    public List<PlayCommandSummaryModel> getEnquedCommands() {
        List<PlayCommandDocument> documents = getPlayCommandDocuments(ImmutableList.of(PlayStatus.ENQUEUED), 100);
        List<PlayCommandSummaryModel> result = Lists.newArrayList();

        for (PlayCommandDocument playCommandDocument : documents) {
            PlayCommandSummaryModel summaryModel = toSummaryModel(playCommandDocument);

            result.add(summaryModel);
        }
        return result;
    }

    public List<PlayCommandSummaryModel> getListByPlayStatusOrderByCreated(List<PlayStatus> states, int limit) {

        List<PlayCommandDocument> documents = getPlayCommandDocuments(states, limit);
        List<PlayCommandSummaryModel> result = Lists.newArrayList();

        int timeToStart = 0;
        PlayerStateRepresentation state = client.getState();

        if (state != null && state.getTrack() != null) {
			timeToStart = appendCurrentTrack(result, timeToStart, state);
        }

        for (PlayCommandDocument playCommandDocument : documents) {
            PlayCommandSummaryModel summaryModel = toSummaryModel(playCommandDocument);
            int trackTimeToPlay = playCommandDocument.getDuration();

            summaryModel.setTimeToStart(timeToStart);
            if (state != null && state.getTrack() != null) {
                if (summaryModel.getId().equals(state.getTrack().getId())) {
                    continue;
                }
            }

            timeToStart += trackTimeToPlay;

            summaryModel.setCaptures(getDateOfFiles(playCommandDocument.getCaptures()));

            result.add(summaryModel);
            if (result.size() > limit) {
                break;
            }
        }

        return result;
    }

	private int appendCurrentTrack(List<PlayCommandSummaryModel> result, int timeToStart, PlayerStateRepresentation state) {
		PlayCommandDocument playCommandDocument = playCommandRepository.findOne(state.getTrack().getId());
		if (playCommandDocument != null) {
			PlayCommandSummaryModel currentTrack = toSummaryModel(playCommandDocument);
			currentTrack.setPlayStatus(PlayStatus.PLAYING);
			currentTrack.setTimeToStart(0);
			currentTrack.setRemaining(state.getEstimatedSecondsToPlay());
			timeToStart += state.getEstimatedSecondsToPlay();
			result.add(currentTrack);
		}
		return timeToStart;
	}

	public TrackContentModel getTrackContent(String id) throws IOException {
        PlayCommandDocument playCommandDocument = playCommandRepository.findOne(id);
        if (playCommandDocument == null) {
            return null;
        }

        GridFSDBFile file = gridFsOperations.findOne(query(where("_id").is(playCommandDocument.getAttachedFile())));
        if (file == null) {
            throw new IllegalStateException("Cannot find file for playCommand " + id);
        }

        TrackContentModel result = new TrackContentModel();
        result.setId(id);

        try (ByteArrayOutputStream buffer = new ByteArrayOutputStream(); InputStream is = file.getInputStream()) {

            IOUtils.copy(is, buffer);
            result.setContent(buffer.toByteArray());
            result.setFilename(file.getFilename());

        }

        return result;
    }

    private List<PlayCommandDocument> getPlayCommandDocuments(List<PlayStatus> states, int limit) {
        List<PlayCommandDocument> documents = new ArrayList<>();

        for (PlayStatus playStatus : states) {
            documents.addAll(playCommandRepository.findByPlayStatusOrderByCreatedAsc(playStatus, new PageRequest(0, limit)));
        }
        return documents;
    }

    public PlayCommandSummaryModel getPlayCommand(String id) {
        PlayCommandDocument playCommandDocument = playCommandRepository.findOne(id);
        if (playCommandDocument != null) {
            PlayCommandSummaryModel summaryModel = toSummaryModel(playCommandDocument);
            summaryModel.setCaptures(getDateOfFiles(playCommandDocument.getCaptures()));
            return summaryModel;
        }

        return null;
    }

    private List<Date> getDateOfFiles(List<ObjectId> objectIds) {
        List<Date> result = Lists.newArrayList();

        for (ObjectId objectId : objectIds) {
            GridFSDBFile file = gridFsOperations.findOne(new Query(where("_id").is(objectId)));

            result.add(file.getUploadDate());
        }

        return result;
    }

    public void setStateExecuted(String id) {
        PlayCommandDocument playCommandDocument = playCommandRepository.findOne(id);
        if (playCommandDocument == null) {
            throw new IllegalStateException("Cannot find playCommand " + id);
        }

        playCommandDocument.setPlayStatus(PlayStatus.EXECUTED);
		playCommandRepository.save(playCommandDocument);

    }

    private PlayCommandSummaryModel toSummaryModel(PlayCommandDocument from) {
        PlayCommandSummaryModel result = new PlayCommandSummaryModel();

        result.setCreated(from.getCreated());
        result.setDuration(from.getDuration());
        result.setRemaining(from.getDuration());
        result.setException(from.getException());
        result.setExternalSessionId(from.getExternalSessionId());
        result.setId(from.getId());
        result.setPlayStatus(from.getPlayStatus());
        result.setSubmissionHost(from.getSubmissionHost());
        result.setTrackName(from.getTrackName());

        return result;
    }

}
