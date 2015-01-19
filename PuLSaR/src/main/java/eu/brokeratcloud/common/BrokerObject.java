package eu.brokeratcloud.common;

import java.util.Date;
import eu.brokeratcloud.persistence.annotations.*;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAttribute;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@RdfSubject
public abstract class BrokerObject extends RootObject {
	@XmlAttribute
	@Id
	@RdfPredicate(uri="http://purl.org/dc/terms/identifier")					// ex. http://www.brokeratcloud.eu/v1/common/hasId
	protected String id;
	@XmlAttribute
	@RdfPredicate(uri="http://purl.org/dc/terms/title", omitIfNull=true)		// ex. http://www.brokeratcloud.eu/v1/common/hasName
	protected String name;
	@XmlAttribute
	@RdfPredicate(uri="http://purl.org/dc/terms/description", omitIfNull=true)	// ex. http://www.brokeratcloud.eu/v1/common/hasDescription
	protected String description;
	@XmlAttribute
	@RdfPredicate(uri="http://purl.org/dc/terms/created", omitIfNull=true)		// ex. http://www.brokeratcloud.eu/v1/common/hasCreateTimestamp
	protected java.util.Date createTimestamp;
	@XmlAttribute
	@RdfPredicate(uri="http://purl.org/dc/terms/modified", omitIfNull=true)		// ex. http://www.brokeratcloud.eu/v1/common/hasLastUpdateTimestamp
	protected java.util.Date lastUpdateTimestamp;
	@XmlAttribute
	@RdfPredicate(uri="http://purl.org/dc/terms/creator", omitIfNull=true)		// ex. http://www.brokeratcloud.eu/v1/common/hasOwner
	//protected Consumer owner;
	protected String owner;
	
	public String getId() { return id; }
	public void setId(String id) { this.id = id; }
	public String getName() { return name; }
	public void setName(String n) { name = n; }
	public String getDescription() { return description; }
	public void setDescription(String n) { description = n; }
	
	public java.util.Date getCreateTimestamp() { return createTimestamp; }
	public void setCreateTimestamp(java.util.Date dt) { createTimestamp = dt; }
	public java.util.Date getLastUpdateTimestamp() { return lastUpdateTimestamp; }
	public void setLastUpdateTimestamp(java.util.Date dt) { lastUpdateTimestamp = dt; }
	
	//public Consumer getOwner() { return owner; }
	//public void setOwner(Consumer c) { owner = c; }
	public String getOwner() { return owner; }
	public void setOwner(String c) { owner = c; }
	
	protected String createId() {
		String clss = getClass().getName();
		int p = clss.lastIndexOf('.');
		clss = (p!=-1) ? clss.substring(p+1) : clss;
		clss = clss.replaceAll("[^A-Za-z0-9_]","");
		clss = clss.replaceAll("([A-Z_])", "-$1");
		clss = clss.replaceAll("^-*", "").replaceAll("-*$","");
		clss = clss.toUpperCase();
		return createId(clss);
	}
	
	protected static String createId(String prefix) {
		return String.format("%s-%s", prefix, java.util.UUID.randomUUID().toString());
	}
	
	public String toString() {
		return 	"\tid = "+id+"\n\tname = "+name+"\n\tdescription = "+description+"\n\tcreation = "+createTimestamp+"\n\tlast-update = "+lastUpdateTimestamp+"\n\towner = "+owner+"\n";
	}
}
