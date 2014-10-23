package de.paluch.heckenlights.application;


import static org.assertj.core.api.Assertions.assertThat;

import de.paluch.heckenlights.model.EnqueueModel;
import de.paluch.heckenlights.model.EnqueueResultModel;
import de.paluch.heckenlights.repositories.PlayCommandService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.util.StreamUtils;

import java.io.InputStream;

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
		EnqueueResultModel result = sut.enqueue(model);

		assertThat(model.getTrackName()).isEqualTo("Seq-1");
		assertThat(result.getTrackName()).isEqualTo("Seq-1");
		assertThat(result.getDurationToPlay()).isEqualTo(0);

    }
}
