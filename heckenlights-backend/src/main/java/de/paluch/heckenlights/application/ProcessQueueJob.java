package de.paluch.heckenlights.application;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import lombok.extern.slf4j.Slf4j;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 */
@DisallowConcurrentExecution
@Slf4j
public class ProcessQueueJob implements Job {

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
