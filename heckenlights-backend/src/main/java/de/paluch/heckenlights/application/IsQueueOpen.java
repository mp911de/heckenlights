package de.paluch.heckenlights.application;

import de.paluch.heckenlights.repositories.StateService;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

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
