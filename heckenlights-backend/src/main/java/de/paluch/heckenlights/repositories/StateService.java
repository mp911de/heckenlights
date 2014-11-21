package de.paluch.heckenlights.repositories;

import org.springframework.stereotype.Service;

import javax.inject.Inject;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
@Service
public class StateService {

    @Inject
    private StateRepository stateRepository;

    public boolean isOnline() {

        StateDocument state = getState();
        return state.isOnline();
    }

    public boolean isQueueOpen() {

        StateDocument state = getState();
        return state.isQueueOpen();
    }

    public boolean isQueueProcessorActive() {

        StateDocument state = getState();
        return state.isQueueProcessorActive();
    }

    private StateDocument getState() {
        StateDocument state = stateRepository.findOne("State");
        if (state == null) {
            state = new StateDocument();
            state.setId("State");
            state.setOnline(true);
            state.setQueueOpen(true);
            state.setQueueProcessorActive(true);
            stateRepository.save(state);
        }
        return state;
    }

}
