package de.paluch.heckenlights.application;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.concurrent.TimeUnit;

import javax.xml.bind.JAXB;

import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.google.common.io.Resources;

import de.paluch.heckenlights.model.Rule;
import de.paluch.heckenlights.model.RuleState;
import de.paluch.heckenlights.model.Rules;

public class ResolveRuleTest {

    private ResolveRule sut;
    private RuleState ruleState;

    @Before
    public void before() throws Exception {

        ruleState = new RuleState();
        Rules rules = JAXB.unmarshal(Resources.getResource("rules.xml"), Rules.class);

        sut = new ResolveRule(ruleState, rules, Clock.systemDefaultZone());
    }

    @Test
    public void lightsOffDuringTheDay() throws Exception {
        setTime("10:10");
        Rule result = sut.getRule();
        assertThat(result.getAction()).isEqualTo(Rule.Action.OFFLINE);

        setTime("22:01");
        result = sut.getRule();
        assertThat(result.getAction()).isEqualTo(Rule.Action.OFFLINE);

        setTime("23:00");
        result = sut.getRule();
        assertThat(result.getAction()).isEqualTo(Rule.Action.OFFLINE);

        setTime("00:00");
        result = sut.getRule();
        assertThat(result.getAction()).isEqualTo(Rule.Action.OFFLINE);
    }

    @Test
    public void startWithPlaylistAutoEnqueue() throws Exception {
        setTime("17:00");
        Rule result = sut.getRule();
        assertThat(result.getAction()).isEqualTo(Rule.Action.PLAYLIST_AUTO_ENQEUE);

        setTime("18:00");
        result = sut.getRule();
        assertThat(result.getAction()).isEqualTo(Rule.Action.PLAYLIST_AUTO_ENQEUE);

        ruleState.setPlaylistSize(100);

        result = sut.getRule();
        assertThat(result.getAction()).isEqualTo(Rule.Action.PLAYLIST_AUTO_ENQEUE);
    }

    @Test
    public void switchLightsOnAndThenAutoEnqueue() throws Exception {
        ruleState.setPlaylistPlayedTimeMs(TimeUnit.MILLISECONDS.convert(11, TimeUnit.MINUTES));

        setTime("17:00");
        Rule result = sut.getRule();
        assertThat(result.getAction()).isEqualTo(Rule.Action.LIGHTS_ON);
        assertThat(result.getReset()).isEmpty();

        ruleState.setPlaylistPlayedTimeMs(TimeUnit.MILLISECONDS.convert(5, TimeUnit.MINUTES));

        setTime("18:00");
        result = sut.getRule();
        assertThat(result.getAction()).isEqualTo(Rule.Action.PLAYLIST_AUTO_ENQEUE);

    }

    @Test
    public void lightsOnAfterPlayAndLightsOn() throws Exception {
        ruleState.setPlaylistPlayedTimeMs(TimeUnit.MILLISECONDS.convert(11, TimeUnit.MINUTES));
        ruleState.setLightsOnTimeMs(TimeUnit.MILLISECONDS.convert(11, TimeUnit.MINUTES));

        setTime("17:20");
        Rule result = sut.getRule();
        assertThat(result.getAction()).isEqualTo(Rule.Action.LIGHTS_ON);
        assertThat(result.getReset()).isNotEmpty();

    }

    @Test
    public void resetCounterRule() throws Exception {
        ruleState.setPlaylistPlayedTimeMs(TimeUnit.MILLISECONDS.convert(11, TimeUnit.MINUTES));
        ruleState.setLightsOnTimeMs(TimeUnit.MILLISECONDS.convert(11, TimeUnit.MINUTES));

        setTime("17:00");
        Rule result = sut.getRule();
        assertThat(result.getAction()).isEqualTo(Rule.Action.LIGHTS_ON);
        assertThat(result.getReset()).contains(Rule.Counter.PlaylistPlayedDuration, Rule.Counter.LightsOnDuration);

        ruleState.setPlaylistPlayedTimeMs(TimeUnit.MILLISECONDS.convert(11, TimeUnit.MINUTES));
        ruleState.setLightsOnTimeMs(TimeUnit.MILLISECONDS.convert(5, TimeUnit.MINUTES));

        setTime("18:00");
        result = sut.getRule();
        assertThat(result.getAction()).isEqualTo(Rule.Action.LIGHTS_ON);
        assertThat(result.getReset()).isEmpty();

        ruleState.setPlaylistPlayedTimeMs(TimeUnit.MILLISECONDS.convert(5, TimeUnit.MINUTES));
        ruleState.setLightsOnTimeMs(TimeUnit.MILLISECONDS.convert(5, TimeUnit.MINUTES));

        setTime("18:00");
        result = sut.getRule();
        assertThat(result.getAction()).isEqualTo(Rule.Action.PLAYLIST_AUTO_ENQEUE);
        assertThat(result.getReset()).isEmpty();

    }

    @Test
    public void startWithPlaylist() throws Exception {
        ruleState.setPlaylistSize(100);

        setTime("05:00");
        Rule result = sut.getRule();
        assertThat(result.getAction()).isEqualTo(Rule.Action.PLAYLIST);

        setTime("07:59");
        result = sut.getRule();
        assertThat(result.getAction()).isEqualTo(Rule.Action.PLAYLIST);
    }

    @Test
    public void lightsOnQueueEmpty() throws Exception {
        ruleState.setPlaylistSize(0);

        setTime("05:00");
        Rule result = sut.getRule();
        assertThat(result.getAction()).isEqualTo(Rule.Action.LIGHTS_ON);

    }

    public void setTime(String time) {

        ZonedDateTime lt = ZonedDateTime.parse("2007-12-03T" + time + ":30.00+01:00[Europe/Berlin]");

        Clock clock = Clock.fixed(lt.toInstant(), ZoneId.of("Europe/Berlin"));

        ReflectionTestUtils.setField(sut, "clock", clock);

    }
}
