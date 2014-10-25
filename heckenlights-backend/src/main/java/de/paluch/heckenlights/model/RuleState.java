package de.paluch.heckenlights.model;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
public class RuleState {

    private long playlistPlayedTimeMs = 0;
    private long staticOnTime = 0;
    private Rule activeRule;
    private Rule.Action activeAction;
    private long ruleActiveSince = 0;
    private int playlistSize = 0;

    public long getPlaylistPlayedTimeMs() {
        return playlistPlayedTimeMs;
    }

    public void setPlaylistPlayedTimeMs(long playlistPlayedTimeMs) {
        this.playlistPlayedTimeMs = playlistPlayedTimeMs;
    }

    public long getStaticOnTime() {
        return staticOnTime;
    }

    public void setStaticOnTime(long staticOnTime) {
        this.staticOnTime = staticOnTime;
    }

    public Rule getActiveRule() {
        return activeRule;
    }

    public void setActiveRule(Rule activeRule) {
        this.activeRule = activeRule;
    }

    public Rule.Action getActiveAction() {
        return activeAction;
    }

    public void setActiveAction(Rule.Action activeAction) {
        this.activeAction = activeAction;
    }

    public long getRuleActiveSince() {
        return ruleActiveSince;
    }

    public void setRuleActiveSince(long ruleActiveSince) {
        this.ruleActiveSince = ruleActiveSince;
    }

    public int getPlaylistSize() {
        return playlistSize;
    }

    public void setPlaylistSize(int playlistSize) {
        this.playlistSize = playlistSize;
    }
}
