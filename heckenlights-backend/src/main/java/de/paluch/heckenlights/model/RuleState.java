package de.paluch.heckenlights.model;

import lombok.Data;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
@Data
public class RuleState {

    long playlistPlayedTimeMs = 0;
    long lightsOnTimeMs = 0;
    Rule activeRule;
    Rule.Action activeAction;
    long ruleActiveSince = 0;
    int playlistSize = 0;
    boolean playing = false;
    boolean switchedPlayState = false;

    public void addPlaylistPlayedTimeMs(long timeMs) {
        playlistPlayedTimeMs += timeMs;
    }

    public void addLightsOnTimeMs(long timeMs) {
        lightsOnTimeMs += timeMs;
    }

}
