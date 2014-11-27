package de.paluch.heckenlights.spring;

import de.paluch.heckenlights.model.Rules;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;

import javax.xml.bind.JAXB;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
@Component
public class RulesFactoryBean extends AbstractFactoryBean<Rules> {

    @Value("${rules.location}")
    private URL ruleLocation;

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
        return JAXB.unmarshal(ruleLocation, Rules.class);
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

            File file = ResourceUtils.getFile(ruleLocation.toURI());
            if (file.exists()) {
                return file;
            }
        } catch (URISyntaxException e) {
            // ignore
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
