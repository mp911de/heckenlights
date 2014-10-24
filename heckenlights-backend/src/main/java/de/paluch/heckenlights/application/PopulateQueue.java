package de.paluch.heckenlights.application;

import javax.inject.Inject;
import javax.sound.midi.InvalidMidiDataException;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Date;

import com.google.common.io.Files;
import de.paluch.heckenlights.model.DurationExceededException;
import de.paluch.heckenlights.model.EnqueueRequest;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
@Component
public class PopulateQueue {

    private Logger log = Logger.getLogger(getClass());

    private String midiDirectory;

    @Inject
    private EnqueueTrack enqueueTrack;

    public void populateQueue() throws IOException, InvalidMidiDataException, DurationExceededException {

        log.info("Populating queue");

        File[] files = getFiles(null);
        if (files != null) {
            log.info("Populating queue, found " + files.length + " file(s)");

            for (File file : files) {
                byte[] bytes = Files.asByteSource(file).read();

                EnqueueRequest model = new EnqueueRequest();
                model.setContent(bytes);
                model.setExternalSessionId(getClass().getSimpleName());
                model.setSubmissionHost(getClass().getSimpleName());
                model.setFileName(FilenameUtils.getName(file.getName()));

                model.setCreated(new Date());
                enqueueTrack.enqueue(model);
            }
        }
    }

    private File[] getFiles(String fileName) {

        if (fileName == null) {
            File file = new File(midiDirectory);

            return file.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.toLowerCase().endsWith(".mid");
                }
            });
        }

        File file = new File(midiDirectory, fileName);
        if (file.exists()) {
            return new File[] { file };
        }

        return new File[0];
    }

    public String getMidiDirectory() {
        return midiDirectory;
    }

    public void setMidiDirectory(String midiDirectory) {
        this.midiDirectory = midiDirectory;
    }
}
