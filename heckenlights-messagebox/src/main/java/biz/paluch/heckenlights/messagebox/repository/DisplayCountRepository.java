package biz.paluch.heckenlights.messagebox.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
public interface DisplayCountRepository extends MongoRepository<DisplayCountDocument, String> {

}
