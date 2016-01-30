package org.broker.orbi.quartz;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.broker.orbi.monitoring.Util;
import org.broker.orbi.util.entities.Host;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 *
 * @author smantzouratos
 */
public class BrokerJob implements Job {

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {

        // Broker Scheduler invokation
        Logger.getLogger(BrokerJob.class.getName()).log(Level.INFO, "Broker Job invoked! Starting process..");

        // Initialize List of Hosts
        List<Host> listOfHosts = Util.initializeZabbixHosts();

        // Retrieve Metrics for all Hosts and for each variable send a Topic Message
        String response = Util.startRetrievalFromZabbix(listOfHosts);

        Logger.getLogger(BrokerJob.class.getName()).log(Level.INFO, "Broker Job finished with message: {0} and sent to TOPIC!", response);
    }

}
