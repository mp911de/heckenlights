package biz.paluch.heckenlights.messagebox.client.midirelay;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import java.util.Date;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 * @since 30.11.13 19:27
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PlayerStateRepresentation
{

    private boolean running;

    private Date started;
    private Date estimatedEnd;
    private int estimatedSecondsToPlay;

    private PlayerStateTrackRepresentation track;

    public boolean isRunning()
    {
        return running;
    }
    public void setRunning(boolean running)
    {
        this.running = running;
    }
    public Date getStarted()
    {
        return started;
    }
    public void setStarted(Date started)
    {
        this.started = started;
    }
    public Date getEstimatedEnd()
    {
        return estimatedEnd;
    }
    public void setEstimatedEnd(Date estimatedEnd)
    {
        this.estimatedEnd = estimatedEnd;
    }
    public int getEstimatedSecondsToPlay()
    {
        return estimatedSecondsToPlay;
    }
    public void setEstimatedSecondsToPlay(int estimatedSecondsToPlay)
    {
        this.estimatedSecondsToPlay = estimatedSecondsToPlay;
    }
    public PlayerStateTrackRepresentation getTrack()
    {
        return track;
    }
    public void setTrack(PlayerStateTrackRepresentation track)
    {
        this.track = track;
    }
}
