package de.paluch.heckenlights.application;

import javax.xml.bind.JAXB;

import org.apache.log4j.Logger;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
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

    private volatile long lastModified;

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {

        try {
            ApplicationContext context = (ApplicationContext) jobExecutionContext.getScheduler().getContext()
                    .get(APPLICATION_CONTEXT_KEY);

            Resource ruleLocation = context.getResource(context.getEnvironment().getProperty("rules.location"));
            Rules rules = context.getBean(Rules.class);

            if (lastModified == 0) {
                lastModified = ruleLocation.lastModified();
            }

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
