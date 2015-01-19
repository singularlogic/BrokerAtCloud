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
	//uri="http://www.brokeratcloud.eu/v1/common/SERVICE-DESCRIPTION",
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
		if (args.length==0) {
			System.out.println("Usage: java ServiceDescription [add|get|delete] <options>");
			System.out.println("  ... add <name> <service> <category> [<attr name> <attr value> <value class>]*");
			System.out.println("  ... get <service description ID>");
			System.out.println("  ... delete <service description ID>");
		} else
		if (args[0].equalsIgnoreCase("add")) {
			int i=1;
			ServiceDescription sd = new ServiceDescription();
			sd.setId("SD-"+java.util.UUID.randomUUID());
			sd.setCreateTimestamp(new java.util.Date());
			sd.setName(args[i++]);
			sd.setServiceName(args[i++]);
			sd.setServiceCategory(args[i++]);
			
			Class[] cArg = new Class[1];
			cArg[0] = String.class;
			for (int j=i; j<args.length; j+=3) {
				Class clss = Class.forName(args[j+2]);
				Object v = null;
				if (clss.equals(String.class)) {
					v = args[j+1];
					sd.setServiceAttributeValue(args[j], v);
				} else {
					java.lang.reflect.Method m = clss.getDeclaredMethod("valueOf", cArg);
					v = m.invoke(null, args[j+1]);
					sd.setServiceAttributeValue(args[j], v);
				}
			}
			System.out.println("New SD:\n"+sd);
			
			eu.brokeratcloud.persistence.RdfPersistenceManager pm = eu.brokeratcloud.persistence.RdfPersistenceManagerFactory.createRdfPersistenceManager();
			pm.persist(sd);
			System.out.println("SD saved in persistent store");
		} else
		if (args[0].equalsIgnoreCase("get")) {
			eu.brokeratcloud.persistence.RdfPersistenceManager pm = eu.brokeratcloud.persistence.RdfPersistenceManagerFactory.createRdfPersistenceManager();
			ServiceDescription sd = (ServiceDescription)pm.find(args[1], ServiceDescription.class);
			System.out.println("SD retrieved from persistent store:\n"+sd);
		} else
		if (args[0].equalsIgnoreCase("delete")) {
			eu.brokeratcloud.persistence.RdfPersistenceManager pm = eu.brokeratcloud.persistence.RdfPersistenceManagerFactory.createRdfPersistenceManager();
			ServiceDescription sd = (ServiceDescription)pm.find(args[1], ServiceDescription.class);
			pm.remove(sd);
			System.out.println("SD deleted from persistent store");
		} else
		if (args[0].equalsIgnoreCase("set-attr")) {
			eu.brokeratcloud.persistence.RdfPersistenceManager pm = eu.brokeratcloud.persistence.RdfPersistenceManagerFactory.createRdfPersistenceManager();
			ServiceDescription sd = (ServiceDescription)pm.find(args[1], ServiceDescription.class);
			System.out.println("SD retrieved from persistent store:\n"+sd);

			Class[] cArg = new Class[1];
			cArg[0] = String.class;
			{
				Class clss = Class.forName(args[2+2]);
				Object v = null;
				if (clss.equals(String.class)) {
					v = args[2+1];
					sd.setServiceAttributeValue(args[2], v);
				} else {
					java.lang.reflect.Method m = clss.getDeclaredMethod("valueOf", cArg);
					v = m.invoke(null, args[2+1]);
					sd.setServiceAttributeValue(args[2], v);
				}
			}
			System.out.println("Updated SD:\n"+sd);
			
			pm.merge(sd);
			System.out.println("SD updated in persistent store");
		} else
		if (args[0].equalsIgnoreCase("del-attr")) {
			eu.brokeratcloud.persistence.RdfPersistenceManager pm = eu.brokeratcloud.persistence.RdfPersistenceManagerFactory.createRdfPersistenceManager();
			ServiceDescription sd = (ServiceDescription)pm.find(args[1], ServiceDescription.class);
			System.out.println("SD retrieved from persistent store:\n"+sd);
			
			sd.removeServiceAttribute(args[2]);
			
			pm.merge(sd);
			System.out.println("SD updated in persistent store");
		} else {
			System.err.println("Unknown command: "+args[0]);
		}
	}
*/	
	public static void main(String[] args) throws Exception {
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
	}
}
