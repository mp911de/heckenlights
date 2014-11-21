package de.paluch.heckenlights.rest;

import com.google.common.collect.Lists;
import de.paluch.heckenlights.application.EnqueueTrack;
import de.paluch.heckenlights.application.GetOnlineState;
import de.paluch.heckenlights.application.GetPlaylist;
import de.paluch.heckenlights.application.IsQueueOpen;
import de.paluch.heckenlights.model.DurationExceededException;
import de.paluch.heckenlights.model.EnqueueRequest;
import de.paluch.heckenlights.model.EnqueueResult;
import de.paluch.heckenlights.model.OfflineException;
import de.paluch.heckenlights.model.PlayCommandSummary;
import de.paluch.heckenlights.model.PlayStatus;
import de.paluch.heckenlights.model.QuotaExceededException;
import org.apache.commons.io.FilenameUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import javax.sound.midi.InvalidMidiDataException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.Date;
import java.util.List;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 * @since 28.11.13 21:52
 */
@Component
@RestController
public class HeckenlightsResource {

    @Inject
    private EnqueueTrack enqueueTrack;

    @Inject
    private GetPlaylist getPlaylist;

	@Inject
	private GetOnlineState getOnlineState;

    @Inject
    private IsQueueOpen isQueueOpen;

    @RequestMapping(value = "/", produces = { MediaType.TEXT_XML, MediaType.APPLICATION_JSON }, method = RequestMethod.POST)
    public ResponseEntity<EnqueueResponseRepresentation> uploadFile(
            @RequestHeader(value = "X-Submission-Host", required = false) String submissionHost,
            @RequestHeader(value = "X-External-SessionId", required = false) String sessionId,
            @RequestHeader(value = "X-Request-FileName", required = false) String fileName, @RequestBody byte[] input)
            throws IOException, InvalidMidiDataException, DurationExceededException {

        if (input == null || input.length == 0) {
            return new ResponseEntity<>(new EnqueueResponseRepresentation(PlayStatus.ERROR, "No data attached"),
                    HttpStatus.BAD_REQUEST);
        }

        try {

            EnqueueRequest model = createModel(submissionHost, sessionId, fileName, input);
            EnqueueResult enqueResult = enqueueTrack.enqueueWithQuotaCheck(model);

            EnqueueResponseRepresentation result = toResult(enqueResult);
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (InvalidMidiDataException | IOException e) {
            return new ResponseEntity<>(new EnqueueResponseRepresentation(PlayStatus.ERROR, e.getMessage()),
                    HttpStatus.BAD_REQUEST);

        } catch (DurationExceededException e) {
            return new ResponseEntity<>(new EnqueueResponseRepresentation(PlayStatus.DURATION_EXCEEDED, e.getMessage()),
                    HttpStatus.BAD_REQUEST);

        } catch (QuotaExceededException e) {
            return new ResponseEntity<>(new EnqueueResponseRepresentation(PlayStatus.QUOTA, e.getMessage()),
                    HttpStatus.TOO_MANY_REQUESTS);

        } catch (OfflineException e) {
            return new ResponseEntity<>(new EnqueueResponseRepresentation(PlayStatus.OFFLINE, e.getMessage()),
                    HttpStatus.LOCKED);

        }

    }

    @RequestMapping(value = "/", produces = { MediaType.TEXT_XML, MediaType.APPLICATION_JSON }, method = RequestMethod.GET)
    public PlayCommandsRepresentation find(@QueryParam("playStatus") PlayStatus playStatus) {
        List<PlayCommandSummary> playlist = getPlaylist.getPlaylist(playStatus);
		boolean isOnline = getOnlineState.isOnline();
		PlayCommandsRepresentation result = new PlayCommandsRepresentation();
		result.setOnline(isOnline);
        result.setQueueOpen(isQueueOpen.isQueueOpen());

        for (PlayCommandSummary summaryModel : playlist) {
            PlayCommandRepresentation playCommandRepresentation = new PlayCommandRepresentation();

            toPlayCommand(summaryModel, playCommandRepresentation);
            result.getPlayCommands().add(playCommandRepresentation);
        }

        return result;
    }

    private void toPlayCommand(PlayCommandSummary summaryModel, PlayCommandRepresentation playCommandRepresentation) {
        playCommandRepresentation.setCreated(summaryModel.getCreated());
        playCommandRepresentation.setDuration(summaryModel.getDuration());
        playCommandRepresentation.setException(summaryModel.getException());
        playCommandRepresentation.setExternalSessionId(summaryModel.getExternalSessionId());
        playCommandRepresentation.setId(summaryModel.getId());
        playCommandRepresentation.setPlayStatus(summaryModel.getPlayStatus());
        playCommandRepresentation.setSubmissionHost(summaryModel.getSubmissionHost());
        playCommandRepresentation.setTrackName(summaryModel.getTrackName());
        playCommandRepresentation.setCaptures(toCaptures(summaryModel.getCaptures()));
        playCommandRepresentation.setTimeToStart(summaryModel.getTimeToStart());
        playCommandRepresentation.setRemaining(summaryModel.getRemaining());
    }

    private List<PlayCaptureRepresentation> toCaptures(List<Date> dates) {
        List<PlayCaptureRepresentation> result = Lists.newArrayList();

        for (Date captureDate : dates) {
            PlayCaptureRepresentation captureRepresentation = new PlayCaptureRepresentation();
            captureRepresentation.setId(result.size());
            captureRepresentation.setCreated(captureDate);

            result.add(captureRepresentation);

        }

        return result;
    }

    private EnqueueResponseRepresentation toResult(EnqueueResult model) {
        EnqueueResponseRepresentation result = new EnqueueResponseRepresentation();
        result.setEnqueuedCommandId(model.getCommandId());
        result.setMessage(model.getException());
        result.setDurationToPlay(model.getDurationToPlay());
        result.setTrackName(model.getTrackName());
        result.setPlayStatus(PlayStatus.ENQUEUED);
        return result;
    }

    private EnqueueRequest createModel(String submissionHost, String sessionId, String fileName, byte[] input) {

        byte[] bytes = input;

        EnqueueRequest model = new EnqueueRequest();

        model.setFileName(FilenameUtils.getName(fileName));
        model.setContent(bytes);
        model.setCreated(new Date());
        model.setExternalSessionId(sessionId);
        model.setSubmissionHost(submissionHost);
        return model;
    }

    @RequestMapping(value = "{id}", produces = { MediaType.TEXT_XML, MediaType.APPLICATION_JSON }, method = RequestMethod.GET)
    public PlayCommandRepresentation find(@PathVariable("id") String id) {
        PlayCommandSummary playCommand = getPlaylist.getPlayCommand(id);
        if (playCommand == null) {
            throw new NotFoundException("PlayCommand with id " + id + " not found");
        }

        PlayCommandRepresentation result = new PlayCommandRepresentation();

        toPlayCommand(playCommand, result);
        return result;

    }

}
