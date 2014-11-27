package de.paluch.heckenlights.application;

import org.apache.log4j.Logger;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.context.ApplicationContext;

import de.paluch.heckenlights.model.Rules;
import de.paluch.heckenlights.spring.RulesFactoryBean;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
@DisallowConcurrentExecution
public class RefreshRulesJob implements Job {

    private static final String APPLICATION_CONTEXT_KEY = "applicationContext";

    private Logger log = Logger.getLogger(getClass());

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {

        try {
            ApplicationContext context = (ApplicationContext) jobExecutionContext.getScheduler().getContext()
                    .get(APPLICATION_CONTEXT_KEY);

            RulesFactoryBean bean = context.getBean(RulesFactoryBean.class);
            if (bean.isModifiedSinceLastCheck()) {
                Rules instance = context.getBean(Rules.class);
                Rules updated = bean.parseRules();

                if (updated != null) {
                    log.info("Updating Rules");
                    bean.updateLastModified();
                    updateRules(instance, updated);
                }
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
