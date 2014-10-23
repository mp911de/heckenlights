package de.paluch.heckenlights.application;

import static org.mockito.Mockito.*;

import com.google.common.collect.ImmutableList;
import de.paluch.heckenlights.model.PlayCommandSummaryModel;
import de.paluch.heckenlights.model.TrackContentModel;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import de.paluch.heckenlights.client.MidiRelayClient;
import de.paluch.heckenlights.client.PlayerStateRepresentation;
import de.paluch.heckenlights.repositories.PlayCommandService;

@RunWith(MockitoJUnitRunner.class)
public class ProcessQueueTest {

    @Mock
    private MidiRelayClient client;

    @Mock
    private PlayCommandService playCommandService;

    @Mock
    private PopulateQueue populateQueue;

    @InjectMocks
    private ProcessQueue sut = new ProcessQueue();

    @Test
    public void testNoState() throws Exception {

        sut.processQueue();

        verify(client).getState();
        verifyZeroInteractions(playCommandService);
        verifyZeroInteractions(populateQueue);
        verifyNoMoreInteractions(client);
    }

    @Test
    public void testRunning() throws Exception {

        PlayerStateRepresentation psr = new PlayerStateRepresentation();
        psr.setRunning(true);
		when(client.getState()).thenReturn(psr);

        sut.processQueue();

        verify(client).getState();
        verifyZeroInteractions(playCommandService);
        verifyZeroInteractions(populateQueue);
        verifyNoMoreInteractions(client);
    }

	@Test
    public void testNotPlaying() throws Exception {

        PlayerStateRepresentation psr = new PlayerStateRepresentation();
        psr.setRunning(false);

		when(client.getState()).thenReturn(psr);

		PlayCommandSummaryModel playCommandSummaryModel = new PlayCommandSummaryModel();
		playCommandSummaryModel.setId("the-id");

		TrackContentModel trackContentModel = new TrackContentModel();
		trackContentModel.setId(playCommandSummaryModel.getId());
		trackContentModel.setFilename("the-file");
		trackContentModel.setContent(new byte[]{1, 2, 3});

		when(playCommandService.getEnquedCommands()).thenReturn(ImmutableList.of(playCommandSummaryModel));
		when(playCommandService.getTrackContent(playCommandSummaryModel.getId())).thenReturn(trackContentModel);

        sut.processQueue();

		verify(client).play(trackContentModel.getId(),trackContentModel.getFilename(), trackContentModel.getContent());

    }

	@Test
    public void testEmptyQueue() throws Exception {

        PlayerStateRepresentation psr = new PlayerStateRepresentation();
        psr.setRunning(false);

		when(client.getState()).thenReturn(psr);

        sut.processQueue();

		verify(populateQueue).populateQueue();

    }
}
