package de.paluch.heckenlights.spring;

import java.net.URL;

import javax.xml.bind.JAXB;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.stereotype.Component;

import de.paluch.heckenlights.model.Rules;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
@Component
public class RulesFactoryBean extends AbstractFactoryBean<Rules> {

    @Value("${rules.location}")
    private URL ruleLocation;

    @Override
    public Class<?> getObjectType() {
        return Rules.class;
    }

    @Override
    protected Rules createInstance() throws Exception {

        return JAXB.unmarshal(ruleLocation, Rules.class);
    }
}
