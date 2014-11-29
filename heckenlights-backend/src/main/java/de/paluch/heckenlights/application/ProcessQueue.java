package de.paluch.heckenlights.application;

import java.io.IOException;
import java.time.Clock;
import java.util.List;

import javax.inject.Inject;
import javax.sound.midi.InvalidMidiDataException;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import de.paluch.heckenlights.client.MidiRelayClient;
import de.paluch.heckenlights.client.PlayerStateRepresentation;
import de.paluch.heckenlights.model.DurationExceededException;
import de.paluch.heckenlights.model.PlayCommandSummary;
import de.paluch.heckenlights.model.Rule;
import de.paluch.heckenlights.model.RuleState;
import de.paluch.heckenlights.model.TrackContent;
import de.paluch.heckenlights.repositories.PlayCommandService;
import de.paluch.heckenlights.repositories.StateService;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 * @since 28.11.13 21:47
 */
@Component
public class ProcessQueue {

    private Logger log = Logger.getLogger(getClass());

    @Inject
    private MidiRelayClient client;

    @Inject
    private PlayCommandService playCommandService;

    @Inject
    private PopulateQueue populateQueue;

    @Inject
    private RuleState ruleState;

    @Inject
    private ResolveRule resolveRule;

    @Inject
    private StateService stateService;

    @Inject
    private Clock clock;

    private long lastScanMs = -1;

    public void processQueue() throws IOException, InvalidMidiDataException, DurationExceededException {

        if (!stateService.isQueueProcessorActive()) {
            return;
        }

        updateLastScan();

        Rule rule = resolveRule.getRule();
        PlayerStateRepresentation state = client.getState();

        if (prematureExit(rule, state)) {
            return;
        }

        List<PlayCommandSummary> commands = playCommandService.getEnquedCommands();
        ruleState.setPlaylistSize(commands.size());

        boolean ruleSwitched = false;
        boolean actionSwitched = false;

        if (ruleState.getActiveRule() == null || !rule.equals(ruleState.getActiveRule())) {
            ruleState.setRuleActiveSince(lastScanMs);
            ruleState.setActiveRule(rule);
            ruleSwitched = true;
        }

        if (ruleState.getActiveAction() != rule.getAction()) {
            ruleState.setActiveAction(rule.getAction());
            actionSwitched = true;
        }

        if (ruleSwitched || actionSwitched) {

            resetCounters(rule);
            log.info("Switched to Rule with action " + ruleState.getActiveAction() + " (" + rule + ")");
        }

        if (ruleState.getActiveAction() == Rule.Action.PLAYLIST_AUTO_ENQEUE
                || ruleState.getActiveAction() == Rule.Action.PLAYLIST) {
            playlist(commands, ruleSwitched, actionSwitched);
        }

        if (ruleState.getActiveAction() == Rule.Action.LIGHTS_ON) {
            lightsOn(ruleSwitched, actionSwitched);
        }

        if (ruleState.getActiveAction() == Rule.Action.LIGHTS_OFF || ruleState.getActiveAction() == Rule.Action.OFFLINE) {
            lightsOff(ruleSwitched, actionSwitched);
        }
    }

    private void resetCounters(Rule rule) {

        boolean resetAll = false;

        if (rule.getAction() == Rule.Action.LIGHTS_OFF || rule.getAction() == Rule.Action.OFFLINE) {
            resetAll = true;
        }

        if (resetAll || rule.getReset().contains(Rule.Counter.LightsOnDuration)) {
            log.info("Reset " + Rule.Counter.LightsOnDuration);
            ruleState.setLightsOnTimeMs(0);
        }

        if (resetAll || rule.getReset().contains(Rule.Counter.PlaylistPlayedDuration)) {
            log.info("Reset " + Rule.Counter.PlaylistPlayedDuration);
            ruleState.setPlaylistPlayedTimeMs(0);
        }
    }

    private boolean prematureExit(Rule rule, PlayerStateRepresentation state) {
        if (rule == null) {
            log.warn("Rule is null");
            return true;
        }

        if (state == null) {
            log.warn("Received null state");
            return true;
        }

        if (ruleState.isPlaying() != state.isRunning()) {
            ruleState.setSwitchedPlayState(true);
        } else {
            ruleState.setSwitchedPlayState(false);
        }

        ruleState.setPlaying(state.isRunning());

        if (state.isRunning()) {
            return true;
        }
        return false;
    }

    private void lightsOn(boolean ruleSwitched, boolean actionSwitched) {
        if (ruleSwitched || actionSwitched) {
            client.switchOn();
        }
    }

    private void lightsOff(boolean ruleSwitched, boolean actionSwitched) {
        if (ruleSwitched || actionSwitched) {
            client.switchOff();
        }
    }

    private void playlist(List<PlayCommandSummary> commands, boolean ruleSwitched, boolean actionSwitched) throws IOException,
            InvalidMidiDataException, DurationExceededException {
        if (commands.isEmpty()) {
            if (ruleState.getActiveAction() == Rule.Action.PLAYLIST_AUTO_ENQEUE) {
                populateQueue.populateQueue();
            }

            if (ruleState.isSwitchedPlayState()) {
                lightsOn(true, true);
            }

        } else {
            PlayCommandSummary playCommand = commands.get(0);
            TrackContent trackContent = playCommandService.getTrackContent(playCommand.getId());
            log.info("Triggering play of " + trackContent.getFilename() + ", duration " + playCommand.getDuration()
                    + " secs submitted by " + playCommand.getSubmissionHost());
            client.play(trackContent.getId(), trackContent.getFilename(), trackContent.getContent());
            playCommandService.setStateExecuted(trackContent.getId());
        }
    }

    private void updateLastScan() {
        boolean found = false;
        if (lastScanMs != -1) {
            if (!found && ruleState.isPlaying()) {
                long played = clock.millis() - lastScanMs;
                ruleState.addPlaylistPlayedTimeMs(played);
                found = true;
            }

            if (!found
                    && (ruleState.getActiveAction() == Rule.Action.LIGHTS_ON
                            || ruleState.getActiveAction() == Rule.Action.PLAYLIST || ruleState.getActiveAction() == Rule.Action.PLAYLIST_AUTO_ENQEUE)) {
                long played = clock.millis() - lastScanMs;
                ruleState.addLightsOnTimeMs(played);
                found = true;
            }
        }

        lastScanMs = clock.millis();
    }
}
