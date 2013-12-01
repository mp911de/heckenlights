package de.paluch.heckenlights.client;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 * @since 01.12.13 10:27
 */
public class PlayerStateTrackRepresentation
{

    private String id;
    private String sequenceName;
    private String fileName;
    private int duration;

    public String getId()
    {
        return id;
    }
    public void setId(String id)
    {
        this.id = id;
    }
    public String getSequenceName()
    {
        return sequenceName;
    }
    public void setSequenceName(String sequenceName)
    {
        this.sequenceName = sequenceName;
    }
    public String getFileName()
    {
        return fileName;
    }
    public void setFileName(String fileName)
    {
        this.fileName = fileName;
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
