package de.paluch.heckenlights.repositories;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import de.paluch.heckenlights.model.PlayStatus;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 * @since 28.11.13 21:10
 */

@CompoundIndexes({@CompoundIndex(name = "PlayCommand_session_host", def ="{externalSessionId: 1, submissionHost: 1, created: 1}")})
@Document(collection = "PlayCommand")
public class PlayCommandDocument
{
    @Id
    private String id;

    @Indexed
    private Date created;

    private String trackName;

	@Indexed
    private PlayStatus playStatus;

    private ObjectId attachedFile;

    private int duration;

    private String externalSessionId;

    private String submissionHost;

    private String exception;

    private List<ObjectId> captures = new ArrayList<>();

    public String getId()
    {
        return id;
    }
    public void setId(String id)
    {
        this.id = id;
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
    public PlayStatus getPlayStatus()
    {
        return playStatus;
    }
    public void setPlayStatus(PlayStatus playStatus)
    {
        this.playStatus = playStatus;
    }
    public ObjectId getAttachedFile()
    {
        return attachedFile;
    }
    public void setAttachedFile(ObjectId attachedFile)
    {
        this.attachedFile = attachedFile;
    }
    public int getDuration()
    {
        return duration;
    }
    public void setDuration(int duration)
    {
        this.duration = duration;
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
    public String getException()
    {
        return exception;
    }
    public void setException(String exception)
    {
        this.exception = exception;
    }
    public List<ObjectId> getCaptures()
    {
        return captures;
    }
    public void setCaptures(List<ObjectId> captures)
    {
        this.captures = captures;
    }

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof PlayCommandDocument)) return false;

		PlayCommandDocument that = (PlayCommandDocument) o;

		if (id != null ? !id.equals(that.id) : that.id != null) return false;

		return true;
	}

	@Override
	public int hashCode() {
		return id != null ? id.hashCode() : 0;
	}
}
