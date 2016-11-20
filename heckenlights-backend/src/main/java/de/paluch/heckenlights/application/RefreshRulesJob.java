package de.paluch.heckenlights.application;

import java.io.IOException;

import javax.annotation.PostConstruct;
import javax.xml.bind.JAXB;

import org.apache.log4j.Logger;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;

import de.paluch.heckenlights.model.Rules;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
@DisallowConcurrentExecution
public class RefreshRulesJob implements Job {

    private static final String APPLICATION_CONTEXT_KEY = "applicationContext";

    private Logger log = Logger.getLogger(getClass());

    @Value("${rules.location}")
    Resource ruleLocation;

    @Autowired
    Rules rules;

    private volatile long lastModified;

    @PostConstruct
    private void postConstruct() throws IOException {
        lastModified = ruleLocation.lastModified();
    }

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {

        try {
            ApplicationContext context = (ApplicationContext) jobExecutionContext.getScheduler().getContext()
                    .get(APPLICATION_CONTEXT_KEY);

            if (lastModified != ruleLocation.lastModified()) {

                log.info("Updating Rules");

                Rules updated = JAXB.unmarshal(ruleLocation.getFile(), Rules.class);
                updateRules(rules, updated);

            }
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
        }
    }

    private void updateRules(Rules instance, Rules updated) {
        instance.getRules().clear();
        instance.getRules().addAll(updated.getRules());

        instance.setDefaultAction(updated.getDefaultAction());
        instance.setTimeunit(updated.getTimeunit());
        instance.setTimezone(updated.getTimezone());
    }
}
