package de.paluch.heckenlights.application;

import org.apache.log4j.Logger;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.context.ApplicationContext;

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

            RuleService rules = context.getBean(RuleService.class);

            if (rules.isChanged()) {

                log.info("Updating Rules");
                rules.updateRules();
            }

        } catch (Exception e) {
            log.warn(e.getMessage(), e);
        }
    }
}
