package de.paluch.heckenlights.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import com.google.common.collect.ImmutableList;
import de.paluch.heckenlights.client.MidiRelayClient;
import de.paluch.heckenlights.client.PlayerStateRepresentation;
import de.paluch.heckenlights.model.PlayCommandSummary;
import de.paluch.heckenlights.model.Rule;
import de.paluch.heckenlights.model.RuleState;
import de.paluch.heckenlights.model.TrackContent;
import de.paluch.heckenlights.repositories.PlayCommandService;
import de.paluch.heckenlights.repositories.StateService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.TimeZone;

@RunWith(MockitoJUnitRunner.class)
public class ProcessQueueTest {

    @Mock
    private MidiRelayClient client;

    @Mock
    private PlayCommandService playCommandService;

    @Mock
    private PopulateQueue populateQueue;

    @Mock
    private ResolveRule resolveRule;

    @Mock
    private StateService stateService;

    @InjectMocks
    private ProcessQueue sut = new ProcessQueue();

    private RuleState ruleState = new RuleState();

    @Before
    public void before() throws Exception {
        ReflectionTestUtils.setField(sut, "ruleState", ruleState);
        ReflectionTestUtils.setField(sut, "clock", Clock.systemDefaultZone());
        when(stateService.isQueueProcessorActive()).thenReturn(true);
    }

    @Test
    public void testNoState() throws Exception {

        when(resolveRule.getRule()).thenReturn(new ResolveRule.FallbackRule(Rule.Action.PLAYLIST));
        sut.processQueue();

        verify(client).getState();
        verifyZeroInteractions(playCommandService);
        verifyZeroInteractions(populateQueue);
        verifyNoMoreInteractions(client);
    }

    @Test
    public void testRunning() throws Exception {

        when(resolveRule.getRule()).thenReturn(new ResolveRule.FallbackRule(Rule.Action.PLAYLIST));
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

        when(resolveRule.getRule()).thenReturn(new ResolveRule.FallbackRule(Rule.Action.PLAYLIST_AUTO_ENQEUE));
        PlayerStateRepresentation psr = new PlayerStateRepresentation();
        psr.setRunning(false);

        when(client.getState()).thenReturn(psr);

        PlayCommandSummary playCommandSummary = new PlayCommandSummary();
        playCommandSummary.setId("the-id");

        TrackContent trackContent = new TrackContent();
        trackContent.setId(playCommandSummary.getId());
        trackContent.setFilename("the-file");
        trackContent.setContent(new byte[] { 1, 2, 3 });

        when(playCommandService.getEnquedCommands()).thenReturn(ImmutableList.of(playCommandSummary));
        when(playCommandService.getTrackContent(playCommandSummary.getId())).thenReturn(trackContent);

        sut.processQueue();

        verify(client).play(trackContent.getId(), trackContent.getFilename(), trackContent.getContent());

    }

    @Test
    public void testNotPlayingNoAutoEnqueue() throws Exception {

        when(resolveRule.getRule()).thenReturn(new ResolveRule.FallbackRule(Rule.Action.PLAYLIST));
        PlayerStateRepresentation psr = new PlayerStateRepresentation();
        psr.setRunning(false);

        when(client.getState()).thenReturn(psr);

        sut.processQueue();

        verify(client).getState();
        verifyNoMoreInteractions(client);
    }

    @Test
    public void testEmptyQueue() throws Exception {

        when(resolveRule.getRule()).thenReturn(new ResolveRule.FallbackRule(Rule.Action.PLAYLIST_AUTO_ENQEUE));

        PlayerStateRepresentation psr = new PlayerStateRepresentation();
        psr.setRunning(false);

        when(client.getState()).thenReturn(psr);

        sut.processQueue();

        verify(populateQueue).populateQueue();

    }

    public void setTime(String time) {

        LocalDateTime lt = LocalDateTime.parse("2007-12-03T" + time + ".00");

        Clock clock = Clock.fixed(lt.toInstant(ZoneOffset.UTC), TimeZone.getTimeZone("Europe/Berlin").toZoneId());

        ReflectionTestUtils.setField(sut, "clock", clock);
    }

