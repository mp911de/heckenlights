package de.paluch.heckenlights.application;

import java.util.Set;

import org.springframework.stereotype.Component;

import com.google.common.collect.ImmutableSet;

import de.paluch.heckenlights.model.Rule;
import de.paluch.heckenlights.model.RuleState;
import de.paluch.heckenlights.repositories.StateService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
@Component
@RequiredArgsConstructor
public class GetOnlineState {

    public final static Set<Rule.Action> ONLINE_ACTIONS = ImmutableSet.of(Rule.Action.LIGHTS_ON, Rule.Action.PLAYLIST,
            Rule.Action.PLAYLIST_AUTO_ENQEUE);

    public final static Set<Rule.Action> PLAYBACK_ACTIONS = ImmutableSet.of(Rule.Action.PLAYLIST,
            Rule.Action.PLAYLIST_AUTO_ENQEUE);

    @NonNull
    RuleState ruleState;

    @NonNull
    StateService stateService;

    public boolean isOnline() {

        if (!stateService.isOnline()) {
            return false;
        }

        if (ruleState.getActiveAction() != null && ONLINE_ACTIONS.contains(ruleState.getActiveAction())) {
            return true;
        }

        return false;
    }

    public boolean isProcessingPlayback() {

        if (ruleState.getActiveAction() != null && PLAYBACK_ACTIONS.contains(ruleState.getActiveAction())) {
            return true;
        }

        return false;
    }
}
