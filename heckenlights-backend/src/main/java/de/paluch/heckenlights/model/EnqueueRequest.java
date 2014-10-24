package de.paluch.heckenlights.model;

import java.util.Date;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 * @since 28.11.13 21:49
 */
public class EnqueueRequest
{

    private String commandId;

    private Date created;

    private String trackName;

    private String fileName;

    private PlayStatus playStatus;

    private byte[] content;

    private String externalSessionId;

    private String submissionHost;

    private int duration;

    public String getCommandId()
    {
        return commandId;
    }
    public void setCommandId(String commandId)
    {
        this.commandId = commandId;
    }
    public Date getCreated()
    {
        return created;
    }
    public void setCreated(Date created)
    {
        this.created = created;
    }
    public String getTrackName()
    {
        return trackName;
    }
    public void setTrackName(String trackName)
    {
        this.trackName = trackName;
    }
    public String getFileName()
    {
        return fileName;
    }
    public void setFileName(String fileName)
    {
        this.fileName = fileName;
    }
    public PlayStatus getPlayStatus()
    {
        return playStatus;
    }
    public void setPlayStatus(PlayStatus playStatus)
    {
        this.playStatus = playStatus;
    }
    public byte[] getContent()
    {
        return content;
    }
    public void setContent(byte[] content)
    {
        this.content = content;
    }
    public String getExternalSessionId()
    {
        return externalSessionId;
    }
    public void setExternalSessionId(String externalSessionId)
    {
        this.externalSessionId = externalSessionId;
    }
    public String getSubmissionHost()
    {
        return submissionHost;
    }
    public void setSubmissionHost(String submissionHost)
    {
        this.submissionHost = submissionHost;
    }
    public int getDuration()
    {
        return duration;
    }
    public void setDuration(int duration)
    {
        this.duration = duration;
    }
}
