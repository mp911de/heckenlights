package de.paluch.heckenlights.spring;

import java.io.File;
import java.io.IOException;

import javax.xml.bind.JAXB;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import de.paluch.heckenlights.model.Rules;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
@Component
public class RulesFactoryBean extends AbstractFactoryBean<Rules> {

    @Value("${rules.location}")
    private Resource ruleLocation;

    private transient long lastModified;

    @Override
    public Class<?> getObjectType() {
        return Rules.class;
    }

    @Override
    protected Rules createInstance() throws Exception {
        updateLastModified();
        return parseRules();
    }

    public Rules parseRules() {
        try {
            return JAXB.unmarshal(ruleLocation.getFile(), Rules.class);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public boolean isModifiedSinceLastCheck() {

        File file = getFileIfExists();
        if (file != null) {
            return lastModified != file.lastModified();
        }

        return false;
    }

    private File getFileIfExists() {
        try {

            File file = ruleLocation.getFile();
            if (file.exists()) {
                return file;
            }
        } catch (IOException e) {
            // ignore
        }

        return null;
    }

    public void updateLastModified() {
        File file = getFileIfExists();
        if (file != null) {
            lastModified = file.lastModified();
        }
    }
}
