package de.paluch.heckenlights.application;

import javax.inject.Inject;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import de.paluch.heckenlights.model.Rule;
import de.paluch.heckenlights.model.RuleState;
import org.springframework.stereotype.Component;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
@Component
public class GetOnlineState {

    public final static Set<Rule.Action> ONLINE_ACTIONS = ImmutableSet.of(Rule.Action.LIGHTS_ON, Rule.Action.PLAYLIST,
            Rule.Action.PLAYLIST_AUTO_ENQEUE);

    @Inject
    private RuleState ruleState;

    public boolean isOnline() {
        if (ruleState.getActiveAction() != null && ONLINE_ACTIONS.contains(ruleState.getActiveAction())) {
            return true;
        }

        return false;
    }
}
