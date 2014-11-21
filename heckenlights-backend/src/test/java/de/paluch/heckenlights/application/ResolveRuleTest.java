package de.paluch.heckenlights.application;

import static org.assertj.core.api.Assertions.assertThat;
import com.google.common.io.Resources;
import de.paluch.heckenlights.model.Rule;
import de.paluch.heckenlights.model.RuleState;
import de.paluch.heckenlights.model.Rules;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.xml.bind.JAXB;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class ResolveRuleTest {

    private ResolveRule sut = new ResolveRule();
    private RuleState ruleState;

    @Before
    public void before() throws Exception {

        ruleState = new RuleState();
        Rules rules = JAXB.unmarshal(Resources.getResource("rules.xml"), Rules.class);
        ReflectionTestUtils.setField(sut, "rules", rules);
        ReflectionTestUtils.setField(sut, "ruleState", ruleState);
    }

    @Test
    public void lightsOffDuringTheDay() throws Exception {
        setTime("10:10");
        Rule result = sut.getRule();
        assertThat(result.getAction()).isEqualTo(Rule.Action.OFFLINE);

        setTime("22:00");
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

        LocalDateTime lt = LocalDateTime.parse("2007-12-03T" + time + ":30.00");

        Clock clock = Clock.fixed(lt.toInstant(ZoneOffset.UTC), TimeZone.getTimeZone("Europe/Berlin").toZoneId());

        ReflectionTestUtils.setField(sut, "clock", clock);

    }
}
