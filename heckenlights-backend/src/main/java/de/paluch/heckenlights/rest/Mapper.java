package de.paluch.heckenlights.rest;

import com.google.common.collect.Lists;
import de.paluch.heckenlights.model.EnqueueRequest;
import de.paluch.heckenlights.model.EnqueueResult;
import de.paluch.heckenlights.model.PlayCommandSummary;
import de.paluch.heckenlights.model.PlayStatus;
import org.apache.commons.io.FilenameUtils;

import java.util.Date;
import java.util.List;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
class Mapper {
    private Mapper() {

    }

    static void toPlayCommand(PlayCommandSummary summaryModel, PlayCommandRepresentation playCommandRepresentation) {
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

    static private List<PlayCaptureRepresentation> toCaptures(List<Date> dates) {
        List<PlayCaptureRepresentation> result = Lists.newArrayList();

        for (Date captureDate : dates) {
            PlayCaptureRepresentation captureRepresentation = new PlayCaptureRepresentation();
            captureRepresentation.setId(result.size());
            captureRepresentation.setCreated(captureDate);

            result.add(captureRepresentation);

        }

        return result;
    }

    static EnqueueResponseRepresentation toResult(EnqueueResult model) {
        EnqueueResponseRepresentation result = new EnqueueResponseRepresentation();
        result.setEnqueuedCommandId(model.getCommandId());
        result.setMessage(model.getException());
        result.setDurationToPlay(model.getDurationToPlay());
        result.setTrackName(model.getTrackName());
        result.setPlayStatus(PlayStatus.ENQUEUED);
        return result;
    }

    static EnqueueRequest createModel(String submissionHost, String sessionId, String fileName, byte[] input) {

        byte[] bytes = input;

        EnqueueRequest model = new EnqueueRequest();

        model.setFileName(FilenameUtils.getName(fileName));
        model.setContent(bytes);
        model.setCreated(new Date());
        model.setExternalSessionId(sessionId);
        model.setSubmissionHost(submissionHost);
        return model;
    }
}
