package eu.brokeratcloud.opt.engine;

import eu.brokeratcloud.common.SLMEvent;

public abstract class EventManager {
	public abstract Configuration getConfiguration();
	public abstract void setConfiguration(Configuration cfg);
	public abstract void startManager();
	public abstract void stopManager();
	public abstract boolean isOnline();
	public abstract void eventReceived(String evtText);
	public abstract void eventReceived(SLMEvent evt);
	public abstract boolean publish(SLMEvent evt);
}
