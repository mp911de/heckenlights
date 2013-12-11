package de.paluch.heckenlights.rest;

import com.google.common.collect.Lists;
import de.paluch.heckenlights.application.Enqueue;
import de.paluch.heckenlights.application.GetPlaylist;
import de.paluch.heckenlights.model.DurationExceededException;
import de.paluch.heckenlights.model.EnqueueModel;
import de.paluch.heckenlights.model.EnqueueResultModel;
import de.paluch.heckenlights.model.PlayCommandSummaryModel;
import de.paluch.heckenlights.model.PlayStatus;
import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.sound.midi.InvalidMidiDataException;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 * @since 28.11.13 21:52
 */
@Path("heckenlights")
@Component
public class HeckenlightsResource
{

    @Inject
    private Enqueue enqueue;

    @Inject
    private GetPlaylist getPlaylist;

    @PUT
    @Produces({ MediaType.TEXT_XML, MediaType.APPLICATION_JSON })
    public EnqueueResponseRepresentation uploadFile(@HeaderParam("X-Submission-Host") String submissionHost,
                                                    @HeaderParam("X-External-SessionId") String sessionId,
                                                    @HeaderParam("X-Request-FileName") String fileName, byte[] input)
            throws IOException, InvalidMidiDataException, DurationExceededException
    {

        if (input == null || input.length == 0)
        {
            return new EnqueueResponseRepresentation(PlayStatus.ERROR, "No data attached");
        }

        EnqueueModel model = createModel(submissionHost, sessionId, fileName, input);
        EnqueueResultModel enqueResult = enqueue.enqueue(model);

        EnqueueResponseRepresentation result = toResult(enqueResult);

        return result;
    }

    @GET
    @Produces({ MediaType.TEXT_XML, MediaType.APPLICATION_JSON })
    @Path("{id}")
    public PlayCommandRepresentation find(@PathParam("id") String id)
    {
        PlayCommandSummaryModel playCommand = getPlaylist.getPlayCommand(id);
        if (playCommand == null)
        {
            throw new NotFoundException("PlayCommand with id " + id + " not found");
        }

        PlayCommandRepresentation result = new PlayCommandRepresentation();

        toPlayCommand(playCommand, result);
        return result;

    }

    @GET
    @Produces({ MediaType.APPLICATION_OCTET_STREAM })
    @Path("{id}/captures/{captureId}")
    public InputStream find(@PathParam("id") String id, @PathParam("captureId") int captureId)
    {
        return null;

    }

    @GET
    @Produces({ MediaType.TEXT_XML, MediaType.APPLICATION_JSON })
    public PlayCommandsRepresentation find(@QueryParam("playStatus") PlayStatus playStatus)
    {
        List<PlayCommandSummaryModel> playlist = getPlaylist.getPlaylist(playStatus);
        PlayCommandsRepresentation result = new PlayCommandsRepresentation();

        for (PlayCommandSummaryModel summaryModel : playlist)
        {
            PlayCommandRepresentation playCommandRepresentation = new PlayCommandRepresentation();

            toPlayCommand(summaryModel, playCommandRepresentation);
            result.getPlayCommands().add(playCommandRepresentation);
        }

        return result;
    }

    private void toPlayCommand(PlayCommandSummaryModel summaryModel,
                               PlayCommandRepresentation playCommandRepresentation)
    {
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
    }

    private List<PlayCaptureRepresentation> toCaptures(List<Date> dates)
    {
        List<PlayCaptureRepresentation> result = Lists.newArrayList();

        for (Date captureDate : dates)
        {
            PlayCaptureRepresentation captureRepresentation = new PlayCaptureRepresentation();
            captureRepresentation.setId(result.size());
            captureRepresentation.setCreated(captureDate);

            result.add(captureRepresentation);

        }

        return result;
    }

    private EnqueueResponseRepresentation toResult(EnqueueResultModel model)
    {
        EnqueueResponseRepresentation result = new EnqueueResponseRepresentation();
        result.setEnqueuedCommandId(model.getCommandId());
        result.setMessage(model.getException());
        result.setDurationToPlay(model.getDurationToPlay());
        return result;
    }

    private EnqueueModel createModel(String submissionHost, String sessionId, String fileName, byte[] input)
    {

        byte[] bytes = input;

        EnqueueModel model = new EnqueueModel();

        model.setFileName(FilenameUtils.getName(fileName));

        model.setContent(bytes);
        model.setCreated(new Date());
        model.setExternalSessionId(sessionId);
        model.setSubmissionHost(submissionHost);
        return model;
    }

}
