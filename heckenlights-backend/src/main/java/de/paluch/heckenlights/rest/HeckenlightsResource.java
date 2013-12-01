package de.paluch.heckenlights.rest;

import de.paluch.heckenlights.application.Enqueue;
import de.paluch.heckenlights.model.DurationExceededException;
import de.paluch.heckenlights.model.EnqueueModel;
import de.paluch.heckenlights.model.EnqueueResult;
import de.paluch.heckenlights.model.PlayStatus;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.sound.midi.InvalidMidiDataException;
import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
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

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
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
        EnqueueResult enqueResult = enqueue.enqueue(model);

        EnqueueResponseRepresentation result = toResult(enqueResult);

        return result;
    }

    private EnqueueResponseRepresentation toResult(EnqueueResult model)
    {
        EnqueueResponseRepresentation result = new EnqueueResponseRepresentation();
        result.setEnqueuedCommandId(model.getCommandId());
        result.setMessage(model.getException());
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
