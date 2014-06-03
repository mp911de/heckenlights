package de.paluch.heckenlights.model;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 * @since 01.12.13 21:08
 */
public class EnqueueResultModel
{

    private String commandId;
    private int durationToPlay;
    private String exception;
    private String trackName;

    public String getCommandId()
    {
        return commandId;
    }
    public void setCommandId(String commandId)
    {
        this.commandId = commandId;
    }
    public int getDurationToPlay()
    {
        return durationToPlay;
    }
    public void setDurationToPlay(int durationToPlay)
    {
        this.durationToPlay = durationToPlay;
    }
    public String getException()
    {
        return exception;
    }
    public void setException(String exception)
    {
        this.exception = exception;
    }
    public String getTrackName()
    {
        return trackName;
    }
    public void setTrackName(String trackName)
    {
        this.trackName = trackName;
    }
}