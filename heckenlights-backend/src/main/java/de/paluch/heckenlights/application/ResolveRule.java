package de.paluch.heckenlights.application;

import javax.inject.Inject;
import java.time.Clock;
import java.time.LocalTime;

import de.paluch.heckenlights.model.Rule;
import de.paluch.heckenlights.model.RuleState;
import de.paluch.heckenlights.model.Rules;
import org.springframework.stereotype.Component;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
@Component
public class ResolveRule {

    @Inject
    private RuleState ruleState;

    @Inject
    private Rules rules;

    @Inject
    private Clock clock;

    public Rule getRule() {

        LocalTime zonedDateTime = LocalTime.now(clock);
        for (Rule rule : rules.getRules()) {
            int hour = zonedDateTime.getHour();
            int minute = zonedDateTime.getMinute();

            boolean matchFrom = false;
            boolean matchTo = false;
            boolean matchQueueSize = false;
            boolean matchPlayedDuration = false;

            if (rule.getHourFrom() < hour || (rule.getHourFrom() == hour && rule.getMinuteFrom() >= minute)) {
                matchFrom = true;
            }

            if (rule.getHourTo() > hour || (rule.getHourTo() == hour && rule.getMinuteTo() < minute)) {
                matchTo = true;
            }

            if (rule.getQueueIsEmpty() != null) {
                if (rule.getQueueIsEmpty().booleanValue() && ruleState.getPlaylistSize() == 0) {
                    matchQueueSize = true;
                }

                if (!rule.getQueueIsEmpty().booleanValue() && ruleState.getPlaylistSize() != 0) {
                    matchQueueSize = true;
                }
            } else {
                matchQueueSize = true;
            }

            if (rule.getMaxPlaylistPlayedDuration() != null) {
                long ms = rules.getTimeunit().toMillis(rule.getMaxPlaylistPlayedDuration().longValue());
                matchPlayedDuration = true;
                if (ms < ruleState.getPlaylistPlayedTimeMs()) {
                    matchPlayedDuration = false;
                }
            } else {
                matchPlayedDuration = true;
            }

            if (matchFrom && matchQueueSize && matchTo && matchPlayedDuration) {
                return rule;
            }

        }

        return new FallbackRule(rules.getDefaultAction());
    }

    private class FallbackRule extends Rule {

        private FallbackRule(Action action) {
            setAction(action);
        }
    }
}
