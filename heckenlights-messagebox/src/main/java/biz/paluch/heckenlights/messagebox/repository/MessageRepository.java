package biz.paluch.heckenlights.messagebox.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
public interface MessageRepository extends MongoRepository<MessageDocument, String> {

    List<MessageDocument> findTop10ByProcessedFalse();
}
