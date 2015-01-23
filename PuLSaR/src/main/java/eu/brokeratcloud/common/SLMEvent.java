package eu.brokeratcloud.common;

import java.io.IOException;
import java.io.StringReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.UUID;
import java.util.Properties;

public class SLMEvent implements Comparable {
	protected static final String ID_KEY = ".SLM_EVENT_ID";
	protected static final String TM_KEY = ".SLM_EVENT_TIMESTAMP";
	protected static final String TOPIC_KEY = ".SLM_EVENT_TOPIC";
	protected static final String TYPE_KEY = ".SLM_EVENT_TYPE";
	protected static final SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ") { 
												public Date parse(String source, java.text.ParsePosition pos) {    
													return super.parse(source.replaceFirst(":(?=[0-9]{2}$)",""),pos);
												}
												public StringBuffer format(Date date, StringBuffer toAppendTo, java.text.FieldPosition pos) {
													StringBuffer sb = super.format(date, toAppendTo, pos);
													if (pos.equals(java.text.DateFormat.Field.TIME_ZONE)) {
														sb.insert(sb.length()-2,':');
													}
													return sb;
												}
											};
	
	protected String id;
	protected Date timestamp;
	protected String topic;
	protected String type;
	protected Properties properties;
	
	public SLMEvent(String id, Date tm, String topic, String type, Properties p) throws ParseException {
		// check and complete parameters
		properties = new Properties();
		if (p!=null) properties.putAll(p);
		p = properties;
		if (id==null) id = p.getProperty(ID_KEY);
		if (id==null) id = UUID.randomUUID().toString();
		if (tm==null) if (p.containsKey(TM_KEY)) tm = dateFormatter.parse(p.getProperty(TM_KEY)); else tm = new Date();
		if (topic==null || (topic=topic.trim()).isEmpty()) topic = p.getProperty(TOPIC_KEY);
		if (type==null || (type=type.trim()).isEmpty()) type = p.getProperty(TYPE_KEY);
		if (topic==null || type==null || (topic=topic.trim()).isEmpty() || (type=type.trim()).isEmpty()) {
			throw new IllegalArgumentException(String.format("SLMEvent.<init>: invalid event topic or type: topic=%s, type=%s",topic,type));
		}
		
		// initialize instance
		this.id = id;
		this.timestamp = tm;
		this.topic = topic;
		this.type = type;
		this.properties = p;
		
		// set properties
		p.setProperty(ID_KEY, id);
		p.setProperty(TM_KEY, dateFormatter.format(tm));
		p.setProperty(TOPIC_KEY, topic);
		p.setProperty(TYPE_KEY, type);
	}
	
	public SLMEvent(String topic, String type, Properties p) throws ParseException {
		this(null, null, topic, type, p);
	}
	
	public String getId() { return id; }
	public Date getTimestamp() { return timestamp; }
	public String getTopic() { return topic; }
	public String getType() { return type; }
	public Enumeration<?> getPropertyNames() { return properties.propertyNames(); }
	public String getProperty(String k) { return properties.getProperty(k); }
	
	public String toString() {
		return 	String.format("SLMEvent: { id=%s, timestamp=%s, topic=%s, type=%s, properties=%s }", id, timestamp.toString(), topic, type, properties.toString());
	}
	
	public int compareTo(Object o) {
		SLMEvent e = (SLMEvent)o;
		String eId = e.getId();
		String eTopic = e.getTopic();
		String eType = e.getType();
		
		// check for equality
		if (id.equals(eId) && topic.equals(eTopic) && type.equals(type)) return 0;
		
		// not equal - check order
		int ord = topic.compareTo(eTopic);
		if (ord!=0) return ord;
		ord = type.compareTo(eType);
		if (ord!=0) return ord;
		return id.compareTo(eId);
	}
	
	public static SLMEvent parseEvent(String m) throws ParseException {
		Properties p = new Properties();
		try { p.load(new StringReader(m)); } catch (IOException e) { throw new IllegalArgumentException(e); }
		return new SLMEvent(null, null, null, null, p);
	}
}
