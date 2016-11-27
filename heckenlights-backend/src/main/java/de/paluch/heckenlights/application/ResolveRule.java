package de.paluch.heckenlights.application;

import java.time.Clock;
import java.time.LocalTime;
import java.util.Arrays;

import org.springframework.stereotype.Component;

import com.google.common.collect.ImmutableSet;

import de.paluch.heckenlights.model.Rule;
import de.paluch.heckenlights.model.RuleState;
import de.paluch.heckenlights.model.Rules;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
@Component
@RequiredArgsConstructor
public class ResolveRule {

    @NonNull
    RuleState ruleState;

    @NonNull
    RuleService ruleService;

    @NonNull
    Clock clock;

    public Rule getRule() {

        LocalTime zonedDateTime = LocalTime.now(clock);

        Rules rules = ruleService.getRules();

        for (Rule rule : rules.getRules()) {
            int hour = zonedDateTime.getHour();
            int minute = zonedDateTime.getMinute();

            boolean matchFrom = false;
            boolean matchTo = false;
            boolean matchQueueSize = false;
            boolean matchPlayedDuration = false;
            boolean matchLightsOnDuration = false;

            if (rule.getHourFrom() < hour || (rule.getHourFrom() == hour && rule.getMinuteFrom() <= minute)) {
                matchFrom = true;
            }

            if (rule.getHourTo() > hour || (rule.getHourTo() == hour && rule.getMinuteTo() >= minute)) {
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

            if (rule.getMinLightsOnDuration() != null) {
                long ms = rules.getTimeunit().toMillis(rule.getMinLightsOnDuration().longValue());
                matchLightsOnDuration = false;
                if (ruleState.getLightsOnTimeMs() >= ms) {
                    matchLightsOnDuration = true;
                }
            } else {
                matchLightsOnDuration = true;
            }

            if (matchFrom && matchQueueSize && matchTo && matchPlayedDuration && matchLightsOnDuration) {
                return rule;
            }

        }

        return new FallbackRule(rules.getDefaultAction(), Rule.Counter.LightsOnDuration, Rule.Counter.PlaylistPlayedDuration);
    }

    public static class FallbackRule extends Rule {

        public FallbackRule(Action action) {
            setAction(action);
        }

        public FallbackRule(Action action, Counter... reset) {
            setAction(action);
            setReset(ImmutableSet.copyOf(Arrays.asList(reset)));
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof Rule)) {
                return false;
            }

            Rule rule = (Rule) o;

            if (getAction() != rule.getAction()) {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode() {
            return getAction() != null ? getAction().hashCode() : 0;
        }

    }
}
