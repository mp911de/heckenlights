package de.paluch.heckenlights.application;

import org.springframework.stereotype.Component;

import de.paluch.heckenlights.repositories.StateService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 * @since 28.11.13 21:49
 */
@Component
@RequiredArgsConstructor
public class IsQueueOpen {

    @NonNull
    StateService stateService;
    @NonNull
    GetOnlineState getOnlineState;

    public boolean isQueueOpen() {
        return getOnlineState.isOnline() && stateService.isQueueOpen();
    }

}
