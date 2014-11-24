package de.paluch.heckenlights.application;

import javax.inject.Inject;

import org.springframework.stereotype.Component;

import de.paluch.heckenlights.repositories.StateService;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 * @since 28.11.13 21:49
 */
@Component
public class IsQueueOpen {

    @Inject
    private StateService stateService;

    public boolean isQueueOpen() {
        return stateService.isQueueOpen();
    }

}
