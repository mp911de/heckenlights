package de.paluch.heckenlights.application;

import de.paluch.heckenlights.model.PlayCommandSummary;
import de.paluch.heckenlights.model.PlayStatus;
import de.paluch.heckenlights.repositories.PlayCommandService;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 * @since 28.11.13 21:49
 */
@Component
public class GetPlaylist
{

    @Inject
    private PlayCommandService playCommandService;

    public List<PlayCommandSummary> getPlaylist(PlayStatus playStatus)
    {
        if (playStatus == null)
        {
            return playCommandService
                    .getListByPlayStatusOrderByCreated(Arrays.asList(PlayStatus.PLAYING, PlayStatus.ENQUEUED), 20);
        }

        return playCommandService.getListByPlayStatusOrderByCreated(Arrays.asList(playStatus), 20);
    }

    public PlayCommandSummary getPlayCommand(String id)
    {
        return playCommandService.getPlayCommand(id);
    }
}
