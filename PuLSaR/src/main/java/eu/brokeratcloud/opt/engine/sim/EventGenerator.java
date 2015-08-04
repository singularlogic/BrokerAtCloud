package eu.brokeratcloud.opt.engine.sim;

import java.io.*;
import eu.brokeratcloud.opt.engine.EventManager;
import eu.brokeratcloud.common.SLMEvent;

public class EventGenerator {
	
	public static enum GEN_MODE { PERIOD, DELAY };
	
	protected long periodInUsec;
	protected long nIterations;
	protected String eventTemplate;
	protected EventManager eventManager;
	protected GEN_MODE mode;
	protected SLMEvent event;
	protected Thread runner;
	protected boolean isRunning;
	protected boolean keepRunning;
	protected PrintStream out;
	protected long cntSuccess;
	protected long cntError;
	
	public EventGenerator(long period, long nIters, String eventTpl, EventManager manager, GEN_MODE mode) {
		this.periodInUsec = period;
		this.nIterations = nIters;
		this.eventTemplate = eventTpl;
		this.eventManager = manager;
		this.out = System.out;
		this.mode = mode;
	}
	
	public EventGenerator(long period, long nIters, String eventTpl, EventManager manager, GEN_MODE mode, PrintStream ps) {
		this(period, nIters, eventTpl, manager, mode);
		this.out = ps;
	}
	
	public void start() {
		// initialize runner thread
		runner = new Thread() {
			public void run() {
				isRunning = true;
				keepRunning = true;
				out.println("EventGenerator: BEGIN: nominal period="+periodInUsec+"usec, iterations="+nIterations+"... ");
				
				// prepare an SLM event
				SLMEvent event = null;
				try { event = SLMEvent.parseEvent( eventTemplate ); } 
				catch (java.text.ParseException e) { isRunning = false; keepRunning = false; throw new RuntimeException(e); }
				
				// initialize variables
				long period = (long)(1000*periodInUsec);	// convert period in nanosecs
				long delay = periodInUsec/1000;				// delay in millis (sleep requires millis)
				long start = System.nanoTime();				// log start time
				long end = start;
				long limit = start;
				long cnt=0;
				cntSuccess = 0;
				cntError = 0;
				
				// generate and send events
				while (cnt<nIterations) {
					try {
						eventManager.eventReceived( event );
						cntSuccess++;
					} catch (Exception e) {			// suppress exception propagation
						cntError++;
						System.err.println("EventGenerator: EXCEPTION: while processing event: "+e);
						e.printStackTrace(System.err);
					}
					cnt++;
					if (cnt<nIterations) {
						if (mode==GEN_MODE.PERIOD) {
							limit += period;
							while ((end=System.nanoTime())<limit) ;
						} else try { 
							Thread.currentThread().sleep(delay);
						} catch (Exception e) { break; }
					}
				}
				//try { Thread.currentThread().sleep(1000); } catch(Exception e){}
				
				String actualPeriod = cnt>0 ? Long.toString((end-start)/cnt/1000)+"usec" : "N/A";
				String duration = cnt>0 ? Long.toString((end-start)/1000)+"usec" : "N/A";
				out.println("EventGenerator: END: nominal period="+periodInUsec+"usec, actual period="+actualPeriod+", iterations="+nIterations+", duration="+duration+
							", success="+cntSuccess+", errors="+cntError);
				isRunning = false;
			}
		};
		
		// start simulation
		keepRunning = true;
		isRunning = true;
		runner.start();
	}
	
	public void stop() {
		keepRunning = false;	// signal runner thread to terminate (currently is ignored)
		waitToComplete();		// wait until runner thread terminates
		runner = null;
	}
	
	public void waitToComplete() {
		while (isRunning) try { Thread.sleep(100); } catch (Exception e) { System.err.println("XXX: "+e); }
	}
}