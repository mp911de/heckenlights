package biz.paluch.heckenlights.messagebox.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
public interface TweetRepository extends MongoRepository<TweetDocument, Long> {

    List<TweetDocument> findTop10ByProcessedFalseOrderByReceivedAsc();
}
