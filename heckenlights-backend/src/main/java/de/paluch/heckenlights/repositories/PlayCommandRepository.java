package de.paluch.heckenlights.repositories;

import de.paluch.heckenlights.model.PlayStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

import java.util.Date;
import java.util.List;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 * @since 28.11.13 21:10
 */
public interface PlayCommandRepository extends CrudRepository<PlayCommandDocument, String>
{

    List<PlayCommandDocument> findByPlayStatusOrderByCreatedAsc(PlayStatus playStatus, Pageable pageable);

    List<PlayCommandDocument> findByCreatedBetweenOrderByCreatedAsc(Date from, Date to);

	List<PlayCommandDocument> findByPlayStatusOrderByCreatedAsc(PlayStatus playStatus);
}
