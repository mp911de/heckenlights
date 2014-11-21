package de.paluch.heckenlights.repositories;

import org.springframework.data.repository.CrudRepository;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
public interface StateRepository extends CrudRepository<StateDocument, String> {
}
