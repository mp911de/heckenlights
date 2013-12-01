package de.paluch.heckenlights.rest;

import de.paluch.heckenlights.model.PlayStatus;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 * @since 28.11.13 21:57
 */
public class EnqueueResponseRepresentation
{
    private PlayStatus playStatus;
    private String message;
    private String enqueuedCommandId;

    public EnqueueResponseRepresentation()
    {
    }

    public EnqueueResponseRepresentation(PlayStatus playStatus, String message)
    {
        this.playStatus = playStatus;
        this.message = message;
    }

    public PlayStatus getPlayStatus()
    {
        return playStatus;
    }
    public void setPlayStatus(PlayStatus playStatus)
    {
        this.playStatus = playStatus;
    }
    public String getMessage()
    {
        return message;
    }
    public void setMessage(String message)
    {
        this.message = message;
    }

    public String getEnqueuedCommandId()
    {
        return enqueuedCommandId;
    }
    public void setEnqueuedCommandId(String enqueuedCommandId)
    {
        this.enqueuedCommandId = enqueuedCommandId;
    }
}
