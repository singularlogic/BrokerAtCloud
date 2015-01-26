package eu.brokeratcloud.common;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import eu.brokeratcloud.persistence.annotations.*;
import javax.xml.bind.annotation.XmlAttribute;
import org.codehaus.jackson.annotate.JsonIgnore;

@RdfSubject(
	rdfType="http://www.linked-usdl.org/ns/usdl-core#Service"
)
public class ServiceDescription extends BrokerObject {
	@XmlAttribute
	@RdfPredicate
	protected String serviceName;
	@XmlAttribute
	@RdfPredicate
	protected String serviceCategory;
	@XmlAttribute
	@RdfPredicate
	protected Map<String,Object> serviceAttributes;
	
	public ServiceDescription() { serviceAttributes = new HashMap<String,Object>(); }
	
	public String getServiceName() { return serviceName; }
	public void setServiceName(String s) { serviceName = s; }
	public String getServiceCategory() { return serviceCategory; }
	public void setServiceCategory(String s) { serviceCategory = s; }
	public Map<String,Object> getServiceAttributes() { return new HashMap<String,Object>(serviceAttributes); }
	public void setServiceAttributes(Map<String,Object> m) { serviceAttributes = m; }
	
	@JsonIgnore
	public Object getServiceAttributeValue(String attrId) {
		return serviceAttributes.get(attrId);
	}
	@JsonIgnore
	public void setServiceAttributeValue(String attrId, Object attrVal) {
		serviceAttributes.put(attrId, attrVal);
	}
	@JsonIgnore
	public void removeServiceAttribute(String attrId) {
		serviceAttributes.remove(attrId);
	}
	
	public String toString() {
		return "ServiceDescription: {\n"+super.toString()+
				"\tservice-name = "+serviceName+
				"\n\tcategory = "+serviceCategory+
				"\n\tattributes = "+serviceAttributes+
				"}\n";
	}
	
/*	public static void main(String[] args) throws Exception {
		System.err.println("Broker@Cloud Service Description management utility");
		if (args.length==0) showHelp();
		else if ((args[0]=args[0].trim()).isEmpty()) showHelp();
		else if (args[0].equalsIgnoreCase("add")) {
			System.out.println("Please fill-in the following Service Description metadata");
			String name = readInput("Service name: ");
			System.out.println("You wrote: "+name);
			String classifications = null;
			do {
				classifications = readInput("Classification dimensions: ");
				System.out.println("You wrote: "+classifications);
				if (classifications.equalsIgnoreCase("L")) {
					System.out.println("Classification dimensions:");
					System.out.println("++++ LIST classification dimensions in TREE-like format ++++++\n");
					classifications = null;
				}
			} while (classifications==null);
		} else {
			System.err.println("Unknown command: "+args[0]);
			showHelp();
		}
	}
	protected static void showHelp() {
		System.out.println("+++ TODO HELP +++");
	}
	protected static String readInput(String prompt) {
		do {
			try {
				System.out.print(prompt);
				System.out.flush();
				java.util.Scanner scan = null;
				if (scan==null) scan = new java.util.Scanner(System.in);
				while (!scan.hasNextLine()) Thread.sleep(200);
				String line = scan.nextLine().trim();
				if (line.length()>0) return line;
			} catch (Exception e) {
				System.err.println(e);
			}
		} while (true);
	}*/
}
