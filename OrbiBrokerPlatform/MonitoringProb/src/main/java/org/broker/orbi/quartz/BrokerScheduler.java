package org.broker.orbi.quartz;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import org.quartz.CronTrigger;
import static org.quartz.JobBuilder.newJob;
import org.quartz.JobDetail;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import static org.quartz.TriggerBuilder.newTrigger;
import org.quartz.impl.StdSchedulerFactory;

/**
 *
 * @author smantzouratos
 */
public class BrokerScheduler {

    public static Scheduler startScheduler() throws SchedulerException, InterruptedException {
        StdSchedulerFactory sf = new StdSchedulerFactory();
        Scheduler sched = sf.getScheduler();

        // define the job and tie it to our HelloJob class 
        JobDetail job = newJob(BrokerJob.class)
                .withIdentity("job1", "group1")
                .build();

        CronTrigger trigger = newTrigger()
                .withIdentity("trigger1", "group1")
                .withSchedule(cronSchedule("0/5 * * * * ?"))
                .build();

        // Tell quartz to schedule the job using our trigger 
        sched.scheduleJob(job, trigger);

        sched.start();
        
        return sched;

    }
    
    public static void stopScheduler(Scheduler scheduler) throws SchedulerException, InterruptedException {
        scheduler.shutdown(true);
    }

    public void singleExecutionOfBrokerJob() throws JobExecutionException {
        BrokerJob job = new BrokerJob();
        job.execute(null);
    }

}
