package biz.paluch.heckenlights.messagebox.repository;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.stereotype.Component;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
@Component
public class MongoConnectionHealth extends AbstractHealthIndicator {

    @Autowired
    private MongoOperations mongoOperations;

    @Override
    protected void doHealthCheck(Health.Builder builder) throws Exception {
        Set<String> collectionNames = mongoOperations.getCollectionNames();
        builder.up().withDetail("Collections", collectionNames).build();
    }
}
