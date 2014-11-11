package biz.paluch.heckenlights.messagebox.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import biz.paluch.heckenlights.messagebox.client.midirelay.MidiRelayClient;
import biz.paluch.heckenlights.messagebox.client.midirelay.PlayerStateRepresentation;
import biz.paluch.heckenlights.messagebox.client.midirelay.PlayerStateTrackRepresentation;

@RunWith(MockitoJUnitRunner.class)
public class GetCurrentTitleTest {
    @InjectMocks
    private GetCurrentTitle sut = new GetCurrentTitle();

    @Mock
    private MidiRelayClient midiRelayClient;

    @Test
    public void testSimple() throws Exception {

        secondsRemaining(5);
        String result = sut.getCurrentTitle();
        assertThat(result).isEqualTo("myfile.mid (0:05)");
    }

    @Test
    public void testLonger() throws Exception {

        secondsRemaining(60);
        String result = sut.getCurrentTitle();
        assertThat(result).isEqualTo("myfile.mid (1:00)");
    }

    private void secondsRemaining(int secondsRemaining) {
        PlayerStateRepresentation playerState = new PlayerStateRepresentation();
        playerState.setRunning(true);
        playerState.setEstimatedSecondsToPlay(secondsRemaining);

        PlayerStateTrackRepresentation track = new PlayerStateTrackRepresentation();
        track.setFileName("myfile.mid");
        playerState.setTrack(track);

        when(midiRelayClient.getState()).thenReturn(playerState);
    }
}