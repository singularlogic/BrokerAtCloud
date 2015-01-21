package org.broker.orbi.scheduler;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.broker.orbi.quartz.BrokerScheduler;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;

/**
 * This class implements the MonitoringScheduler which will be responsible for
 * the jobs
 *
 * @author smantzouratos
 */
public enum MonitoringScheduler {

    INSTANCE;

    private enum STATE {

        RUNNING, STOPPED, STARTED, ERROR
    };

    private boolean isActive = false;
    private MonitoringScheduler.STATE state = STATE.STOPPED;
    private Scheduler scheduler = null;

    /*  The following represent the possible states of the MonitoringScheduler
    
     RUNNING - MonitoringScheduler is started successfuly
     STOPPED - MonitoringScheduler is stopped
     STARTED - MonitoringScheduler is initializing
     ERROR   - MonitoringScheduler has occured an error
             
     /**
     * Run the MonitoringScheduler
     *
     * @return True on success run; otherwise false
     */
    public synchronized boolean startMonitoringScheduler() {
        boolean isSuccess = false;
        if (INSTANCE.isMonitoringSchedulerActive()) {
            return isSuccess;
        }
        //Lock the activation status of MonitoringScheduler
        INSTANCE.changeMonitoringSchedulerState();

        //TODO: Handle Error State
        if (INSTANCE.getMonitoringSchedulerState() == STATE.STOPPED) {
            //Change MonitoringScheduler to Started
            INSTANCE.setMonitoringSchedulerState(STATE.STARTED);

            try {
                scheduler = BrokerScheduler.startScheduler();
            } catch (SchedulerException ex) {
                Logger.getLogger(MonitoringScheduler.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InterruptedException ex) {
                Logger.getLogger(MonitoringScheduler.class.getName()).log(Level.SEVERE, null, ex);
            }
            if (null != scheduler) {
                //Change DAScheduler state to "RUNNING"
                INSTANCE.setMonitoringSchedulerState(STATE.RUNNING);
                Logger.getLogger(MonitoringScheduler.class.getName()).info("MonitoringScheduler successfuly started!!!");
                isSuccess = !isSuccess;
            } else {
                //Something went wrong....
                INSTANCE.setMonitoringSchedulerState(STATE.ERROR);
                Logger.getLogger(MonitoringScheduler.class.getName()).severe("Unable to start MonitoringScheduler...");
            }

        }
        //Unlock the activation status of MonitoringScheduler
        INSTANCE.changeMonitoringSchedulerState();
        return isSuccess;
    }

    public synchronized boolean stopMonitoringScheduler() {
        boolean isSuccess = false;
        if (INSTANCE.isMonitoringSchedulerActive()) {
            return isSuccess;
        }
        //Lock the activation status of DAScheduler
        this.changeMonitoringSchedulerState();

        //TODO: Handle Error State
        if (INSTANCE.getMonitoringSchedulerState() == STATE.RUNNING) {
            if (null != scheduler) {

                try {
                    //Terminate Scheduler

                    BrokerScheduler.stopScheduler(scheduler);

                    //Change MonitoringScheduler state to "STOPPED"
                    INSTANCE.setMonitoringSchedulerState(STATE.STOPPED);
                    Logger.getLogger(MonitoringScheduler.class.getName()).info("MonitoringScheduler has been successfuly shuted down...");
                    isSuccess = true;
                } catch (SchedulerException ex) {
                    INSTANCE.setMonitoringSchedulerState(STATE.ERROR);
                    Logger.getLogger(MonitoringScheduler.class.getName()).severe("Unable to shutdown MonitoringScheduler");
                } catch (InterruptedException ex) {
                    Logger.getLogger(MonitoringScheduler.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                //Change MonitoringScheduler state to "STOPPED"
                INSTANCE.setMonitoringSchedulerState(STATE.STOPPED);
                Logger.getLogger(MonitoringScheduler.class.getName()).warning("Suspicious Stop action, MonitoringScheduler was in \"RUNNING\" state however Quartz Instance was set to null... \n Change MonitoringScheduler state to \"STOPPED\"");
            }
        }
        //Unlock the activation status of MonitoringScheduler
        INSTANCE.changeMonitoringSchedulerState();
        return isSuccess;
    }

    /**
     *
     * @return true if MonitoringScheduler is active; otherwise false
     */
    private boolean isMonitoringSchedulerActive() {
        return this.isActive;
    }

    /**
     * Change the activation status of MonitoringScheduler; if is active then
     * deactivated and vice versa.
     */
    private void changeMonitoringSchedulerState() {
        this.isActive = !this.isActive;
    }

    /* 
     * @eturn The current state of MonitoringScheduler ; one of the values {RUNNING,STOPPED, PAUSED, STARTED, ERROR}

     */
    public STATE getMonitoringSchedulerState() {
        return this.state;
    }

    public String getMonitoringSchedulerStateAsString() {
        return this.state.toString();
    }

    private void setMonitoringSchedulerState(STATE newState) {
        this.state = newState;
    }

}
