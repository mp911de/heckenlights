package de.paluch.heckenlights.application;

import de.paluch.heckenlights.model.EnqueueModel;
import de.paluch.heckenlights.repositories.PlayCommandService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.util.StreamUtils;

import java.io.InputStream;

import static org.junit.Assert.assertEquals;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 * @since 17.02.14 20:35
 */

@RunWith(MockitoJUnitRunner.class)
public class EnqueueTest {

    @InjectMocks
    private Enqueue sut = new Enqueue();

    @Mock
    private PlayCommandService playCommandService;

    @Test
    public void testEnqueue() throws Exception {

        EnqueueModel model = new EnqueueModel();
        InputStream is = getClass().getResourceAsStream("/P-Christmas_Carols_-_Winter_Wonderland.mid");
        model.setContent(StreamUtils.copyToByteArray(is));

        model.setDuration(12);
        sut.enqueue(model);

        assertEquals("Seq-1", model.getTrackName());

    }
}
