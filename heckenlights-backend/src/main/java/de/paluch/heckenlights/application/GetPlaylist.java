package de.paluch.heckenlights.application;

import java.util.Arrays;
import java.util.List;

import lombok.NonNull;
import org.springframework.stereotype.Component;

import de.paluch.heckenlights.model.PlayCommandSummary;
import de.paluch.heckenlights.model.PlayStatus;
import de.paluch.heckenlights.repositories.PlayCommandService;
import lombok.RequiredArgsConstructor;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 * @since 28.11.13 21:49
 */
@Component
@RequiredArgsConstructor
public class GetPlaylist {

    @NonNull
    PlayCommandService playCommandService;

    public List<PlayCommandSummary> getPlaylist(PlayStatus playStatus) {
        if (playStatus == null) {
            return playCommandService.getListByPlayStatusOrderByCreated(Arrays.asList(PlayStatus.PLAYING, PlayStatus.ENQUEUED),
                    20);
        }

        return playCommandService.getListByPlayStatusOrderByCreated(Arrays.asList(playStatus), 20);
    }

    public PlayCommandSummary getPlayCommand(String id) {
        return playCommandService.getPlayCommand(id);
    }
}
