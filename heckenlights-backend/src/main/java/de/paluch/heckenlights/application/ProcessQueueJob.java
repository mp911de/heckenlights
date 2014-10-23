package de.paluch.heckenlights.application;

import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
public class ProcessQueueJob implements Job {

    private Logger log = Logger.getLogger(getClass());

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        ProcessQueue processQueue = (ProcessQueue) jobExecutionContext.getMergedJobDataMap().get("processQueue");
        try {
            processQueue.processQueue();
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
        }
    }
}