    @Test
    public void testUpdatePlayTime() throws Exception {

        when(resolveRule.getRule()).thenReturn(new ResolveRule.FallbackRule(Rule.Action.PLAYLIST_AUTO_ENQEUE));
        PlayerStateRepresentation psr = new PlayerStateRepresentation();
        psr.setRunning(true);

        ruleState.setActiveAction(Rule.Action.PLAYLIST);

        when(client.getState()).thenReturn(psr);

        setTime("10:00:00");
        sut.processQueue();

        assertThat(ruleState.getPlaylistPlayedTimeMs()).isEqualTo(0);

        sut.processQueue();
        assertThat(ruleState.getPlaylistPlayedTimeMs()).isEqualTo(0);

        setTime("10:01:00");
        sut.processQueue();
        assertThat(ruleState.getPlaylistPlayedTimeMs()).isEqualTo(60000);
        assertThat(ruleState.getLightsOnTimeMs()).isEqualTo(0);

        psr.setRunning(false);

        setTime("10:02:00");
        sut.processQueue();
        assertThat(ruleState.getPlaylistPlayedTimeMs()).isEqualTo(120000);
        assertThat(ruleState.getLightsOnTimeMs()).isEqualTo(0);

        setTime("10:03:00");
        sut.processQueue();
        assertThat(ruleState.getPlaylistPlayedTimeMs()).isEqualTo(120000);
        assertThat(ruleState.getLightsOnTimeMs()).isEqualTo(60000);

    }

    @Test
    public void testUpdateLightsOnTime() throws Exception {

        when(resolveRule.getRule()).thenReturn(new ResolveRule.FallbackRule(Rule.Action.PLAYLIST_AUTO_ENQEUE));
        PlayerStateRepresentation psr = new PlayerStateRepresentation();

        ruleState.setActiveAction(Rule.Action.LIGHTS_ON);

        when(client.getState()).thenReturn(psr);

        setTime("10:00:00");
        sut.processQueue();

        assertThat(ruleState.getLightsOnTimeMs()).isEqualTo(0);

        sut.processQueue();
        assertThat(ruleState.getLightsOnTimeMs()).isEqualTo(0);

        setTime("10:01:00");
        sut.processQueue();
        assertThat(ruleState.getLightsOnTimeMs()).isEqualTo(60000);
        assertThat(ruleState.getPlaylistPlayedTimeMs()).isEqualTo(0);

    }

    @Test
    public void testReset() throws Exception {

        ResolveRule.FallbackRule rule = new ResolveRule.FallbackRule(Rule.Action.PLAYLIST_AUTO_ENQEUE);
        rule.getReset().add(Rule.Counter.LightsOnDuration);
        rule.getReset().add(Rule.Counter.PlaylistPlayedDuration);

        when(resolveRule.getRule()).thenReturn(rule);
        PlayerStateRepresentation psr = new PlayerStateRepresentation();
        psr.setRunning(false);

        ruleState.setActiveAction(Rule.Action.LIGHTS_ON);
        ruleState.setPlaylistPlayedTimeMs(100);
        ruleState.setLightsOnTimeMs(100);
        ruleState.setActiveAction(Rule.Action.LIGHTS_ON);
        when(client.getState()).thenReturn(psr);

        setTime("10:00:00");
        sut.processQueue();

        assertThat(ruleState.getLightsOnTimeMs()).isEqualTo(0);
        assertThat(ruleState.getPlaylistPlayedTimeMs()).isEqualTo(0);

        rule = new ResolveRule.FallbackRule(Rule.Action.PLAYLIST);
        rule.getReset().add(Rule.Counter.PlaylistPlayedDuration);
        when(resolveRule.getRule()).thenReturn(rule);

        ruleState.setPlaylistPlayedTimeMs(100);
        ruleState.setLightsOnTimeMs(100);

        sut.processQueue();
        assertThat(ruleState.getLightsOnTimeMs()).isEqualTo(100);
        assertThat(ruleState.getPlaylistPlayedTimeMs()).isEqualTo(0);

    }

    @Test
    public void lightsOff() throws Exception {

        when(resolveRule.getRule()).thenReturn(new ResolveRule.FallbackRule(Rule.Action.LIGHTS_OFF));
        PlayerStateRepresentation psr = new PlayerStateRepresentation();
        psr.setRunning(false);
        when(client.getState()).thenReturn(psr);

        sut.processQueue();

        verify(client).switchOff();

    }

    @Test
    public void lightsOffWhenRunning() throws Exception {

        when(resolveRule.getRule()).thenReturn(new ResolveRule.FallbackRule(Rule.Action.LIGHTS_OFF));
        PlayerStateRepresentation psr = new PlayerStateRepresentation();
        psr.setRunning(true);
        when(client.getState()).thenReturn(psr);

        sut.processQueue();

        verify(client, never()).switchOff();

    }

    @Test
    public void lightsOn() throws Exception {

        when(resolveRule.getRule()).thenReturn(new ResolveRule.FallbackRule(Rule.Action.LIGHTS_ON));
        PlayerStateRepresentation psr = new PlayerStateRepresentation();
        psr.setRunning(false);
        when(client.getState()).thenReturn(psr);

        sut.processQueue();

        verify(client).switchOn();

    }
}
