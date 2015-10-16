package diva.brokeratcloud.fpr.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class RecommendationHistory {

	public static RecommendationHistory INSTANCE = new RecommendationHistory();

	public static class HistoryItem {
		public String customer = null;
		public String profile = null;
		public List<String> added = new ArrayList<String>();
		public List<String> removed = new ArrayList<String>();
		public Date timestamp = null;
	}

	public List<HistoryItem> items = new ArrayList<HistoryItem>();

	public void initSamples() {
		HistoryItem item = null;

		// History item ONE
		item = new HistoryItem();
		item.customer = "ONE";
		item.added.add("http://www.broker-cloud.eu/service-descriptions/CAS/service-providerCAS#AddressApp1");
		item.added.add("http://www.broker-cloud.eu/service-descriptions/CAS/service-providerCAS#CalenderApp1");
		item.removed.add("http://www.broker-cloud.eu/service-descriptions/CAS/service-providerCAS#AddressApp2");
		item.removed.add("http://www.broker-cloud.eu/service-descriptions/CAS/service-providerCAS#CalenderApp2");
		item.timestamp = new Date();
		item.timestamp.setMonth(5);
		item.timestamp.setDate(4);
		items.add(item);

		// History item TWO

		item = new HistoryItem();
		item.customer = "TWO";
		item.removed.add("http://www.broker-cloud.eu/service-descriptions/CAS/service-providerCAS#AddressApp2");
		item.removed.add("http://www.broker-cloud.eu/service-descriptions/CAS/service-providerCAS#CASProductApp1");
		item.removed.add("http://www.broker-cloud.eu/service-descriptions/CAS/service-providerCAS#CASTaskApp1");
		item.timestamp = new Date();
		item.timestamp.setMonth(6);
		item.timestamp.setDate(1);
		items.add(item);

		// Another item for TWO
		item = new HistoryItem();
		item.customer = "TWO";
		item.added.add("http://www.broker-cloud.eu/service-descriptions/CAS/service-providerCAS#CASTaskApp2");
		item.added.add("http://www.broker-cloud.eu/service-descriptions/CAS/service-providerCAS#AddressApp2");
		item.added.add("http://www.broker-cloud.eu/service-descriptions/CAS/service-providerCAS#CASReportsApp2");
		item.removed.add("http://www.broker-cloud.eu/service-descriptions/CAS/service-providerCAS#AddressApp1");
		item.removed.add("http://www.broker-cloud.eu/service-descriptions/CAS/service-providerCAS#CASReportsApp1");
		item.removed.add("http://www.broker-cloud.eu/service-descriptions/CAS/service-providerCAS#CASTaskApp1");
		item.timestamp = new Date();
		item.timestamp.setMonth(6);
		item.timestamp.setDate(10);
		items.add(item);
	}

	public List<HistoryItem> after(Date timestamp) {
		List<HistoryItem> results = new ArrayList<HistoryItem>();
		for (HistoryItem item : items) {
			System.out.println(item.timestamp);
			if (item.timestamp.after(timestamp))
				results.add(item);
		}
		return results;
	}

}
