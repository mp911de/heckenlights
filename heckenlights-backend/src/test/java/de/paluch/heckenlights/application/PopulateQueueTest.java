package de.paluch.heckenlights.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import com.google.common.io.Resources;
import de.paluch.heckenlights.model.EnqueueRequest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;
import java.nio.file.Paths;

@RunWith(MockitoJUnitRunner.class)
public class PopulateQueueTest {

    public static final String RESOURCE_NAME = "P-Christmas_Carols_-_Winter_Wonderland.mid";

	@InjectMocks
    private PopulateQueue sut = new PopulateQueue();

    @Mock
    private EnqueueTrack enqueueTrack;

    @Captor
    private ArgumentCaptor<EnqueueRequest> enqueueCaptor;

    @Before
    public void before() throws Exception {
        File file = Paths.get(Resources.getResource(RESOURCE_NAME).toURI()).toFile();
        sut.setMidiDirectory(file.getParentFile().getCanonicalPath());
    }

    @Test
    public void testEnqueue() throws Exception {
        sut.populateQueue();

        verify(enqueueTrack).populate(enqueueCaptor.capture());
        EnqueueRequest value = enqueueCaptor.getValue();

        assertThat(value.getFileName()).isEqualTo(RESOURCE_NAME);
        assertThat(value.getExternalSessionId()).isEqualTo(PopulateQueue.class.getSimpleName());
        assertThat(value.getSubmissionHost()).isEqualTo(PopulateQueue.class.getSimpleName());
    }

    @Test
    public void testNoFiles() throws Exception {
        sut.setMidiDirectory("non-existent");

        sut.populateQueue();

        verifyZeroInteractions(enqueueTrack);
    }
}
