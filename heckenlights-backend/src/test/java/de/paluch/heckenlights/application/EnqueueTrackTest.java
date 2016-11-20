package de.paluch.heckenlights.application;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.InputStream;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.util.StreamUtils;

import de.paluch.heckenlights.model.EnqueueRequest;
import de.paluch.heckenlights.model.EnqueueResult;
import de.paluch.heckenlights.repositories.PlayCommandService;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 * @since 17.02.14 20:35
 */

@RunWith(MockitoJUnitRunner.class)
public class EnqueueTrackTest {

    @InjectMocks
    private EnqueueTrack sut;

    @Mock
    private PlayCommandService playCommandService;

    @Before
    public void before() throws Exception {
        sut = new EnqueueTrack(playCommandService, null, null, null);
    }

    @Test
    public void testEnqueue() throws Exception {

        EnqueueRequest model = new EnqueueRequest();
        InputStream is = getClass().getResourceAsStream("/P-Christmas_Carols_-_Winter_Wonderland.mid");
        model.setContent(StreamUtils.copyToByteArray(is));

        model.setDuration(12);
        EnqueueResult result = sut.populate(model);

        assertThat(model.getTrackName()).isEqualTo("Seq-1");
        assertThat(result.getTrackName()).isEqualTo("Seq-1");
        assertThat(result.getDurationToPlay()).isEqualTo(0);
    }
}
