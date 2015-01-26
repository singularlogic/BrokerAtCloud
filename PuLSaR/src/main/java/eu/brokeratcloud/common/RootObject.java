package eu.brokeratcloud.common;

import eu.brokeratcloud.persistence.annotations.RdfPredicate;
import javax.xml.bind.annotation.XmlAttribute;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.log4j.PropertyConfigurator;

public abstract class RootObject {
	protected final static Logger logger;
	static {
		logger = LoggerFactory.getLogger("eu.brokeratcloud");
		// debugging
		if (logger.isDebugEnabled() || logger.isTraceEnabled())
			logger.debug("RootObject.<static>: Debug messages are 'on'");
	}
	
	@XmlAttribute
	@RdfPredicate(lang="en", uri="http://www.w3.org/2000/01/rdf-schema#label", omitIfNull=true)
	protected String labelEn;
	@XmlAttribute
	@RdfPredicate(lang="de", uri="http://www.w3.org/2000/01/rdf-schema#label", omitIfNull=true)
	protected String labelDe;
	@XmlAttribute
	@RdfPredicate(uri="http://www.w3.org/2000/01/rdf-schema#comment", omitIfNull=true)
	protected String comment;
	
	public String getLabel() { return labelEn; }
	public void setLabel(String s) { labelEn = s; }
	public String getLabelEn() { return labelEn; }
	public void setLabelEn(String s) { labelEn = s; }
	public String getLabelDe() { return labelDe; }
	public void setLabelDe(String s) { labelDe = s; }
	public String getComment() { return comment; }
	public void setComment(String s) { comment = s; }
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if (labelEn!=null) sb.append("label@en=").append(labelEn);
		if (labelDe!=null) {
			if (sb.length()>0) sb.append(", ");
			sb.append("label@de=").append(labelDe);
		}
		if (comment!=null) {
			if (sb.length()>0) sb.append(", ");
			sb.append("comment=").append(comment);
		}
		if (sb.length()>0) sb.insert(0, "RootObject: ");
		return sb.toString();
	}
	
/*	protected void stopDebug(String mesg) {
		if (mesg!=null && !mesg.isEmpty()) System.err.println(mesg);
		System.err.println("PRESS <ENTER> TO CONTINUE...");
		try{System.in.read();System.in.read();}catch(Exception e){}
	}*/
}
