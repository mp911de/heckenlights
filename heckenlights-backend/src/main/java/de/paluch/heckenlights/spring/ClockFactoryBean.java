package de.paluch.heckenlights.spring;

import javax.inject.Inject;
import java.time.Clock;
import java.util.TimeZone;

import de.paluch.heckenlights.model.Rules;
import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.stereotype.Component;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
@Component
public class ClockFactoryBean extends AbstractFactoryBean<Clock> {

    @Inject
    private Rules rules;

    @Override
    public Class<?> getObjectType() {
        return Clock.class;
    }

    @Override
    protected Clock createInstance() throws Exception {
        return Clock.system(TimeZone.getTimeZone(rules.getTimezone()).toZoneId());
    }
}
