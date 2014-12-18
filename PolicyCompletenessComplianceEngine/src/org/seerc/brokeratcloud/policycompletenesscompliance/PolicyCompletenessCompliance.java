package org.seerc.brokeratcloud.policycompletenesscompliance;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.output.TeeOutputStream;

import com.hp.hpl.jena.datatypes.DatatypeFormatException;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.util.FileManager;

public class PolicyCompletenessCompliance {

	//private static final Object brokerPolicyResources = "Ontologies/SAP_HANA_Cloud_Apps_Broker_Policy_test.ttl";
	private static final Object[] brokerPolicyResources = {"Ontologies/ForReview/CAS-broker-policies.ttl", "Ontologies/ForReview/CAS-Service-Level-Profile-silver.ttl", "Ontologies/ForReview/CAS-functional-categories.ttl"};
	//private static final String serviceDescriptionResources = "Ontologies/SAP_HANA_Cloud_Apps_SD_test.ttl";
	private static final Object[] serviceDescriptionResources = {"Ontologies/ForReview/CAS-AddressApp.ttl", "Ontologies/ForReview/CAS-Service-Provider.ttl", "Ontologies/ForReview/CAS-functional-categories.ttl"};
	
	private static final Object[] brokerPolicyStressTestResources = {"Ontologies/ForStressTest/CAS-broker-policies.ttl", "Ontologies/ForStressTest/CAS-Service-Level-Profile-silver.ttl", "Ontologies/ForStressTest/CAS-functional-categories.ttl"};
	private static final Object[] serviceDescriptionStressTestResources = {"Ontologies/ForStressTest/CAS-AddressApp.ttl", "Ontologies/ForStressTest/CAS-Service-Provider.ttl", "Ontologies/ForStressTest/CAS-functional-categories.ttl"};

	protected OntModel modelMem = null;
	private BrokerPolicy bp = new BrokerPolicy();
	private BrokerPolicyReportObject brokerPolicyReport; // this is where the broker policy report will be stored
	private CompletenessReportObject completenessReport; // this is where the completeness report will be stored
	private ComplianceReportObject complianceReport; // this is where the compliance report will be stored
	private static final String USDL_CORE = "http://www.linked-usdl.org/ns/usdl-core#";
	private static final String USDL_BUSINESS_ROLES = "http://www.linked-usdl.org/ns/usdl-business-roles#";
	private static final String USDL_SLA = "http://www.linked-usdl.org/ns/usdl-sla#";
	private static final String USDL_CORE_CB = "http://www.linked-usdl.org/ns/usdl-core/cloud-broker#";
	private static final String RDFS = "http://www.w3.org/2000/01/rdf-schema#";
	private static final String GR = "http://purl.org/goodrelations/v1#";
	private static final String RDF = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
	private static final String FC = "http://www.broker-cloud.eu/service-descriptions/CAS/categories#";

	// The tee output stream that will output broker policy report messages both to System.out and file.
	TeeOutputStream brokerPolicyReportTos;
	
	// The tee output stream that will output completeness report messages both to System.out and file.
	TeeOutputStream completenessReportTos;
	
	// The tee output stream that will output compliance report messages both to System.out and file.
	TeeOutputStream complianceReportTos;
	
	// indicates whether the loaded BP has SLP in Connection (denoting that we should go for minimal SD check)
	private boolean bpHasSLPInConnection = false;
	
	public PolicyCompletenessCompliance()
	{
		try {
			// The file output streams for reports
			File brokerPolicyReportFile = new File("brokerPolicyReport.txt");
			FileOutputStream brokerPolicyReportFop = new FileOutputStream(brokerPolicyReportFile);
			
			File completenessReportFile = new File("completenessReport.txt");
			FileOutputStream completenessReportFop = new FileOutputStream(completenessReportFile);
			
			File complianceReportFile = new File("complianceReport.txt");
			FileOutputStream complianceReportFop = new FileOutputStream(complianceReportFile);
			
			// construct the brokerPolicyReportTos double stream
			brokerPolicyReportTos = new TeeOutputStream(System.out, brokerPolicyReportFop);		

			// construct the completenessReportTos double stream
			completenessReportTos = new TeeOutputStream(System.out, completenessReportFop);		

			// construct the complianceReportTos double stream
			complianceReportTos = new TeeOutputStream(System.out, complianceReportFop);	
			
			acquireMemoryForData(OntModelSpec.RDFS_MEM);

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		try {
			PolicyCompletenessCompliance pc = new PolicyCompletenessCompliance();

			// validate broker policy first
			pc.validateBrokerPolicy(brokerPolicyResources);
			//pc.validateBrokerPolicy(brokerPolicyPath);
			
			// Get broker policy in Java object structure
			pc.getBrokerPolicy(brokerPolicyResources);

			// Perform completeness check
			pc.validateSDForCompletenessCompliance(serviceDescriptionResources);

			//pc.performStressTest();
			
		} catch (Exception e) {
			System.out.println("Failure: " + e.getClass().getName() + " - "
					+ e.getMessage());
			e.printStackTrace();
			return;
		}
	}

	private void performStressTest() 
	{
		/*
		 * For each triple (T) in model:
		 * 		For all three elements (E) in triple (T):
		 * 			1) Make a typo in (E)
		 * 			2) Run validation mechanism. You should get Exception (X).
		 * 			3) if(X)
		 * 					correct typo in (E)
		 * 					continue loop
		 * 			   else
		 * 					report that failure has not been caught.
		 * 			
		 */
		
		// nullify System.out
		System.setOut(new PrintStream(new OutputStream() {
            public void write(int b) {
                //DO NOTHING
            }
        }));
		
		//OntModel cachedModel = this.modelMem;
		//int numOfTriples = this.modelMem.getGraph().size();
		//List<Triple> triplesList = this.modelMem.getGraph().find(Node.ANY, Node.ANY, Node.ANY).toList();
		//numOfTriples = triplesList.size();
		int problemNumber = 0;
		int okTriples = 0;
		int totalOK = 0;
		
		PolicyCompletenessCompliance pc = new PolicyCompletenessCompliance();

		//loadTriples(triplesList, pc);
		
		//InputStream is = convertTriplesToInputStream(pc);
		
			// load BP first
			try {
				pc.addDataToJenaModel(brokerPolicyStressTestResources);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			//get triples
			List<Triple> bpTriplesList = pc.modelMem.getGraph().find(Node.ANY, Node.ANY, Node.ANY).toList();
			InputStream bpIs = null;
			System.err.println("Total number of triples: " + bpTriplesList.size());
			System.err.println("Triples changed that did not create problem:");
			for(Triple t:bpTriplesList)
			{
				Triple erroredT = null;
				try {
					// reset model
					pc.modelMem.removeAll();
					// add data
					pc.addDataToJenaModel(brokerPolicyStressTestResources);
					// delete triple
					pc.modelMem.getGraph().delete(t);
					// create erroredT
					erroredT = createErroredT(t);
					// add erroredT
					pc.modelMem.getGraph().add(erroredT);
					// convert to InputStream
					bpIs = convertTriplesToInputStream(pc);
					// reset model
					pc.modelMem.removeAll();
					// validate broker policy
					pc.validateBrokerPolicy(bpIs);
					
					// no exception with erroredT, this is a problem
					if(okTriples > 0)
					{
						totalOK += okTriples;
						System.err.println("... in the meantime " + okTriples + " OK ...");
						okTriples = 0;
					}
					System.err.println(++problemNumber + ") " + t);
					/*System.err.println(t);
					System.err.println("to:");
					System.err.println(erroredT);
					System.err.println("did not cause a problem!");
					System.err.println("-------------------------------------------------------------------");
					System.err.println();*/
				} catch (BrokerPolicyException | CompletenessException | ComplianceException e) {
					okTriples++;
				} catch (Exception e) {
					// other exception with erroredT, this is a problem
					if(okTriples > 0)
					{
						totalOK += okTriples;
						System.err.println("... in the meantime " + okTriples + " OK ...");
						okTriples = 0;
					}
					System.err.println(++problemNumber + ") " + e.getMessage() + " for " + t);
				}
			}

			System.err.println("Total number of triples that caused problem: " + (totalOK + okTriples));
			System.err.println("Total number of triples that did not cause problem: " + problemNumber);
		
		/*// reset model
		pc.modelMem.removeAll();

		// now load SD
		pc.addDataToJenaModel(serviceDescriptionStressTestResources);
		//get triples
		List<Triple> sdTriplesList = pc.modelMem.getGraph().find(Node.ANY, Node.ANY, Node.ANY).toList();
		// convert to InputStream
		InputStream sdIs = convertTriplesToInputStream(pc);
		// reset model
		pc.modelMem.removeAll();

		// reset and add the bpIs
		bpIs.reset();
		pc.addDataToJenaModel(bpIs);
		// Perform completeness/compliance check
		pc.validateSDForCompletenessCompliance(sdIs);*/

		int i=0;
	}

	private Triple createErroredT(Triple t) {
		Node erroredSubject = createErroredNode(t.getSubject());
		Node erroredPredicate = createErroredNode(t.getPredicate());
		Node erroredObject = createErroredNode(t.getObject());
		Triple erroredT = new Triple(erroredSubject, erroredPredicate, erroredObject);
		return erroredT;
	}

	private Node createErroredNode(Node node) {
		Node erroredNode = null;
		if(node.isLiteral())
		{
			erroredNode = Node.createLiteral(node.toString() + "1");
		}
		else if(node.isURI())
		{
			erroredNode = Node.createURI(node.toString() + "1");
		}
		else if(node.isVariable())
		{
			erroredNode = Node.createVariable(node.toString() + "1");
		}
		return erroredNode;
	}

	private InputStream convertTriplesToInputStream(PolicyCompletenessCompliance pc) {
		ByteArrayOutputStream outA = new ByteArrayOutputStream();
		pc.modelMem.write(outA, "TURTLE");
		InputStream decodedInput=new ByteArrayInputStream(outA.toByteArray());
		return decodedInput;
	}

	private void loadTriples(List<Triple> triplesList,
			PolicyCompletenessCompliance pc) {
		for(Triple t:triplesList)
		{
			//PolicyCompletenessCompliance pc = new PolicyCompletenessCompliance();
			pc.modelMem.getGraph().add(t);
			// initially set the same model
			//pc.modelMem = cachedModel;
			// delete the current triple
			//pc.modelMem.getGraph().delete(t);
			// add the "errored" with typo triple
			//Triple erroredT = Triple.create(t.getSubject(), t.getPredicate(), t.getObject());

		}
	}

	public void validateSDForCompletenessCompliance(Object... dataToCheck) throws IOException, CompletenessException, ComplianceException {
		if(bpHasSLPInConnection())
		{	// already loaded BP had SLP info
			// SD will be checked with the minimal connection checks
			writeMessageToBrokerPolicyReport("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
			writeMessageToBrokerPolicyReport("Loaded BP had SLP information inside! Will run the minimal checks on this SD!");
			writeMessageToBrokerPolicyReport("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
			writeMessageToBrokerPolicyReport("");

			runMinimalCheck(dataToCheck);			
		}
		else
		{	// normal case, full completeness/compliance check in SD.
			runCompletenessCompliance(dataToCheck);			
		}
	}
	
	private void runMinimalCheck(Object... dataToCheck) throws IOException, CompletenessException, ComplianceException 
	{
		writeMessageToCompletenessReport("##################");
		writeMessageToCompletenessReport("Minimal Check");
		writeMessageToCompletenessReport("##################");
		
		// Add the file contents into the Jena model prior to caching it
		addDataToJenaModel(dataToCheck);
		// cache the current modelMem with BP inside in order to use it later for relations with instances checks
		OntModel cachedModel = modelMem;
		
		// Initialize model in this case in order not to find entities from BP
		acquireMemoryForData(OntModelSpec.RDFS_MEM);
		
		// if BP data are in InputStream, reset it to reuse it
		for(int i=0;i<dataToCheck.length;i++)
		{
			if(dataToCheck[i] instanceof InputStream)
			{
				((InputStream) dataToCheck[i]).reset();
			}
		}

		// Add the file contents into the Jena model
		addDataToJenaModel(dataToCheck);
		
		writeMessageToCompletenessReport("----------------");
		writeMessageToCompletenessReport("Usdl-core completeness section:");
		writeMessageToCompletenessReport("----------------");

		// check that instance of Service class exists. Will take the first one, could there be more than one? No, checking later
		RDFNode sInstance = oneVarOneSolutionQuery("{?var a usdl-core:Service}");
		if(sInstance == null)
		{
			writeMessageToCompletenessReport("Error - No Service instance was found in the Service Description.");
			throw new CompletenessException("No Service instance was found in the Service Description.");
		}
		writeMessageToCompletenessReport("Service instance was found in the Service Description: " + sInstance.toString());		
		
		// check that instance of Service is associated with a hasMakeAndModel relation with a Service Model
		RDFNode smInstance = oneVarOneSolutionQuery("{<" + sInstance.toString() + "> usdl-core-cb:hasServiceModel ?var}");
		if(smInstance == null)
		{
			writeMessageToCompletenessReport("Error - No Service instance with usdl-core-cb:hasServiceModel association was found with a Service Model.");
			throw new CompletenessException("No Service instance with usdl-core-cb:hasServiceModel association was found with a Service Model.");
		}
		writeMessageToCompletenessReport("Service instance usdl-core-cb:hasServiceModel association was found with the Service Model: " + smInstance.toString());		
		
		// check that instance of Entity Involvement exists
		RDFNode eiInstance = oneVarOneSolutionQuery("{?var a usdl-core:EntityInvolvement}");
		if(eiInstance == null)
		{
			writeMessageToCompletenessReport("Error - No Entity Involvement instance was found in the Service Description.");
			throw new CompletenessException("No Entity Involvement instance was found in the Service Description.");
		}
		writeMessageToCompletenessReport("Entity Involvement instance was found in the Service Description: " + eiInstance.toString());		
		
		// check that Service instance is associated via a hasEntityInvolvement relation with the Entity Involvement instance
		Integer heiAssociationsCount = countQuery("{<" + sInstance.toString() + "> usdl-core:hasEntityInvolvement <" + eiInstance.toString() + ">}");
		if(heiAssociationsCount == 0)
		{
			writeMessageToCompletenessReport("Error - Service instance is not associated via a hasEntityInvolvement relation with the Entity Involvement instance.");
			throw new CompletenessException("Service instance is not associated via a hasEntityInvolvement relation with the Entity Involvement instance.");
		}
		writeMessageToCompletenessReport("Service instance is associated via a hasEntityInvolvement relation with the Entity Involvement instance.");		
		
		// check that instance of Entity Involvement is associated via the withBusinessRole relation with the provider instance of the class BusinessRoles
		Integer wbrAssociationsCountInstance = countQuery("{<" + eiInstance.toString() + "> usdl-core:withBusinessRole <" + USDL_BUSINESS_ROLES + "provider>}");
		if(wbrAssociationsCountInstance == 0)
		{
			writeMessageToCompletenessReport("Error - Entity Involvement instance is not associated via the withBusinessRole relation with the provider instance of the class BusinessRoles.");
			throw new CompletenessException("Entity Involvement instance is not associated via the withBusinessRole relation with the provider instance of the class BusinessRoles.");
		}
		writeMessageToCompletenessReport("Entity Involvement instance is associated via the withBusinessRole relation with the provider instance of the class BusinessRoles.");		
		
		// check that instance of Business Entity exists
		RDFNode beInstance = oneVarOneSolutionQuery("{?var a gr:BusinessEntity}");
		if(beInstance == null)
		{
			writeMessageToCompletenessReport("Error - No Business Entity instance was found in the Service Description.");
			throw new CompletenessException("No Business Entity instance was found in the Service Description.");
		}
		writeMessageToCompletenessReport("Business Entity instance was found in the Service Description: " + beInstance.toString());		

		// check that instance of Entity Involvement is associated via the ofBusinessEnity relation with the Business Entity instance
		Integer obeAssociationsCountInstance = countQuery("{<" + eiInstance.toString() + "> usdl-core:ofBusinessEntity <" + beInstance.toString() + ">}");
		if(obeAssociationsCountInstance == 0)
		{
			writeMessageToCompletenessReport("Error - Entity Involvement instance is not associated via the ofBusinessEnity relation with the Business Entity instance.");
			throw new CompletenessException("Entity Involvement instance is not associated via the ofBusinessEnity relation with the Business Entity instance.");
		}
		writeMessageToCompletenessReport("Entity Involvement instance is associated via the ofBusinessEnity relation with the Business Entity instance.");		
		
		writeMessageToCompletenessReport("----------------");
		writeMessageToCompletenessReport("Service Section:");
		writeMessageToCompletenessReport("----------------");
		String si_uri = null; // service instance uri

		// how many usdl-core:Service instances are present in the SD?
		int countSi = countQuery("{?si rdf:type usdl-core:Service}");

		if (countSi == 0) {
			writeMessageToCompletenessReport("Error - SD does not contain an instance of usdl-core:Service");
			throw new CompletenessException("SD does not contain an instance of usdl-core:Service");
		} else if (countSi > 1) {
			writeMessageToCompletenessReport("Error - SD must contain only 1 service instance");
			throw new CompletenessException("SD must contain only 1 service instance");
		} else if (countSi == 1) {
			writeMessageToCompletenessReport("OK - SD contains exactly 1 service instance of type usdl-core:Service");
			RDFNode node = oneVarOneSolutionQuery("{?var rdf:type usdl-core:Service}");
			si_uri = node.toString();
		}
		
		String smi_uri = null; // service model instance uri
		// Exactly one Service Model should be found
		int countSm = countQuery("{?sm rdf:type <" + bp.getServiceModelMap().keySet().iterator().next() + ">}");

		if (countSm == 0) {
			writeMessageToCompletenessReport("Error - SD does not contain an instance of Service Model");
			throw new CompletenessException("SD does not contain an instance of Service Model");
		} else if (countSm > 1) {
			writeMessageToCompletenessReport("Error - SD must contain only 1 Service Model instance");
			throw new CompletenessException("SD must contain only 1 Service Model instance");
		} else if (countSm == 1) {
			writeMessageToCompletenessReport("OK - SD contains exactly 1 Service Model instance:");
			RDFNode node = oneVarOneSolutionQuery("{?var rdf:type <" + bp.getServiceModelMap().keySet().iterator().next() + ">}");
			smi_uri = node.toString();
			writeMessageToCompletenessReport(smi_uri);
		}

		int countIsVariantOf = countQuery("{<" + smi_uri + "> gr:isVariantOf ?someValue}");

		if (countIsVariantOf == 0) {
			writeMessageToCompletenessReport("Error - Service Model does not declare gr:isVariantOf.");
			throw new CompletenessException("Service Model does not declare gr:isVariantOf.");
		} else if (countIsVariantOf > 1) {
			writeMessageToCompletenessReport("Error - Service Model declares more than one gr:isVariantOf.");
			throw new CompletenessException("Service Model declares more than one gr:isVariantOf.");
		} else if (countIsVariantOf == 1) {
			writeMessageToCompletenessReport("OK - Service Model gr:isVariantOf:");
			RDFNode node = oneVarOneSolutionQuery("{<" + smi_uri + "> gr:isVariantOf ?var}");
			writeMessageToCompletenessReport(node.toString());
		}
		
		this.checkClassificationDimensionsInSD(smi_uri);

		// now bring back the cached model with BP inside in order to use it for relations with instances checks
		modelMem = cachedModel;

		// for all Service Model properties from BP:
		// if the SD's SM has a relation equal to uri
		// then the var must be an instance of rangeUri
		for(BrokerPolicyClass bpc:bp.getServiceModelMap().values())
		{
			for(Subproperty sp:bpc.getPropertyMap().values())
			{
				RDFNode qvNode = oneVarOneSolutionQuery("{<" + smi_uri + "> <" + sp.getUri() + "> ?var;}");
				if(qvNode != null)
				{	// there is a relation with the SD's SM
					Integer countQVs = countQuery("{<" + qvNode.toString() + "> rdf:type <" + sp.getRangeUri() + ">}");
					if(countQVs == 0)
					{	// found relation with non-existent instance, throw exception
						writeMessageToCompletenessReport("Error - SD's Service model instance has a " + sp.getUri() + " relation with non existent " + sp.getRangeUri() + " " + qvNode.toString() + ".");
						throw new CompletenessException("SD's Service model instance has a " + sp.getUri() + " relation with non existent " + sp.getRangeUri() + " " + qvNode.toString() + ".");						
					}
					else
					{
						writeMessageToCompletenessReport("OK - SD's Service model instance has a correct " + sp.getUri() + " relation with the " + sp.getRangeUri() + " " + qvNode.toString() + ".");						
					}
				}
			}
		}
	}
	
	private void runCompletenessCompliance(Object... dataToCheck) throws IOException, CompletenessException, ComplianceException {
		List<ClassInstancePair> qvPairList = null;
			qvPairList = this.completenessCheck(dataToCheck);

		// if BP data are in InputStream, reset it to reuse it
		for(int i=0;i<dataToCheck.length;i++)
		{
			if(dataToCheck[i] instanceof InputStream)
			{
				((InputStream) dataToCheck[i]).reset();
			}
		}
		
		// Perform compliance check
		if (qvPairList != null) {
				this.complianceCheck(dataToCheck, qvPairList);
		}
	}

	private void acquireMemoryForData(OntModelSpec spec) {
		modelMem = ModelFactory.createOntologyModel(spec);
	}

	protected void addDataToJenaModel(Object inputData) throws IOException {
		if(inputData instanceof String)
		{	// load from file
			this.addDataFromFile((String)inputData);
		}
		else if(inputData instanceof InputStream)
		{	// load from stream
			this.addDataFromInputStream((InputStream)inputData);
		}
		else if(inputData instanceof Object[])
		{	// load each one recursively
			for(int i=0;i<((Object[])inputData).length;i++)
			{
				this.addDataToJenaModel(((Object[])inputData)[i]);
			}
		}
	}

	protected void addDataFromInputStream(InputStream inputData) throws IOException 
	{
		modelMem.read(inputData, null, "TTL");
		inputData.close();
	}

	protected void addDataFromFile(String filePath) throws IOException {
		// System.out.println("Loading from " + file + " File");
		InputStream in = FileManager.get().open(filePath);
		modelMem.read(in, null, "TTL");
		in.close();		
	}
	
	public void getBrokerPolicy(Object bpFileData) throws SecurityException,
			IllegalArgumentException, NoSuchMethodException,
			ClassNotFoundException, InstantiationException,
			IllegalAccessException, InvocationTargetException, IOException {

		// Initial Creation
		//acquireMemoryForData(OntModelSpec.RDFS_MEM);

		// Add the BP into the Jena model
		addDataToJenaModel(bpFileData);

		// First, construct the objects corresponding to broker policy classes
		bp.setServiceModelMap(getBrokerPolicyClassMap(USDL_CORE, "ServiceModel"));

		//validateBrokerPolicy(bpFileData);

		bp.setServiceLevelProfileMap(getBrokerPolicyClassMap(USDL_SLA,
				"ServiceLevelProfile"));
		bp.setServiceLevelMap(getBrokerPolicyClassMap(USDL_SLA, "ServiceLevel"));
		bp.setServiceLevelExpressionMap(getBrokerPolicyClassMap(USDL_SLA,
				"ServiceLevelExpression"));
		bp.setExpressionVariableMap(getBrokerPolicyClassMap(USDL_SLA,
				"Variable"));
		bp.setQuantitativeValueIntegerMap(getBrokerPolicyClassMap(GR,
				"QuantitativeValueInteger"));
		bp.setQuantitativeValueFloatMap(getBrokerPolicyClassMap(GR,
				"QuantitativeValueFloat"));
		bp.setQualitativeValueMap(getBrokerPolicyClassMap(GR,
		"QualitativeValue"));

		// Next, construct the objects corresponding to broker policy
		// (QuantitativeValue) instances
		List<String> quantVSubclassList = new ArrayList<String>(); // this list
																// contains the
																// URIs of all
																// QuantitativeValue
																// (Integer and
																// Float)
																// subclasses

		// Add QuantitativeValueInteger subclasses into the list
		Iterator<BrokerPolicyClass> iterInt = bp
				.getQuantitativeValueIntegerMap().values().iterator();
		while (iterInt.hasNext()) {
			quantVSubclassList.add((iterInt.next()).getUri());
		}

		// Add QuantitativeValueFloat subclasses into the list
		Iterator<BrokerPolicyClass> iterFl = bp.getQuantitativeValueFloatMap()
				.values().iterator();
		while (iterFl.hasNext()) {
			quantVSubclassList.add((iterFl.next()).getUri());
		}

		bp.setQuantitativeValueMap(getQuantitativeValueMap(quantVSubclassList));
		
		// Next, construct the objects corresponding to broker policy
		// (QualitativeValue) instances
		List<String> qualVSubclassList = new ArrayList<String>(); // this list
																// contains the
																// URIs of all
																// QualitativeValue

		// Add QuantitativeValueInteger subclasses into the list
		Iterator<BrokerPolicyClass> iterQual = bp
				.getQualitativeValueMap().values().iterator();
		while (iterQual.hasNext()) {
			qualVSubclassList.add((iterQual.next()).getUri());
		}

		bp.setQualitativeValueMapWithInstances(getQualitativeValueMap(qualVSubclassList));
		
		// indicate whether this BP has SLP in connection
		checkBpHasSLPInConnection();
	}

	public void validateBrokerPolicy(Object bpFileData) throws IOException,
		NoSuchMethodException, ClassNotFoundException,
		InstantiationException, IllegalAccessException,
		InvocationTargetException, BrokerPolicyException, CompletenessException, ComplianceException {
		
		this.validateBrokerPolicy(new Object[] {bpFileData});
	}
	
	public void validateBrokerPolicy(Object... bpFileData) throws IOException,
			NoSuchMethodException, ClassNotFoundException,
			InstantiationException, IllegalAccessException,
			InvocationTargetException, BrokerPolicyException, CompletenessException, ComplianceException {
		
		// Initial Creation
		//acquireMemoryForData(OntModelSpec.RDFS_MEM);

		// Add the BP into the Jena model
		addDataToJenaModel(bpFileData);
		
		writeMessageToBrokerPolicyReport("##################");
		writeMessageToBrokerPolicyReport("Broker Policy Check");
		writeMessageToBrokerPolicyReport("##################");

		// First, construct the objects corresponding to broker policy classes
		bp.setServiceModelMap(getBrokerPolicyClassMap(USDL_CORE, "ServiceModel"));
		// if ServiceModelMap is found empty then it means we have no subclass of ServiceModel defined; throw Exception
		if(bp.getServiceModelMap().isEmpty())
		{
			writeMessageToBrokerPolicyReport("Error - No subClass of usdl-core:ServiceModel was found in the Broker Policy.");
			throw new BrokerPolicyException("No subClass of usdl-core:ServiceModel was found in the Broker Policy.");
		}
		else if(bp.getServiceModelMap().size() > 1)
		{	// more than one subclass, throw exception
			writeMessageToBrokerPolicyReport("Error - More than one subclasses of usdl-core:ServiceModel was found in the Broker Policy.");
			throw new BrokerPolicyException("More than one subclasses of usdl-core:ServiceModel was found in the Broker Policy.");
		} 
		writeMessageToBrokerPolicyReport("SubClass of usdl-core:ServiceModel was found in the Broker Policy: " + bp.getServiceModelMap().keySet().iterator().next());
		
		// check that single instance of Service Model class exists. Will take the first class from iterator, already checked that there is only one subclass.
		Integer smInstanceCount = countQuery("{?var a <" + bp.getServiceModelMap().keySet().iterator().next() + ">}");
		if(smInstanceCount > 1)
		{	// more than one instances of service model, throw exception
			writeMessageToBrokerPolicyReport("Error - More than one instances of Service Model was found in the Broker Policy.");
			throw new BrokerPolicyException("More than one instances of Service Model was found in the Broker Policy.");
		}

		RDFNode smInstance = oneVarOneSolutionQuery("{?var a <" + bp.getServiceModelMap().keySet().iterator().next() + ">}");
		if(smInstance == null)
		{
			writeMessageToBrokerPolicyReport("Error - No Service Model instance was found in the Broker Policy.");
			throw new BrokerPolicyException("No Service Model instance was found in the Broker Policy.");
		}
		writeMessageToBrokerPolicyReport("Service Model instance was found in the Broker Policy: " + smInstance.toString());
		
		// check that SM instance is connected to fc:rootConcept via the usdl-core-cb:hasClassificationDimension
		Integer rcHasClassificationDimensionCount = countQuery("{<"+ smInstance.toString() + "> usdl-core-cb:hasClassificationDimension <" + FC + "rootConcept>}");
		if(rcHasClassificationDimensionCount == 0)
		{	// more than one instances of service model, throw exception
			writeMessageToBrokerPolicyReport("Error - Service Model instance is not connected to fc:rootConcept via the usdl-core-cb:hasClassificationDimension.");
			throw new BrokerPolicyException("Service Model instance is not connected to fc:rootConcept via the usdl-core-cb:hasClassificationDimension.");
		}
		
		
		// check that single instance of usdl-core:EntityInvolvement class exists.
		Integer eiInstanceCount = countQuery("{?var a usdl-core:EntityInvolvement}");
		if(eiInstanceCount > 1)
		{	// more than one instances of entity involvement, throw exception
			writeMessageToBrokerPolicyReport("Error - More than one instances of Entity Involvement was found in the Broker Policy.");
			throw new BrokerPolicyException("More than one instances of Entity Involvement was found in the Broker Policy.");
		}

		RDFNode eiInstance = oneVarOneSolutionQuery("{?var a usdl-core:EntityInvolvement}");
		if(eiInstance == null)
		{
			writeMessageToBrokerPolicyReport("Error - No Entity Involvement instance was found in the Broker Policy.");
			throw new BrokerPolicyException("No Entity Involvement instance was found in the Broker Policy.");
		}
		writeMessageToBrokerPolicyReport("Entity Involvement instance was found in the Broker Policy: " + eiInstance.toString());

		// check that single Service Model instance hasEntityInvolvement association with single Entity Involvement instance.
		Integer heiAssociationsCount = countQuery("{<"+ smInstance.toString() + "> usdl-core:hasEntityInvolvement <" + eiInstance.toString() + ">}");
		if(heiAssociationsCount == 0)
		{
			writeMessageToBrokerPolicyReport("Error - Service Model instance does not have hasEntityInvolvement association with Entity Involvement instance.");
			throw new BrokerPolicyException("Service Model instance does not have hasEntityInvolvement association with Entity Involvement instance.");
		}
		writeMessageToBrokerPolicyReport("Service Model instance correctly has hasEntityInvolvement association with Entity Involvement instance.");

		// check that single Entity Involvement instance is associated with the intermediary instance of the class BusinessRole via the object property withBusinessRole
		Integer wbrAssociationsCount = countQuery("{<"+ eiInstance.toString() + "> usdl-core:withBusinessRole <" + USDL_BUSINESS_ROLES + "intermediary>}");
		if(wbrAssociationsCount == 0)
		{
			writeMessageToBrokerPolicyReport("Error - Entity Involvement instance is not associated with the intermediary instance of the class BusinessRole via the object property withBusinessRole.");
			throw new BrokerPolicyException("Entity Involvement instance is not associated with the intermediary instance of the class BusinessRole via the object property withBusinessRole.");
		}
		writeMessageToBrokerPolicyReport("Entity Involvement instance is associated with the intermediary instance of the class BusinessRole via the object property withBusinessRole.");

		// check that single Business Entity instance exists for the Cloud Platform
		Integer beInstanceCount = countQuery("{?var a gr:BusinessEntity}");
		if(beInstanceCount == 0 || beInstanceCount > 1)
		{	// more than one instances of business entity, throw exception
			writeMessageToBrokerPolicyReport("Error - Not exactly one instance of Business Entity was found in the Broker Policy.");
			throw new BrokerPolicyException("Not exactly one instance of Business Entity was found in the Broker Policy.");
		}
		writeMessageToBrokerPolicyReport("Exactly one instance of Business Entity was found in the Broker Policy.");

		RDFNode beInstance = oneVarOneSolutionQuery("{?var a gr:BusinessEntity}");
		if(beInstance == null)
		{
			writeMessageToBrokerPolicyReport("Error - Business Entity instance for the Cloud Platform does not exist in the Broker Policy.");
			throw new BrokerPolicyException("Business Entity instance for the Cloud Platform does not exist in the Broker Policy.");
		}
		writeMessageToBrokerPolicyReport("Business Entity instance for the Cloud Platform found in the Broker Policy: " + beInstance.toString());

		// check that single Entity Involvement instance is associated with the single Business Entity instance via the ofBusinessEntity relation.
		Integer obeAssociationsCount = countQuery("{<"+ eiInstance.toString() + "> usdl-core:ofBusinessEntity <"+ beInstance.toString() + ">}");
		if(obeAssociationsCount == 0)
		{
			writeMessageToBrokerPolicyReport("Error - Entity Involvement instance is not associated with the Business Entity instance via the ofBusinessEntity relation.");
			throw new BrokerPolicyException("Entity Involvement instance is not associated with the Business Entity instance via the ofBusinessEntity relation.");
		}
		writeMessageToBrokerPolicyReport("Entity Involvement instance is associated with the Business Entity instance via the ofBusinessEntity relation.");
		
		// if BP data are in InputStream, reset it to reuse it
		for(int i=0;i<bpFileData.length;i++)
		{
			if(bpFileData[i] instanceof InputStream)
			{
				((InputStream) bpFileData[i]).reset();
			}
		}
		
		// read the broker policy classes
		this.getBrokerPolicy(bpFileData);
		
		/*
		 * For every Framework declaration, the range should be declared as subclass of Quantitative or Qualitative Value.  
		 */
		for(BrokerPolicyClass bpc:bp.getServiceModelMap().values())
		{
			for(Subproperty sp:bpc.getPropertyMap().values())
			{
				String range = sp.getRangeUri();
				/*
				 * If it's the SLP declaration, omit it
				 */
				Integer countIfSlp = countQuery("{<" + range + "> rdfs:subClassOf usdl-sla:ServiceLevelProfile}");
				if(countIfSlp != 0) continue;
				
				Integer countQuantitativeValueIntegerSubclass = countQuery("{<" + range + "> rdfs:subClassOf gr:QuantitativeValueInteger}");
				Integer countQuantitativeValueFloatSubclass = countQuery("{<" + range + "> rdfs:subClassOf gr:QuantitativeValueFloat}");
				Integer countQualitativeValueSubclass = countQuery("{<" + range + "> rdfs:subClassOf gr:QualitativeValue}");
				if(countQuantitativeValueIntegerSubclass == 0 && countQuantitativeValueFloatSubclass == 0 && countQualitativeValueSubclass == 0)
				{	// problem, found a range in Framework declaration that is not Quantitative or Qualitative Value
					writeMessageToBrokerPolicyReport("Error - Range " + range + " declared in \"Framework\" subproperty " + sp.getUri() + " is not a subclass of Quantitative or Qualitative Value.");
					throw new BrokerPolicyException("Error - Range " + range + " declared in \"Framework\" subproperty " + sp.getUri() + " is not a subclass of Quantitative or Qualitative Value.");
				}
			}
		}
		
		// At least one SLP should exist
		if(bp.getServiceLevelProfileMap() == null || bp.getServiceLevelProfileMap().size() <1)
		{
			writeMessageToBrokerPolicyReport("Error - No Service Level Profile exists in Broker Policy.");
			throw new BrokerPolicyException("No Service Level Profile exists in Broker Policy.");
		}
		writeMessageToBrokerPolicyReport(bp.getServiceLevelProfileMap().size() + " Service Level Profile(s) were found in Broker Policy.");

		// At least one SL should exist
		if(bp.getServiceLevelMap() == null || bp.getServiceLevelMap().size() <1)
		{
			writeMessageToBrokerPolicyReport("Error - No Service Level exists in Broker Policy.");
			throw new BrokerPolicyException("No Service Level exists in Broker Policy.");
		}
		writeMessageToBrokerPolicyReport(bp.getServiceLevelMap().size() + " Service Level(s) were found in Broker Policy.");

		// for the single SM, at least one SLP should exist
		for(BrokerPolicyClass bpc : bp.getServiceModelMap().values())
		{
			if(bpc.getPropertyMap().values().size() < 1)
			{
				writeMessageToBrokerPolicyReport("Error - Service Model " + bpc.getUri() + " is not connected to at least one Service Level Profile.");
				throw new BrokerPolicyException("Service Model " + bpc.getUri() + " is not connected to at least one Service Level Profile.");
			}
			writeMessageToBrokerPolicyReport("Service Model " + bpc.getUri() + " is connected to some Service Level Profile(s).");
		}

		// for each SLP, at least one SL should exist
		for(BrokerPolicyClass bpc : bp.getServiceLevelProfileMap().values())
		{
			if(bpc.getPropertyMap().values().size() < 1 || allSubpropertyRangesAreNull(bpc.getPropertyMap().values()))
			{
				writeMessageToBrokerPolicyReport("Error - Service Level Profile " + bpc.getUri() + " is not connected to at least one Service Level.");
				throw new BrokerPolicyException("Service Level Profile " + bpc.getUri() + " is not connected to at least one Service Level.");
			}
			writeMessageToBrokerPolicyReport("Service Level Profile " + bpc.getUri() + " is connected to some Service Level(s).");
		}

		// for each SL, at least one SLE should exist
		for(BrokerPolicyClass bpc : bp.getServiceLevelMap().values())
		{
			if(bpc.getPropertyMap().values().size() < 1 || allSubpropertyRangesAreNull(bpc.getPropertyMap().values()))
			{
				writeMessageToBrokerPolicyReport("Error - Service Level " + bpc.getUri() + " is not connected to exactly one Service Level Expression.");
				throw new BrokerPolicyException("Service Level " + bpc.getUri() + " is not connected to exactly one Service Level Expression.");
			}
			
			for(Subproperty sle:bpc.getPropertyMap().values())
			{
				if(!bp.getServiceLevelExpressionMap().containsKey(sle.getRangeUri()))
				{
					writeMessageToBrokerPolicyReport("Error - Service Level Expression " + sle.getRangeUri() + " does not exist.");
					throw new BrokerPolicyException("Service Level Expression " + sle.getRangeUri() + " does not exist.");				
				}
			}
			writeMessageToBrokerPolicyReport("Service Level " + bpc.getUri() + " is connected to exactly one Service Level Expression: " + bpc.getPropertyMap().values().iterator().next().getRangeUri());
		}
		
		// for each SLE, exactly one Variable should exist
		for(BrokerPolicyClass bpc : bp.getServiceLevelExpressionMap().values())
		{
			if(bpc.getPropertyMap().values().size() < 1 || allSubpropertyRangesAreNull(bpc.getPropertyMap().values()))
			{
				writeMessageToBrokerPolicyReport("Error - Service Level Expression " + bpc.getUri() + " is not connected to at least one Variable.");
				throw new BrokerPolicyException("Service Level Expression " + bpc.getUri() + " is not connected to at least one Variable.");
			}
			if(!bp.getExpressionVariableMap().containsKey(bpc.getPropertyMap().values().iterator().next().getRangeUri()))
			{
				writeMessageToBrokerPolicyReport("Error - Variable " + bpc.getPropertyMap().values().iterator().next().getRangeUri() + " does not exist.");
				throw new BrokerPolicyException("Variable " + bpc.getPropertyMap().values().iterator().next().getRangeUri() + " does not exist.");				
			}
			writeMessageToBrokerPolicyReport("Service Level Expression " + bpc.getUri() + " is connected to at least one Variable");
		}
		
		// for each Variable, exactly one QV should exist
		for(BrokerPolicyClass bpc : bp.getExpressionVariableMap().values())
		{
			if(bpc.getPropertyMap().values().size() != 1)
			{
				writeMessageToBrokerPolicyReport("Error - Variable " + bpc.getUri() + " is not connected to exactly one QV.");
				throw new BrokerPolicyException("Variable " + bpc.getUri() + " is not connected to exactly one QV.");
			}
			writeMessageToBrokerPolicyReport("Variable " + bpc.getUri() + " is connected to exactly one QV: " + bpc.getPropertyMap().values().iterator().next().getRangeUri());
		}
		
		// No two different Variables must be connected to the same VC.
		for(BrokerPolicyClass v1 : bp.getExpressionVariableMap().values())
		{
			for(BrokerPolicyClass v2 : bp.getExpressionVariableMap().values())
			{
				for(Subproperty sp1:v1.getPropertyMap().values())
				{
					for(Subproperty sp2:v2.getPropertyMap().values())
					{
						if(!sp1.getDomainUri().equals(sp2.getDomainUri()))
						{	// different variables
							if(sp1.getRangeUri().equals(sp2.getRangeUri()))
							{	// same range, throw exception
								writeMessageToBrokerPolicyReport("Error - Variables " + sp1.getDomainUri() + " and " + sp2.getDomainUri() + " are connected to the same value class " + sp1.getRangeUri() + ".");
								throw new BrokerPolicyException("Variables " + sp1.getDomainUri() + " and " + sp2.getDomainUri() + " are connected to the same value class " + sp1.getRangeUri() + ".");
							}
						}
					}
				}
			}
		}
		
		/*
		 * All gr:QuantitativeValueFloat sub-classes should declare:
		 * 1) gr:hasMinValueFloat
		 * 2) gr:hasMaxValueFloat
		 */
		for(String qvf:bp.getQuantitativeValueFloatMap().keySet())
		{
			// gr:hasMinValueFloat
			checkCorrectFloatDeclaration(qvf, "gr:hasMinValueFloat");
			
			// gr:hasMaxValueFloat
			checkCorrectFloatDeclaration(qvf, "gr:hasMaxValueFloat");
		}
		
		/*
		 * All gr:QuantitativeValueInteger sub-classes should declare:
		 * 1) gr:hasMinValueInteger
		 * 2) gr:hasMaxValueInteger
		 */
		for(String qvi:bp.getQuantitativeValueIntegerMap().keySet())
		{
			// gr:hasMinValueInteger
			checkCorrectIntegerDeclaration(qvi, "gr:hasMinValueInteger");
			
			// gr:hasMaxValueInteger
			checkCorrectIntegerDeclaration(qvi, "gr:hasMaxValueInteger");
		}
		
		/*
		 * All gr:QuantitativeValue sub-classes should declare:
		 * 1) gr:hasUnitOfMeasurement
		 */
		for(String qv:bp.getQuantitativeValueMap().keySet())
		{
			// gr:hasUnitOfMeasurement
			checkCorrectStringDeclaration(qv, "gr:hasUnitOfMeasurement");
		}
		
		/*
		 * Check that Variables used in Broker Policy are declared in the "framework" part.
		 * This goes somewhat like this:
		 * for each used Variable (V):
		 * 1) Find the range (R) of the sub-properties of usdl-sla-cb:hasDefaultQuantitativeValue or usdl-sla-cb:hasDefaultQualitativeValue where (V) is the domain.
		 * 2) Find the domain (D) of the sub-property (must be exactly one) of gr:quantitativeProductOrServiceProperty or gr:qualitativeProductOrServiceProperty where (R) is the range.
		 * 3) (D) should be the Service Model subclass.
		 * All these data are already stored in the HashMaps of bp.
		 */
		for(BrokerPolicyClass variableV : bp.getExpressionVariableMap().values())
		{	// this iterates the Variables ClassMap
			Boolean variableExistsInFramework = false;
			for(Subproperty spOfVariableV: variableV.getPropertyMap().values())
			{	// This iterates the subproperties that hold domain/range where variableV is domain
				String rangeR = spOfVariableV.getRangeUri();
				// service model is only one
				for(Subproperty spOfSm: bp.getServiceModelMap().values().iterator().next().getPropertyMap().values())
				{	// this iterates the subproperties that hold domain/range where the Service Model is domain
					if(rangeR.equals(spOfSm.getRangeUri()))
					{	// success!
						if(variableExistsInFramework)
						{	// we have found the value class second time, throw exception
							writeMessageToBrokerPolicyReport("Error - Value class " + variableV.getPropertyMap().values().iterator().next().getRangeUri() + " is declared second time in properties of framework declaration.");
							throw new BrokerPolicyException("Value class " + variableV.getPropertyMap().values().iterator().next().getRangeUri() + " is declared second time in properties of framework declaration.");							
						}
						variableExistsInFramework = true;
					}
				}
			}
			
			if(!variableExistsInFramework)
			{
				writeMessageToBrokerPolicyReport("Error - Variable " + variableV.getUri() + " does not exist in framework declaration.");
				throw new BrokerPolicyException("Variable " + variableV.getUri() + " does not exist in framework declaration.");
			}
			writeMessageToBrokerPolicyReport("Variable " + variableV.getUri() + " exists in framework declaration and points to: " + variableV.getPropertyMap().values().iterator().next().getRangeUri() + " shared value class.");
		}
		
		/*
		 * if a Service Level Profile has been created and is bound to the Broker Policy instance,
		 * then we should check BP for completeness/compliance too
		 */
		if(bpHasSLPInConnection())
		{	// found SLP connection, run completeness/compliance
			writeMessageToBrokerPolicyReport("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
			writeMessageToBrokerPolicyReport("This BP has also SLP information inside! Will run completeness/compliance algorithm on it!");
			writeMessageToBrokerPolicyReport("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
			this.runCompletenessCompliance(bpFileData);			
		}

		writeMessageToBrokerPolicyReport("");
	}

	public boolean bpHasSLPInConnection() {
		return this.bpHasSLPInConnection;
	}

	private void checkBpHasSLPInConnection()
			throws IOException {
		RDFNode smInstance = oneVarOneSolutionQuery("{?var rdf:type <" + bp.getServiceModelMap().keySet().iterator().next()
				+ ">}");
		for(String slpClassUri:bp.getServiceLevelProfileMap().keySet())
		{
			for(BrokerPolicyClass smBpc:bp.getServiceModelMap().values())
			{
				for(Subproperty smSp:smBpc.getPropertyMap().values())
				{
					if(smSp.getRangeUri().equals(slpClassUri))
					{	// we found the hasSLP property
						String hasSlpProperty = smSp.getUri();
						// check if broker policy instance is connected with this
						// property to something (which denotes an SLP connection).
						RDFNode slpConnection = oneVarOneSolutionQuery("{<" + smInstance.toString() + "> <" + hasSlpProperty + "> ?var}");
						if(slpConnection != null)
						{	// found SLP connection
							this.bpHasSLPInConnection = true;
							return;
						}
					}
				}
			}
		}
		this.bpHasSLPInConnection = false;
		return;
	}

	public void checkCorrectStringDeclaration(String stringClassUri, String relationToLookFor) throws BrokerPolicyException {
		String subClassOf = "gr:QuantitativeValue";
		XSDDatatype datatypeToLookFor = XSDDatatype.XSDstring;
		
		RDFNode valueNode = checkCorrectQvType(stringClassUri,
				relationToLookFor, subClassOf, datatypeToLookFor);
		
		try {
			valueNode.asLiteral().getString();
		} catch (DatatypeFormatException e) {
			writeMessageToComplianceReport("For " + subClassOf + " URI: " + stringClassUri + " property " + relationToLookFor + " must be associated with " + datatypeToLookFor.getURI() + " value");
			throw new BrokerPolicyException("For " + subClassOf + " URI: " + stringClassUri + " property " + relationToLookFor + " must be associated with " + datatypeToLookFor.getURI() + " value");
		}
	}
	
	public void checkCorrectIntegerDeclaration(String integerClassUri, String relationToLookFor) throws BrokerPolicyException {
		String subClassOf = "gr:QuantitativeValueInteger";
		XSDDatatype datatypeToLookFor = XSDDatatype.XSDinteger;
		
		RDFNode valueNode = checkCorrectQvType(integerClassUri,
				relationToLookFor, subClassOf, datatypeToLookFor);
		
		try {
			valueNode.asLiteral().getInt();
		} catch (DatatypeFormatException e) {
			writeMessageToComplianceReport("For " + subClassOf + " URI: " + integerClassUri + " property " + relationToLookFor + " must be associated with " + datatypeToLookFor.getURI() + " value");
			throw new BrokerPolicyException("For " + subClassOf + " URI: " + integerClassUri + " property " + relationToLookFor + " must be associated with " + datatypeToLookFor.getURI() + " value");
		}
	}
	
	public void checkCorrectFloatDeclaration(String floatClassUri, String relationToLookFor) throws BrokerPolicyException {
		String subClassOf = "gr:QuantitativeValueFloat";
		XSDDatatype datatypeToLookFor = XSDDatatype.XSDfloat;
		
		RDFNode valueNode = checkCorrectQvType(floatClassUri,
				relationToLookFor, subClassOf, datatypeToLookFor);
		
		try {
			valueNode.asLiteral().getFloat();
		} catch (DatatypeFormatException e) {
			writeMessageToComplianceReport("For " + subClassOf + " URI: " + floatClassUri + " property " + relationToLookFor + " must be associated with " + datatypeToLookFor.getURI() + " value");
			throw new BrokerPolicyException("For " + subClassOf + " URI: " + floatClassUri + " property " + relationToLookFor + " must be associated with " + datatypeToLookFor.getURI() + " value");
		}
	}

	public RDFNode checkCorrectQvType(String classUri,
			String relationToLookFor, String subClassOf,
			XSDDatatype datatypeToLookFor) throws BrokerPolicyException {
		RDFNode valueNode = oneVarOneSolutionQuery("{<"
				+ classUri + "> " + relationToLookFor + " ?var}");
		if(valueNode == null)
		{
			writeMessageToBrokerPolicyReport("Error - For " + subClassOf + " URI: " + classUri + " property " + relationToLookFor + " is not declared");
			throw new BrokerPolicyException("For " + subClassOf + " URI: " + classUri + " property " + relationToLookFor + " is not declared");				
		}
		
		if (!valueNode.isLiteral())
		{
			writeMessageToBrokerPolicyReport("Error - For " + subClassOf + " URI: " + classUri + " property " + relationToLookFor + " must be associated with a literal");
			throw new BrokerPolicyException("For " + subClassOf + " URI: " + classUri + " property " + relationToLookFor + " must be associated with a literal");
		}
		
		if (!valueNode.asLiteral().getDatatype().equals(datatypeToLookFor))
		{
			writeMessageToComplianceReport("Error - For " + subClassOf + " URI: " + classUri + " property " + relationToLookFor + " must be associated with " + datatypeToLookFor.getURI() + " type");
			throw new BrokerPolicyException("For " + subClassOf + " URI: " + classUri + " property " + relationToLookFor + " must be associated with " + datatypeToLookFor.getURI() + " type");
		}
		return valueNode;
	}

	private boolean allSubpropertyRangesAreNull(Collection<Subproperty> values) 
	{
		for(Subproperty value: values)
		{
			if(value.getRangeUri() != null)
			{
				return false;
			}
		}
		return true;
	}

	private Map<String, BrokerPolicyClass> getBrokerPolicyClassMap(
			String prefix, String classNameRDF) throws SecurityException,
			NoSuchMethodException, ClassNotFoundException,
			IllegalArgumentException, InstantiationException,
			IllegalAccessException, InvocationTargetException {

		// Construct the Map containing the objects corresponding to subclasses
		// of "classNameRDF"
		Map<String, BrokerPolicyClass> bpcMap = new HashMap<String, BrokerPolicyClass>();
		Property subclassOfProperty = ResourceFactory.createProperty(RDFS
				+ "subClassOf");
		Resource resource = ResourceFactory.createResource(prefix
				+ classNameRDF);
		ResIterator riOut = modelMem.listResourcesWithProperty(
				subclassOfProperty, resource);

		// For each subclass find the subproperties whose domain is the subclass
		while (riOut.hasNext()) {
			Resource subclassResource = riOut.next();

			BrokerPolicyClass bpc = new BrokerPolicyClass(
					subclassResource.toString());

			// Find properties with domain = URI of the subclass
			Map<String, Subproperty> propertyMap = new HashMap<String, Subproperty>();

			Property domainProperty = ResourceFactory.createProperty(RDFS
					+ "domain");
			ResIterator riIn = modelMem.listResourcesWithProperty(
					domainProperty, subclassResource);
			// For each subproperty found construct the corresponding object
			while (riIn.hasNext()) {
				Resource subclassPropertyResource = riIn.next();
				// Construction of the Subproperty object passing the URI to the
				// constructor
				Subproperty prop = new Subproperty(
						subclassPropertyResource.toString());
				// Set the URI of the domain subclass
				prop.setDomainUri(subclassResource.toString());

				// Find the URI of the range subclass
				Property rangeProperty = ResourceFactory.createProperty(RDFS
						+ "range");
				NodeIterator riIn2 = modelMem.listObjectsOfProperty(
						subclassPropertyResource, rangeProperty);
				while (riIn2.hasNext()) {
					RDFNode rangeNode = riIn2.next();
					// Set the URI of the range subclass
					prop.setRangeUri(rangeNode.toString());
				}
				propertyMap.put(prop.getUri(), prop); // put the Subproperty
														// object to the
														// propertyMap
			}
			bpc.setPropertyMap(propertyMap); // set the propertyMap to the
												// corresponding subclass (bpc)
			bpcMap.put(bpc.getUri(), bpc); // put the subclass
											// (BrokerPolicyClass object) to
											// corresponding subclass map
		}

		return bpcMap;
	}

	// The input to this method is a list containing the URIs of the QV
	// subclasses and it returns a map with key-value entries where key=URI of
	// the QV subclass and
	// value=the corresponding QuantitativeValue object
	private Map<String, QuantitativeValue> getQuantitativeValueMap(
			List<String> qvSubclassList) {

		// Construct the Map containing the objects corresponding to subclasses
		// of QuantitativeValue
		Map<String, QuantitativeValue> qvMap = new HashMap<String, QuantitativeValue>();
		Iterator<String> iter = qvSubclassList.iterator();

		// For each subclass of QV find the corresponding instances
		while (iter.hasNext()) {
			String qvSubclassUri = (String) iter.next();
			QuantitativeValue qvSubclass = new QuantitativeValue(qvSubclassUri);

			// Construct the Map containing the objects corresponding to
			// instances of the QuantitativeValue subclass
			Map<String, QuantitativeValueInstance> instanceMap = new HashMap<String, QuantitativeValueInstance>();

			Property typeProperty = ResourceFactory
					.createProperty(RDF + "type");
			Resource qvSubclassResource = ResourceFactory
					.createResource(qvSubclass.getUri());

			// Find the instances
			ResIterator ri = modelMem.listResourcesWithProperty(typeProperty,
					qvSubclassResource);

			// For each instance find the corresponding values
			while (ri.hasNext()) {
				Resource qvInstanceResource = ri.next();
				// Create QV instance object
				QuantitativeValueInstance instance = new QuantitativeValueInstance(
						qvInstanceResource.toString());

				Property hasMinValueIntegerProperty = ResourceFactory
						.createProperty(GR + "hasMinValueInteger");
				Property hasMaxValueIntegerProperty = ResourceFactory
						.createProperty(GR + "hasMaxValueInteger");
				Property hasMinValueFloatProperty = ResourceFactory
						.createProperty(GR + "hasMinValueFloat");
				Property hasMaxValueFloatProperty = ResourceFactory
						.createProperty(GR + "hasMaxValueFloat");
				Property hasUnitOfMeasurementProperty = ResourceFactory
						.createProperty(GR + "hasUnitOfMeasurement");

				// -------------------------------------------------------
				// Find the values
				NodeIterator riInMinInt = modelMem.listObjectsOfProperty(
						qvInstanceResource, hasMinValueIntegerProperty);
				while (riInMinInt.hasNext()) {
					RDFNode valueNode = riInMinInt.next();
					// Set the minimum integer value
					instance.setMinValue(valueNode.asLiteral().getInt());
				}

				NodeIterator riInMaxInt = modelMem.listObjectsOfProperty(
						qvInstanceResource, hasMaxValueIntegerProperty);
				while (riInMaxInt.hasNext()) {
					RDFNode valueNode = riInMaxInt.next();
					// Set the maximum integer value
					instance.setMaxValue(valueNode.asLiteral().getInt());
				}

				NodeIterator riInMinFl = modelMem.listObjectsOfProperty(
						qvInstanceResource, hasMinValueFloatProperty);
				while (riInMinFl.hasNext()) {
					RDFNode valueNode = riInMinFl.next();
					// Set the minimum float value
					instance.setMinValue(valueNode.asLiteral().getFloat());
				}

				NodeIterator riInMaxFl = modelMem.listObjectsOfProperty(
						qvInstanceResource, hasMaxValueFloatProperty);
				while (riInMaxFl.hasNext()) {
					RDFNode valueNode = riInMaxFl.next();
					// Set the maximum float value
					instance.setMaxValue(valueNode.asLiteral().getFloat());
				}

				NodeIterator riInUOM = modelMem.listObjectsOfProperty(
						qvInstanceResource, hasUnitOfMeasurementProperty);
				while (riInUOM.hasNext()) {
					RDFNode valueNode = riInUOM.next();
					// Set the unit of measurement value
					instance.setUnitOfMeasurement(valueNode.asLiteral()
							.getString());
				}
				// ----------------------------------------------------
				instanceMap.put(instance.getUri(), instance); // put the
																// instance
																// found into
																// the
																// corresponding
																// map

			}
			qvSubclass.setInstanceMap(instanceMap); // set the instance map of
													// the corresponding QV
													// subclass object
			qvMap.put(qvSubclass.getUri(), qvSubclass); // put the QV subclass
														// object into the map
														// that contains all QV
														// subclass objects
		}

		return qvMap;
	}

	// The input to this method is a list containing the URIs of the QV
	// subclasses and it returns a map with key-value entries where key=URI of
	// the QV subclass and
	// value=the corresponding QualitativeValue object
	private Map<String, QualitativeValue> getQualitativeValueMap(
			List<String> qvSubclassList) {

		// Construct the Map containing the objects corresponding to subclasses
		// of QualitativeValue
		Map<String, QualitativeValue> qvMap = new HashMap<String, QualitativeValue>();
		Iterator<String> iter = qvSubclassList.iterator();

		// For each subclass of QV find the corresponding instances
		while (iter.hasNext()) {
			String qvSubclassUri = (String) iter.next();
			QualitativeValue qvSubclass = new QualitativeValue(qvSubclassUri);

			// Construct the Map containing the objects corresponding to
			// instances of the QualitativeValue subclass
			Map<String, QualitativeValueInstance> instanceMap = new HashMap<String, QualitativeValueInstance>();

			Property typeProperty = ResourceFactory
					.createProperty(RDF + "type");
			Resource qvSubclassResource = ResourceFactory
					.createResource(qvSubclass.getUri());

			// Find the instances
			ResIterator ri = modelMem.listResourcesWithProperty(typeProperty,
					qvSubclassResource);

			// For each instance find the corresponding values
			while (ri.hasNext()) {
				Resource qvInstanceResource = ri.next();
				// Create QV instance object
				QualitativeValueInstance instance = new QualitativeValueInstance(
						qvInstanceResource.toString());

				// ----------------------------------------------------
				instanceMap.put(instance.getUri(), instance); // put the
																// instance
																// found into
																// the
																// corresponding
																// map

			}
			qvSubclass.setInstanceMap(instanceMap); // set the instance map of
													// the corresponding QV
													// subclass object
			qvMap.put(qvSubclass.getUri(), qvSubclass); // put the QV subclass
														// object into the map
														// that contains all QV
														// subclass objects
		}

		return qvMap;
	}
	
	private List<ClassInstancePair> completenessCheck(Object... fileData)
			throws IOException, CompletenessException {

		writeMessageToCompletenessReport("##################");
		writeMessageToCompletenessReport("Completeness Check");
		writeMessageToCompletenessReport("##################");

		// Add the file contents into the Jena model prior to caching it
		addDataToJenaModel(fileData);
		// cache the current modelMem with BP inside in order to use it later for relations with instances checks
		OntModel cachedModel = modelMem;

		// Init model now in order not to find BP stuff inside
		acquireMemoryForData(OntModelSpec.RDFS_MEM);

		// if BP data are in InputStream, reset it to reuse it
		for(int i=0;i<fileData.length;i++)
		{
			if(fileData[i] instanceof InputStream)
			{
				((InputStream) fileData[i]).reset();
			}
		}

		// Add the file contents into the Jena model
		addDataToJenaModel(fileData);

		// Usdl-core SD checks should be done only on pure SDs, not on BP-like-SD cases. Those types of checks run in minimal check flow for minimal SDs.
		if(!this.bpHasSLPInConnection())
		{
			writeMessageToCompletenessReport("----------------");
			writeMessageToCompletenessReport("Usdl-core completeness section:");
			writeMessageToCompletenessReport("----------------");
	
			// check that instance of Service Individual class exists. Will take the first one, could there be more than one? TODO
			RDFNode siInstance = oneVarOneSolutionQuery("{?var a usdl-core:ServiceIndividual}");
			if(siInstance == null)
			{
				writeMessageToCompletenessReport("Error - No Service Individual instance was found in the Service Description.");
				throw new CompletenessException("No Service Individual instance was found in the Service Description.");
			}
			writeMessageToCompletenessReport("Service Individual instance was found in the Service Description: " + siInstance.toString());		
			
			// check that instance of Service Individual is associated with a hasMakeAndModel relation with a Broker Policy
			RDFNode bpInstance = oneVarOneSolutionQuery("{<" + siInstance.toString() + "> gr:hasMakeAndModel ?var}");
			if(bpInstance == null)
			{
				writeMessageToCompletenessReport("Error - No Service Individual instance gr:hasMakeAndModel association was found with a Broker Policy.");
				throw new CompletenessException("No Service Individual instance gr:hasMakeAndModel association was found with a Broker Policy.");
			}
			writeMessageToCompletenessReport("Service Individual instance gr:hasMakeAndModel association was found with the Broker Policy: " + bpInstance.toString());		
			
			// check that instance of Entity Involvement exists
			RDFNode eiInstance = oneVarOneSolutionQuery("{?var a usdl-core:EntityInvolvement}");
			if(eiInstance == null)
			{
				writeMessageToCompletenessReport("Error - No Entity Involvement instance was found in the Service Description.");
				throw new CompletenessException("No Entity Involvement instance was found in the Service Description.");
			}
			writeMessageToCompletenessReport("Entity Involvement instance was found in the Service Description: " + eiInstance.toString());		
			
			// check that Service Individual instance is associated via a hasEntityInvolvement relation with the Entity Involvement instance
			Integer heiAssociationsCount = countQuery("{<" + siInstance.toString() + "> usdl-core:hasEntityInvolvement <" + eiInstance.toString() + ">}");
			if(heiAssociationsCount == 0)
			{
				writeMessageToCompletenessReport("Error - Service Individual instance is not associated via a hasEntityInvolvement relation with the Entity Involvement instance.");
				throw new CompletenessException("Service Individual instance is not associated via a hasEntityInvolvement relation with the Entity Involvement instance.");
			}
			writeMessageToCompletenessReport("Service Individual instance is associated via a hasEntityInvolvement relation with the Entity Involvement instance.");		
			
			// check that instance of Entity Involvement is associated via the withBusinessRole relation with the Provider instance of the class BusinessRoles
			Integer wbrAssociationsCountInstance = countQuery("{<" + eiInstance.toString() + "> usdl-core:withBusinessRole <" + USDL_BUSINESS_ROLES + "provider>}");
			if(wbrAssociationsCountInstance == 0)
			{
				writeMessageToCompletenessReport("Error - Entity Involvement instance is not associated via the withBusinessRole relation with the provider instance of the class BusinessRoles.");
				throw new CompletenessException("Entity Involvement instance is not associated via the withBusinessRole relation with the provider instance of the class BusinessRoles.");
			}
			writeMessageToCompletenessReport("Entity Involvement instance is associated via the withBusinessRole relation with the provider instance of the class BusinessRoles.");		
			
			// check that instance of Business Entity exists
			RDFNode beInstance = oneVarOneSolutionQuery("{?var a gr:BusinessEntity}");
			if(beInstance == null)
			{
				writeMessageToCompletenessReport("Error - No Business Entity instance was found in the Service Description.");
				throw new CompletenessException("No Business Entity instance was found in the Service Description.");
			}
			writeMessageToCompletenessReport("Business Entity instance was found in the Service Description: " + beInstance.toString());		
	
			// check that instance of Entity Involvement is associated via the ofBusinessEnity relation with the Business Entity instance
			Integer obeAssociationsCountInstance = countQuery("{<" + eiInstance.toString() + "> usdl-core:ofBusinessEntity <" + beInstance.toString() + ">}");
			if(obeAssociationsCountInstance == 0)
			{
				writeMessageToCompletenessReport("Error - Entity Involvement instance is not associated via the ofBusinessEnity relation with the Business Entity instance.");
				throw new CompletenessException("Entity Involvement instance is not associated via the ofBusinessEnity relation with the Business Entity instance.");
			}
			writeMessageToCompletenessReport("Entity Involvement instance is associated via the ofBusinessEnity relation with the Business Entity instance.");		
			
			writeMessageToCompletenessReport("----------------");
			writeMessageToCompletenessReport("Service Section:");
			writeMessageToCompletenessReport("----------------");
			String si_uri = null; // service instance uri
	
			// how many usdl-core:Service instances are present in the SD?
			int countSi = countQuery("{?si rdf:type usdl-core:Service}");
	
			if (countSi == 0) {
				writeMessageToCompletenessReport("Error - SD does not contain an instance of usdl-core:Service");
				throw new CompletenessException("SD does not contain an instance of usdl-core:Service");
			} else if (countSi > 1) {
				writeMessageToCompletenessReport("Error - SD must contain only 1 service instance");
				throw new CompletenessException("SD must contain only 1 service instance");
			} else if (countSi == 1) {
				writeMessageToCompletenessReport("OK - SD contains exactly 1 service instance of type usdl-core:Service");
				RDFNode node = oneVarOneSolutionQuery("{?var rdf:type usdl-core:Service}");
				si_uri = node.toString();
			}
		}
			writeMessageToCompletenessReport("-------------------------------");
			writeMessageToCompletenessReport("Service - ServiceModel Section:");
			writeMessageToCompletenessReport("-------------------------------");
			String smi_uri = null; // service model instance uri
	
			// -----------------------------------------------
			// Get the URI of the ServiceModel subclass from the BP
			Iterator<String> smcIter = bp.getServiceModelMap().keySet().iterator();
			String smc_uri = null;
	
			// we know there is only 1 service model class (broker policy file is
			// properly created)
			smc_uri = smcIter.next();
			// -----------------------------------------------
	
			/*int countSmi = countQuery("{<" + si_uri
					+ "> usdl-core:hasServiceModel ?var}"); // how many instances
															// are associated with
															// the usdl-core:Service
															// instance found
															// previously via the
															// usdl-core:hasServiceModel
															// property?
	
			if (countSmi == 0) {
				writeMessageToCompletenessReport("Error - Instance:");
				writeMessageToCompletenessReport(si_uri);
				writeMessageToCompletenessReport("is not connected to any instance via the appropriate property");
				writeMessageToCompletenessReport(USDL_CORE + "hasServiceModel");
				throw new CompletenessException("Instance: " + si_uri + " is not connected to any instance via the appropriate property " + USDL_CORE + "hasServiceModel");
			} else if (countSmi > 1) {
				writeMessageToCompletenessReport("Error - Instance: ");
				writeMessageToCompletenessReport(si_uri);
				writeMessageToCompletenessReport("must be connected to only 1 instance via the appropriate property");
				writeMessageToCompletenessReport(USDL_CORE + "hasServiceModel");
				throw new CompletenessException("Instance: " + si_uri + " must be connected to only 1 instance via the appropriate property " + USDL_CORE + "hasServiceModel");
			} else if (countSmi == 1) {
				writeMessageToCompletenessReport("OK - Instance: ");
				writeMessageToCompletenessReport(si_uri);
				writeMessageToCompletenessReport("is correctly connected to exactly 1 instance via the appropriate property");
				writeMessageToCompletenessReport(USDL_CORE + "hasServiceModel");*/
	
				// Get the service model instance
				RDFNode node = oneVarOneSolutionQuery("{?var rdf:type <" + smc_uri
						+ ">}");
				smi_uri = node.toString(); // Service model instance URI
				int countSmi2 = countQuery("{<" + smi_uri + "> rdf:type <"
						+ smc_uri + ">}"); // count the occurrences of this triple
											// in the file contents, the possible result is
											// either 0 or 1
				if (countSmi2 == 0) {// couldn't find an instance of the
										// correct service model subclass or
					writeMessageToCompletenessReport("Error - Instance");
					writeMessageToCompletenessReport(smi_uri);
					writeMessageToCompletenessReport("does not belong to the correct type:");
					writeMessageToCompletenessReport(smc_uri);
					throw new CompletenessException("Instance " + smi_uri + " does not belong to the correct type: " + smc_uri);
				} else { // countSmi2 = 1
					writeMessageToCompletenessReport("OK - Found exactly 1  instance of the correct type:");
					writeMessageToCompletenessReport(smc_uri);
					/*writeMessageToCompletenessReport("which is correctly connected to the instance:");
					writeMessageToCompletenessReport(si_uri);*/
	
					int countSmi3 = countQuery("{<" + smi_uri
							+ "> rdf:type ?someType}"); // maybe the service model
														// instance is associated
														// with other types, count
														// the number of types the
														// service model instance is
														// associated with
					if (countSmi3 > 1) {
						writeMessageToCompletenessReport("Error - The instance URI:");
						writeMessageToCompletenessReport(smi_uri);
						writeMessageToCompletenessReport("is falsely associated with other types");
						throw new CompletenessException("The instance URI: " + smi_uri + " is falsely associated with other types");
					} else {
						writeMessageToCompletenessReport("OK - Found exactly 1 instance of the correct type:");
						writeMessageToCompletenessReport(smc_uri);
						/*writeMessageToCompletenessReport("which is correctly connected to the instance:");
						writeMessageToCompletenessReport(si_uri);*/
						writeMessageToCompletenessReport("and no other types are associated with this instance");
						writeMessageToCompletenessReport("");
					}
				}
			//}

		// Usdl-core SD checks should be done only on pure SDs, not on BP-like-SD cases. Those types of checks run in minimal check flow for minimal SDs.
		if(!this.bpHasSLPInConnection())
		{
			
			// check gr:isVariantOf
			int countIsVariantOf = countQuery("{<" + smi_uri + "> gr:isVariantOf ?someValue}");
	
			if (countIsVariantOf == 0) {
				writeMessageToCompletenessReport("Error - Service Model does not declare gr:isVariantOf.");
				throw new CompletenessException("Service Model does not declare gr:isVariantOf.");
			} else if (countIsVariantOf > 1) {
				writeMessageToCompletenessReport("Error - Service Model declares more than one gr:isVariantOf.");
				throw new CompletenessException("Service Model declares more than one gr:isVariantOf.");
			} else if (countIsVariantOf == 1) {
				writeMessageToCompletenessReport("OK - Service Model gr:isVariantOf:");
				RDFNode isVariantOfNode = oneVarOneSolutionQuery("{<" + smi_uri + "> gr:isVariantOf ?var}");
				writeMessageToCompletenessReport(isVariantOfNode.toString());
			}			
		}

		this.checkClassificationDimensionsInSD(smi_uri);

		// now bring back the cached model with BP inside in order to use it for further checks
		modelMem = cachedModel;

		this.checkQuantitativeValuesRanges();
		
		// smCip: needed input for Service Model - Service Level Profile section
		// in stepCompletenessCheck method (smc_uri (subclass URI) is needed to
		// find the
		// subproperties connecting the Service Model subclass with the Service
		// Level Profile subclasses and smi_uri (instance URI) is needed as a
		// starting point
		// for the various checks
		ClassInstancePair smCip = new ClassInstancePair(smc_uri, smi_uri);

		// Perform the same checks as above for all the remaining sections of
		// the file contents

		// Service Model - Service Level Profile
		writeMessageToCompletenessReport("-------------------------------------------");
		writeMessageToCompletenessReport("ServiceModel - ServiceLevelProfile Section:");
		writeMessageToCompletenessReport("-------------------------------------------");
		List<ClassInstancePair> slpPairList = new ArrayList<ClassInstancePair>();
		slpPairList = stepCompletenessCheck(smCip, 0);
		Iterator<ClassInstancePair> slpIter = slpPairList.iterator();

		// Service Level Profile - Service Level
		writeMessageToCompletenessReport("-------------------------------------------");
		writeMessageToCompletenessReport("ServiceLevelProfile - ServiceLevel Section:");
		writeMessageToCompletenessReport("-------------------------------------------");
		List<ClassInstancePair> slPairListTotal = new ArrayList<ClassInstancePair>();
		List<ClassInstancePair> slPairList = new ArrayList<ClassInstancePair>();
		while (slpIter.hasNext()) {
			slPairList = stepCompletenessCheck(slpIter.next(), 1);
			slPairListTotal.addAll(slPairList);
		}
		Iterator<ClassInstancePair> slIterTotal = slPairListTotal.iterator();

		// Service Level - Service Level Expression
		writeMessageToCompletenessReport("----------------------------------------------");
		writeMessageToCompletenessReport("ServiceLevel - ServiceLevelExpression Section:");
		writeMessageToCompletenessReport("----------------------------------------------");
		List<ClassInstancePair> slePairListTotal = new ArrayList<ClassInstancePair>();
		List<ClassInstancePair> slePairList = new ArrayList<ClassInstancePair>();
		while (slIterTotal.hasNext()) {
			slePairList = stepCompletenessCheck(slIterTotal.next(), 2);
			slePairListTotal.addAll(slePairList);
		}
		Iterator<ClassInstancePair> sleIterTotal = slePairListTotal.iterator();

		// Service Level Expression - Variable
		writeMessageToCompletenessReport("------------------------------------------");
		writeMessageToCompletenessReport("ServiceLevelExpression - Variable Section:");
		writeMessageToCompletenessReport("------------------------------------------");
		List<ClassInstancePair> vPairListTotal = new ArrayList<ClassInstancePair>();
		List<ClassInstancePair> vPairList = new ArrayList<ClassInstancePair>();
		while (sleIterTotal.hasNext()) {
			vPairList = stepCompletenessCheck(sleIterTotal.next(), 3);
			vPairListTotal.addAll(vPairList);
		}
		Iterator<ClassInstancePair> vIterTotal = vPairListTotal.iterator();

		// Variable - Quantitative Value
		writeMessageToCompletenessReport("-------------------------------------");
		writeMessageToCompletenessReport("Variable - QuantitativeValue Section:");
		writeMessageToCompletenessReport("-------------------------------------");
		List<ClassInstancePair> qvPairListTotal = new ArrayList<ClassInstancePair>();
		List<ClassInstancePair> qvPairList = new ArrayList<ClassInstancePair>();
		while (vIterTotal.hasNext()) {
			qvPairList = stepCompletenessCheck(vIterTotal.next(), 4);
			qvPairListTotal.addAll(qvPairList);
		}

		return qvPairListTotal;
	}

	private void checkQuantitativeValuesRanges() throws CompletenessException 
	{
		for(QuantitativeValue qv:this.bp.getQuantitativeValueMap().values())
		{
			// get all instances of this QV, including those in SD
			RDFNode[] qvInstances = oneVarManySolutionsQuery("{?var rdf:type <" + qv.getUri() + ">}");
			// is this QV a range?
			Integer isRangeCount = countQuery("{<" + qv.getUri() + "> <" + USDL_CORE_CB + "isRange> ?var}");
			for(RDFNode qvInstance:qvInstances)
			{	// for every QV instance
				if(isRangeCount !=0)
				{	// it's range
					if(this.bp.getQuantitativeValueIntegerMap().containsKey(qv.getUri()))
					{	// integer range
						RDFNode hasMinValueInteger = oneVarOneSolutionQuery("{<" + qvInstance.toString() + "> <" + GR + "hasMinValueInteger> ?var}");
						if(hasMinValueInteger == null)
						{
							writeMessageToCompletenessReport("Error - QV instance " + qvInstance.toString() + " is integer range and does not declare hasMinValueInteger.");
							throw new CompletenessException("QV instance " + qvInstance.toString() + " is integer range and does not declare hasMinValueInteger.");
						}
						
						try {
							checkCorrectIntegerDeclaration(qvInstance.toString(), "gr:hasMinValueInteger");
						} catch (BrokerPolicyException e) {
							e.printStackTrace();
							throw new CompletenessException(e.getMessage());
						}
						writeMessageToCompletenessReport("QV instance " + qvInstance.toString() + " is integer range and declares hasMinValueInteger " + hasMinValueInteger.toString() + ".");
						
						RDFNode hasMaxValueInteger = oneVarOneSolutionQuery("{<" + qvInstance.toString() + "> <" + GR + "hasMaxValueInteger> ?var}");
						if(hasMaxValueInteger == null)
						{
							writeMessageToCompletenessReport("Error - QV instance " + qvInstance.toString() + " is integer range and does not declare hasMaxValueInteger.");
							throw new CompletenessException("QV instance " + qvInstance.toString() + " is integer range and does not declare hasMaxValueInteger.");
						}
						
						try {
							checkCorrectIntegerDeclaration(qvInstance.toString(), "gr:hasMaxValueInteger");
						} catch (BrokerPolicyException e) {
							e.printStackTrace();
							throw new CompletenessException(e.getMessage());
						}
						writeMessageToCompletenessReport("QV instance " + qvInstance.toString() + " is integer range and declares hasMaxValueInteger " + hasMaxValueInteger.toString() + ".");

					}
					else
					{	// float range
						RDFNode hasMinValueFloat = oneVarOneSolutionQuery("{<" + qvInstance.toString() + "> <" + GR + "hasMinValueFloat> ?var}");
						if(hasMinValueFloat == null)
						{
							writeMessageToCompletenessReport("Error - QV instance " + qvInstance.toString() + " is float range and does not declare hasMinValueFloat.");
							throw new CompletenessException("QV instance " + qvInstance.toString() + " is float range and does not declare hasMinValueFloat.");
						}
						
						try {
							checkCorrectFloatDeclaration(qvInstance.toString(), "gr:hasMinValueFloat");
						} catch (BrokerPolicyException e) {
							e.printStackTrace();
							throw new CompletenessException(e.getMessage());
						}
						writeMessageToCompletenessReport("QV instance " + qvInstance.toString() + " is float range and declares hasMinValueFloat " + hasMinValueFloat.toString() + ".");
						
						RDFNode hasMaxValueFloat = oneVarOneSolutionQuery("{<" + qvInstance.toString() + "> <" + GR + "hasMaxValueFloat> ?var}");
						if(hasMaxValueFloat == null)
						{
							writeMessageToCompletenessReport("Error - QV instance " + qvInstance.toString() + " is float range and does not declare hasMaxValueFloat.");
							throw new CompletenessException("QV instance " + qvInstance.toString() + " is float range and does not declare hasMaxValueFloat.");
						}
						
						try {
							checkCorrectFloatDeclaration(qvInstance.toString(), "gr:hasMaxValueFloat");
						} catch (BrokerPolicyException e) {
							e.printStackTrace();
							throw new CompletenessException(e.getMessage());
						}
						writeMessageToCompletenessReport("QV instance " + qvInstance.toString() + " is float range and declares hasMaxValueFloat " + hasMaxValueFloat.toString() + ".");
						
					}
				}
				else
				{	// it's simple value, NOT range
					if(this.bp.getQuantitativeValueIntegerMap().containsKey(qv.getUri()))
					{	// integer value
						RDFNode hasValueInteger = oneVarOneSolutionQuery("{<" + qvInstance.toString() + "> <" + GR + "hasValueInteger> ?var}");
						if(hasValueInteger == null)
						{
							writeMessageToCompletenessReport("Error - QV instance " + qvInstance.toString() + " is integer value and does not declare hasValueInteger.");
							throw new CompletenessException("QV instance " + qvInstance.toString() + " is integer value and does not declare hasValueInteger.");
						}
						
						try {
							checkCorrectIntegerDeclaration(qvInstance.toString(), "gr:hasValueInteger");
						} catch (BrokerPolicyException e) {
							e.printStackTrace();
							throw new CompletenessException(e.getMessage());
						}
						writeMessageToCompletenessReport("QV instance " + qvInstance.toString() + " is integer value and declares hasValueInteger " + hasValueInteger.toString() + ".");						
					}
					else
					{	// float value
						RDFNode hasValueFloat = oneVarOneSolutionQuery("{<" + qvInstance.toString() + "> <" + GR + "hasValueFloat> ?var}");
						if(hasValueFloat == null)
						{
							writeMessageToCompletenessReport("Error - QV instance " + qvInstance.toString() + " is float value and does not declare hasValueFloat.");
							throw new CompletenessException("QV instance " + qvInstance.toString() + " is float value and does not declare hasValueFloat.");
						}
						
						try {
							checkCorrectFloatDeclaration(qvInstance.toString(), "gr:hasValueFloat");
						} catch (BrokerPolicyException e) {
							e.printStackTrace();
							throw new CompletenessException(e.getMessage());
						}
						writeMessageToCompletenessReport("QV instance " + qvInstance.toString() + " is float value and declares hasValueFloat " + hasValueFloat.toString() + ".");												
					}
				}				
			}
		}
		
	}

	private void checkClassificationDimensionsInSD(String smi_uri) throws CompletenessException 
	{
		Integer countCDs = countQuery("{<" + smi_uri + "> <" + USDL_CORE_CB + "hasClassificationDimension> ?var}");
		if (countCDs == 0) {
			writeMessageToCompletenessReport("Error - Service Model instance has no classification dimensions declared.");
			throw new CompletenessException("Service Model instance has no classification dimensions declared.");
		}
		writeMessageToCompletenessReport("Service Model instance has " + countCDs + " classification dimension(s) declared.");
		
		RDFNode[] cdsNodes = oneVarManySolutionsQuery("{<" + smi_uri + "> <" + USDL_CORE_CB + "hasClassificationDimension> ?var}");
		for(int i=0;i<cdsNodes.length;i++)
		{
			// check CD exists
			Integer cdCount = countQuery("{<" + cdsNodes[i].toString() + "> rdf:type <" + USDL_CORE_CB + "ClassificationDimension>}");
			if(cdCount == 0)
			{
				writeMessageToCompletenessReport("Error - Classification dimension " + cdsNodes[i].toString() + " does not exist.");
				throw new CompletenessException("Classification dimension " + cdsNodes[i].toString() + " does not exist.");
			}
			writeMessageToCompletenessReport("Found classification dimension " + cdsNodes[i].toString() + ".");
		}
		int i=0;
	}

	// ClassInstancePair Input: the instance URI used as a starting point for
	// the various checks along with the class URI this instance belongs to, so
	// that the properties connecting this instance with other instances can be
	// found from the BP object
	// Output: A list of ClassInstancePair objects corresponding to the
	// instances which are associated with the instance URI in the input
	// ClassInstancePair
	private List<ClassInstancePair> stepCompletenessCheck(
			ClassInstancePair cip, int startClassIndex)
			throws CompletenessException {

		List<ClassInstancePair> nlPairList = new ArrayList<ClassInstancePair>();

		Map<String, BrokerPolicyClass> bpClassMap = null;
		switch (startClassIndex) {
		case 0:
			bpClassMap = bp.getServiceModelMap();
			break;
		case 1:
			bpClassMap = bp.getServiceLevelProfileMap();
			break;
		case 2:
			bpClassMap = bp.getServiceLevelMap();
			break;
		case 3:
			bpClassMap = bp.getServiceLevelExpressionMap();
			break;
		case 4:
			bpClassMap = bp.getExpressionVariableMap();
			break;
		}

		Map<String, Subproperty> propertyMap = null;
		Collection<Subproperty> propertyCollection = null;
		String classUri = null;
		String instanceUri = null;

		classUri = cip.getClassUri();
		instanceUri = cip.getInstanceUri();

		// get the relevant subproperties from the BP object
		propertyMap = bpClassMap.get(classUri).getPropertyMap();
		propertyCollection = propertyMap.values();

		Iterator<Subproperty> propertyIter = propertyCollection.iterator();
		Subproperty prop = null;
		// For each subproperty perform the checks
		while (propertyIter.hasNext()) {
			prop = propertyIter.next();

			int countNli = countQuery("{<" + instanceUri + "> <"
					+ prop.getUri() + "> ?var}");

			String nli_uri = null;

			// This "if" section is entered for all subclasses used as domains
			// to subproperties except for the ServiceModel subclass
			// An instance that belongs to a subclass (domain subclass) must be
			// connected to instances of the next subclasses in
			// the chain (range subclasses) via each and every subproperty
			// connecting the domain subclass with the range subclasses
			if (startClassIndex != 0) {
				if (countNli == 0) {
					writeMessageToCompletenessReport("Error - Instance:");
					writeMessageToCompletenessReport(instanceUri);
					writeMessageToCompletenessReport("is not connected to any instance via the appropriate property");
					writeMessageToCompletenessReport(prop.getUri());
					throw new CompletenessException("Instance: " + instanceUri + " is not connected to any instance via the appropriate property " + prop.getUri());
				}
			}
			if (countNli > 1) {
				writeMessageToCompletenessReport("Error - Instance: ");
				writeMessageToCompletenessReport(instanceUri);
				writeMessageToCompletenessReport("must be connected to only 1 instance via the appropriate property");
				writeMessageToCompletenessReport(prop.getUri());
				throw new CompletenessException("Instance: " + instanceUri + " must be connected to only 1 instance via the appropriate property" + prop.getUri());
			} else if (countNli == 1) {
				writeMessageToCompletenessReport("OK - Instance: ");
				writeMessageToCompletenessReport(instanceUri);
				writeMessageToCompletenessReport("is correctly connected to exactly 1 instance via the appropriate property");
				writeMessageToCompletenessReport(prop.getUri());

				RDFNode node = oneVarOneSolutionQuery("{<" + instanceUri
						+ "> <" + prop.getUri() + "> ?var}");
				nli_uri = node.toString(); // Next level instance

				// -------------------------------------------------------------
				// This is a workaround so that BP file is not needed as an
				// input to completenessCheck method
				// but the information pertaining to the QV instances which are
				// present in BP file is drawn from
				// the BrokerPolicy object
				int countNli2 = 0;
				/*if (startClassIndex == 4) {

					if (bp.getQuantitativeValueMap().get(prop.getRangeUri())
							.getInstanceMap().containsKey(nli_uri)) {
						countNli2 = 1;
					} else {
						countNli2 = countQuery("{<" + nli_uri + "> rdf:type <"
								+ prop.getRangeUri() + ">}");
					}
				} else {*/
					countNli2 = countQuery("{<" + nli_uri + "> rdf:type <"
							+ prop.getRangeUri() + ">}");
				//}
				// ----------------------------------------------------------------

				if (countNli2 == 0) {
					writeMessageToCompletenessReport("Error - Instance");
					writeMessageToCompletenessReport(nli_uri);
					writeMessageToCompletenessReport("does not belong to the correct type:");
					writeMessageToCompletenessReport(prop.getRangeUri());
					throw new CompletenessException("Instance " + nli_uri + " does not belong to the correct type: " + prop.getRangeUri());
				} else {
					writeMessageToCompletenessReport("OK - Found exactly 1  instance of the correct type:");
					writeMessageToCompletenessReport(prop.getRangeUri());
					writeMessageToCompletenessReport("which is correctly connected to the instance:");
					writeMessageToCompletenessReport(instanceUri);
					int countSmi3 = countQuery("{<" + nli_uri
							+ "> rdf:type ?someType}");
					if (countSmi3 > 1) {
						writeMessageToCompletenessReport("Error - The instance URI:");
						writeMessageToCompletenessReport(nli_uri);
						writeMessageToCompletenessReport("is falsely associated with other types");
						throw new CompletenessException("The instance URI: " + nli_uri + " is falsely associated with other types");
					} else {
						writeMessageToCompletenessReport("OK - Found exactly 1 instance of the correct type:");
						writeMessageToCompletenessReport(prop.getRangeUri());
						writeMessageToCompletenessReport("which is correctly connected to the instance:");
						writeMessageToCompletenessReport(instanceUri);
						writeMessageToCompletenessReport("and no other types are associated with this instance");
						writeMessageToCompletenessReport("");

						ClassInstancePair nlCip = new ClassInstancePair(
								prop.getRangeUri(), nli_uri);
						nlPairList.add(nlCip);
					}
				}
			}
		}// propertyIter while

		// This "if" section is entered for the ServiceModel subclass
		// The ServiceModel subclass instance is connected to instances of
		// ServiceLevelProfile subclasses
		// via at least one subproperty connecting the ServiceModel subclass
		// with the ServiceLevelProfile subclasses.
		if (startClassIndex == 0) {
			if (nlPairList.isEmpty()) {
				writeMessageToCompletenessReport("Error - Instance:");
				writeMessageToCompletenessReport(instanceUri);
				writeMessageToCompletenessReport("is not connected to any instance of the class");
				writeMessageToCompletenessReport(prop.getRangeUri());
				writeMessageToCompletenessReport("via property");
				writeMessageToCompletenessReport(prop.getUri());
				throw new CompletenessException("Instance: " + instanceUri + " is not connected to any instance of the class " + prop.getRangeUri() + " via property " + prop.getUri());
			}
		}
		return nlPairList;
	}

	// qvPairList is a list of QV class-instance pairs (see ClassInstancePair
	// class) and is the second argument to this method. The class URIs are
	// needed to find the corresponding instances from the BP while the instance
	// URIs correspond to the instances found in the file contents. qvPairList is returned
	// from the completenessCheck method

	private void complianceCheck(Object fileData,
			List<ClassInstancePair> qvPairList) throws ComplianceException,
			IOException {

		// Initial Creation
		//acquireMemoryForData(OntModelSpec.RDFS_MEM);

		// Add the file contents into the Jena model
		addDataToJenaModel(fileData);

		writeMessageToComplianceReport("################");
		writeMessageToComplianceReport("Compliance Check");
		writeMessageToComplianceReport("################");

		Iterator<ClassInstancePair> qvPairIter = qvPairList.iterator();

		String classUri = null;
		String instanceUri = null;
		// For each QV class-instance pair
		while (qvPairIter.hasNext()) {
			ClassInstancePair pair = qvPairIter.next();
			classUri = pair.getClassUri(); // QV subclass
			instanceUri = pair.getInstanceUri(); // QV instance
			ValueObject vObj = null; // this is the object that holds the values
										// of the QV instance

			// if instanceUri is inside bp's QuantitativeValue instances
			if (bp.getQuantitativeValueMap().containsKey(classUri) && bp.getQuantitativeValueMap().get(classUri).getInstanceMap()
					.containsKey(instanceUri)) {
				writeMessageToComplianceReport("OK - Instance URI:");
				writeMessageToComplianceReport(instanceUri);
				writeMessageToComplianceReport("is compliant since it is directly contained in broker policy's instances");
			}
			// else if instanceUri is inside bp's QualitativeValue instances
			else if (bp.getQualitativeValueMapWithInstances().containsKey(classUri) && bp.getQualitativeValueMapWithInstances().get(classUri).getInstanceMap()
					.containsKey(instanceUri)) {
				writeMessageToComplianceReport("OK - Instance URI:");
				writeMessageToComplianceReport(instanceUri);
				writeMessageToComplianceReport("is compliant since it is directly contained in broker policy's instances");
			}
			else {
				Collection<QuantitativeValueInstance> bpInstancesCollection = bp
						.getQuantitativeValueMap().get(classUri)
						.getInstanceMap().values();
				Iterator<QuantitativeValueInstance> bpInstancesIter = bpInstancesCollection
						.iterator();
				
				// Case of integer quantitative value
				if (bp.getQuantitativeValueIntegerMap().containsKey(classUri)) {
					int intValueCount = countQuery("{<" + instanceUri
							+ "> gr:hasValueInteger ?someValue}");
					if (intValueCount > 1) {
						writeMessageToComplianceReport("Error - For instance URI:");
						writeMessageToComplianceReport(instanceUri);
						writeMessageToComplianceReport("property gr:hasValueInteger must be associated with only one value");
						throw new ComplianceException("For instance URI: " + instanceUri + " property gr:hasValueInteger must be associated with only one value");
					} else if (intValueCount == 1) {
						RDFNode intValueNode = oneVarOneSolutionQuery("{<"
								+ instanceUri + "> gr:hasValueInteger ?var}");
						if (intValueNode.isResource()) {
							writeMessageToComplianceReport("Error - For instance URI:");
							writeMessageToComplianceReport(instanceUri);
							writeMessageToComplianceReport("property gr:hasValueInteger must be associated with a literal");
							throw new ComplianceException("For instance URI: " + instanceUri + " property gr:hasValueInteger must be associated with a literal");
						} else if (intValueNode.isLiteral()) {
							writeMessageToComplianceReport("OK - For instance URI:");
							writeMessageToComplianceReport(instanceUri);
							writeMessageToComplianceReport("property gr:hasValueInteger is correctly associated with a literal");
							if (!intValueNode.asLiteral().getDatatype()
									.equals(XSDDatatype.XSDinteger)) {
								writeMessageToComplianceReport("Error - For instance URI:");
								writeMessageToComplianceReport(instanceUri);
								writeMessageToComplianceReport("property gr:hasValueInteger must be associated with xsd:integer type");
								throw new ComplianceException("For instance URI: " + instanceUri + " property gr:hasValueInteger must be associated with xsd:integer type");
							} else {
								int intValue;
								try {
									intValue = intValueNode.asLiteral()
											.getInt();
								} catch (DatatypeFormatException e) {
									writeMessageToComplianceReport("Error - For instance URI:");
									writeMessageToComplianceReport(instanceUri);
									writeMessageToComplianceReport("property gr:hasValueInteger must be associated with xsd:integer value");
									throw new ComplianceException("For instance URI: " + instanceUri + " property gr:hasValueInteger must be associated with xsd:integer value");
								}
								writeMessageToComplianceReport("OK - For instance URI:");
								writeMessageToComplianceReport(instanceUri);
								writeMessageToComplianceReport("property gr:hasValueInteger is correctly associated with a xsd:integer value");
								vObj = new ValueObject(instanceUri, intValue);
							}
						}
					} else if (intValueCount == 0) {
						int minMaxIntValueCount = countQuery("{<" + instanceUri
								+ "> gr:hasMinValueInteger ?someMinValue; "
								+ "gr:hasMaxValueInteger ?someMaxValue}");
						if (minMaxIntValueCount > 1) {
							writeMessageToComplianceReport("Error - Instance URI:");
							writeMessageToComplianceReport(instanceUri);
							writeMessageToComplianceReport("is falsely associated with multiple ranges using properties gr:hasMinValueInteger, gr:hasMinValueInteger");
							throw new ComplianceException("Instance URI: " + instanceUri + " is falsely associated with multiple ranges using properties gr:hasMinValueInteger, gr:hasMinValueInteger");
						} else if (minMaxIntValueCount == 0) {
							writeMessageToComplianceReport("Error - Instance URI:");
							writeMessageToComplianceReport(instanceUri);
							writeMessageToComplianceReport("is not associated to a range using properties gr:hasMinValueInteger, gr:hasMinValueInteger or a value using property gr:hasValueInteger");
							throw new ComplianceException("Instance URI: " + instanceUri + " is not associated to a range using properties gr:hasMinValueInteger, gr:hasMinValueInteger or a value using property gr:hasValueInteger");
						} else if (minMaxIntValueCount == 1) {
							int intMinValue = 0;
							int intMaxValue = 0;
							// Min
							RDFNode intMinValueNode = oneVarOneSolutionQuery("{<"
									+ instanceUri
									+ "> gr:hasMinValueInteger ?var}");
							if (intMinValueNode.isResource()) {
								writeMessageToComplianceReport("Error - For instance URI:");
								writeMessageToComplianceReport(instanceUri);
								writeMessageToComplianceReport("property gr:hasMinValueInteger must be associated with a literal");
								throw new ComplianceException("For instance URI: " + instanceUri + " property gr:hasMinValueInteger must be associated with a literal");
							} else if (intMinValueNode.isLiteral()) {
								writeMessageToComplianceReport("OK - For instance URI:");
								writeMessageToComplianceReport(instanceUri);
								writeMessageToComplianceReport("property gr:hasMinValueInteger is correctly associated with a literal");
								if (!intMinValueNode.asLiteral().getDatatype()
										.equals(XSDDatatype.XSDinteger)) {
									writeMessageToComplianceReport("Error - For instance URI:");
									writeMessageToComplianceReport(instanceUri);
									writeMessageToComplianceReport("property gr:hasMinValueInteger must be associated with xsd:integer type");
									throw new ComplianceException("For instance URI: " + instanceUri + " property gr:hasMinValueInteger must be associated with xsd:integer type");
								} else {
									try {
										intMinValue = intMinValueNode
												.asLiteral().getInt();
									} catch (DatatypeFormatException e) {
										writeMessageToComplianceReport("Error - For instance URI:");
										writeMessageToComplianceReport(instanceUri);
										writeMessageToComplianceReport("property gr:hasMinValueInteger must be associated with xsd:integer value");
										throw new ComplianceException("For instance URI: " + instanceUri + " property gr:hasMinValueInteger must be associated with xsd:integer value");
									}
									writeMessageToComplianceReport("OK - For instance URI:");
									writeMessageToComplianceReport(instanceUri);
									writeMessageToComplianceReport("property gr:hasMinValueInteger is correctly associated with a xsd:integer value");
								}
							}

							// Max
							RDFNode intMaxValueNode = oneVarOneSolutionQuery("{<"
									+ instanceUri
									+ "> gr:hasMaxValueInteger ?var}");
							if (intMaxValueNode.isResource()) {
								writeMessageToComplianceReport("Error - For instance URI:");
								writeMessageToComplianceReport(instanceUri);
								writeMessageToComplianceReport("property gr:hasMaxValueInteger must be associated with a literal");
								throw new ComplianceException("For instance URI: " + instanceUri + " property gr:hasMaxValueInteger must be associated with a literal");
							} else if (intMaxValueNode.isLiteral()) {
								writeMessageToComplianceReport("OK - For instance URI:");
								writeMessageToComplianceReport(instanceUri);
								writeMessageToComplianceReport("property gr:hasMaxValueInteger is correctly associated with a literal");
								if (!intMaxValueNode.asLiteral().getDatatype()
										.equals(XSDDatatype.XSDinteger)) {
									writeMessageToComplianceReport("Error - For instance URI:");
									writeMessageToComplianceReport(instanceUri);
									writeMessageToComplianceReport("property gr:hasMaxValueInteger must be associated with xsd:integer type");
									throw new ComplianceException("For instance URI: " + instanceUri + " property gr:hasMaxValueInteger must be associated with xsd:integer type");
								} else {
									try {
										intMaxValue = intMaxValueNode
												.asLiteral().getInt();
									} catch (DatatypeFormatException e) {
										writeMessageToComplianceReport("Error - For instance URI:");
										writeMessageToComplianceReport(instanceUri);
										writeMessageToComplianceReport("property gr:hasMaxValueInteger must be associated with xsd:integer value");
										throw new ComplianceException("For instance URI: " + instanceUri + " property gr:hasMaxValueInteger must be associated with xsd:integer value");
									}
									writeMessageToComplianceReport("OK - For instance URI:");
									writeMessageToComplianceReport(instanceUri);
									writeMessageToComplianceReport("property gr:hasMaxValueInteger is correctly associated with a xsd:integer value");
								}
							}
							if (intMinValue >= intMaxValue) {
								writeMessageToComplianceReport("Error - For instance URI:");
								writeMessageToComplianceReport(instanceUri);
								writeMessageToComplianceReport("the value associated with property gr:hasMaxValueInteger must be greater than the value associated with property gr:hasMinValueInteger");
								throw new ComplianceException("For instance URI: " + instanceUri + " the value associated with property gr:hasMaxValueInteger must be greater than the value associated with property gr:hasMinValueInteger");
							} else {
								writeMessageToComplianceReport("OK - For instance URI:");
								writeMessageToComplianceReport(instanceUri);
								writeMessageToComplianceReport("the value associated with property gr:hasMaxValueInteger is greater than the value associated with property gr:hasMinValueInteger");
								vObj = new ValueObject(instanceUri,
										intMinValue, intMaxValue);
							}
						}
					}

					// Classification to ranges
					if (vObj != null) {
						// In this case the SP has provided a single value
						if (vObj.getValue() != null) {
							boolean flag = false; // this boolean variable will
													// be true if the value
													// provided is inside a bp
													// range
							QuantitativeValueInstance bpQvInstance = null;

							bpInstancesIter = bpInstancesCollection.iterator();

							// Iterate through all available ranges in bp to
							// check if the value provided is inside a
							// permissible range
							while (bpInstancesIter.hasNext()) {
								bpQvInstance = bpInstancesIter.next();
								if (((Integer) vObj.getValue() < (Integer) bpQvInstance
										.getMaxValue())
										&& ((Integer) vObj.getValue() >= (Integer) bpQvInstance
												.getMinValue())) {
									writeMessageToComplianceReport("OK - Instance URI:");
									writeMessageToComplianceReport(instanceUri);
									writeMessageToComplianceReport("is compliant to broker policy and classified as:");
									writeMessageToComplianceReport(bpQvInstance.getUri());
									flag = true;
									break;
								}
							}
							if (flag == false) {
								writeMessageToComplianceReport("Error - Instance URI:");
								writeMessageToComplianceReport(instanceUri);
								writeMessageToComplianceReport("is not compliant to any of the permissible value ranges offered by the broker policy");
								throw new ComplianceException("Instance URI: " + instanceUri + " is not compliant to any of the permissible value ranges offered by the broker policy");
							}
						} else {
							// In this case the SP has provided a value range
							boolean flag = false;// this boolean variable will
													// be true if the range
													// provided is inside a bp
													// range
							QuantitativeValueInstance bpQvInstance = null;

							// Iterate through all available ranges in bp to
							// check if the range provided is inside a
							// permissible range
							while (bpInstancesIter.hasNext()) {
								bpQvInstance = bpInstancesIter.next();
								if (((Integer) vObj.getMaxValue() < (Integer) bpQvInstance
										.getMaxValue())
										&& ((Integer) vObj.getMinValue() >= (Integer) bpQvInstance
												.getMinValue())) {
									writeMessageToComplianceReport("OK - Instance URI:");
									writeMessageToComplianceReport(instanceUri);
									writeMessageToComplianceReport("is compliant to broker policy and classified as:");
									writeMessageToComplianceReport(bpQvInstance.getUri());
									flag = true;
									break;
								}
							}
							if (flag == false) {
								writeMessageToComplianceReport("Error - Instance URI:");
								writeMessageToComplianceReport(instanceUri);
								writeMessageToComplianceReport("is not compliant to any of the permissible value ranges offered by the broker policy");
								throw new ComplianceException("Instance URI: " + instanceUri + " is not compliant to any of the permissible value ranges offered by the broker policy");
							}
						}
					}

					// Case of float quantitative value
				} else if (bp.getQuantitativeValueFloatMap().containsKey(
						classUri)) {
					int floatValueCount = countQuery("{<" + instanceUri
							+ "> gr:hasValueFloat ?someValue}");
					if (floatValueCount > 1) {
						writeMessageToComplianceReport("Error - For instance URI:");
						writeMessageToComplianceReport(instanceUri);
						writeMessageToComplianceReport("property gr:hasValueFloat must be associated with only one value");
						throw new ComplianceException("For instance URI: " + instanceUri + " property gr:hasValueFloat must be associated with only one value");
					} else if (floatValueCount == 1) {
						RDFNode floatValueNode = oneVarOneSolutionQuery("{<"
								+ instanceUri + "> gr:hasValueFloat ?var}");
						if (floatValueNode.isResource()) {
							writeMessageToComplianceReport("Error - For instance URI:");
							writeMessageToComplianceReport(instanceUri);
							writeMessageToComplianceReport("property gr:hasValueFloat must be associated with a literal");
							throw new ComplianceException("For instance URI: " + instanceUri + " property gr:hasValueFloat must be associated with a literal");
						} else if (floatValueNode.isLiteral()) {
							writeMessageToComplianceReport("OK - For instance URI:");
							writeMessageToComplianceReport(instanceUri);
							writeMessageToComplianceReport("property gr:hasValueFloat is correctly associated with a literal");
							if (!floatValueNode.asLiteral().getDatatype()
									.equals(XSDDatatype.XSDfloat)) {
								writeMessageToComplianceReport("Error - For instance URI:");
								writeMessageToComplianceReport(instanceUri);
								writeMessageToComplianceReport("property gr:hasValueFloat must be associated with xsd:float type");
								throw new ComplianceException("For instance URI: " + instanceUri + " property gr:hasValueFloat must be associated with xsd:float type");
							} else {
								float floatValue;
								try {
									floatValue = floatValueNode.asLiteral()
											.getFloat();
								} catch (DatatypeFormatException e) {
									writeMessageToComplianceReport("Error - For instance URI:");
									writeMessageToComplianceReport(instanceUri);
									writeMessageToComplianceReport("property gr:hasValueFloat must be associated with xsd:float value");
									throw new ComplianceException("For instance URI: " + instanceUri + " property gr:hasValueFloat must be associated with xsd:float value");
								}
								writeMessageToComplianceReport("OK - For instance URI:");
								writeMessageToComplianceReport(instanceUri);
								writeMessageToComplianceReport("property gr:hasValueFloat is correctly associated with a xsd:float value");
								vObj = new ValueObject(instanceUri, floatValue);
							}
						}
					} else if (floatValueCount == 0) {
						int minMaxFloatValueCount = countQuery("{<" + instanceUri
								+ "> gr:hasMinValueFloat ?someMinValue; "
								+ "gr:hasMaxValueFloat ?someMaxValue}");
						if (minMaxFloatValueCount > 1) {
							writeMessageToComplianceReport("Error - Instance URI:");
							writeMessageToComplianceReport(instanceUri);
							writeMessageToComplianceReport("is falsely associated with multiple ranges using properties gr:hasMinValueFloat, gr:hasMinValueFloat");
							throw new ComplianceException("Instance URI: " + instanceUri + " is falsely associated with multiple ranges using properties gr:hasMinValueFloat, gr:hasMinValueFloat");
						} else if (minMaxFloatValueCount == 0) {
							writeMessageToComplianceReport("Error - Instance URI:");
							writeMessageToComplianceReport(instanceUri);
							writeMessageToComplianceReport("is not associated to a range using properties gr:hasMinValueFloat, gr:hasMinValueFloat or a value using property gr:hasValueFloat");
							throw new ComplianceException("Instance URI: " + instanceUri + " is not associated to a range using properties gr:hasMinValueFloat, gr:hasMinValueFloat or a value using property gr:hasValueFloat");
						} else if (minMaxFloatValueCount == 1) {
							float floatMinValue = 0;
							float floatMaxValue = 0;
							// Min
							RDFNode floatMinValueNode = oneVarOneSolutionQuery("{<"
									+ instanceUri
									+ "> gr:hasMinValueFloat ?var}");
							if (floatMinValueNode.isResource()) {
								writeMessageToComplianceReport("Error - For instance URI:");
								writeMessageToComplianceReport(instanceUri);
								writeMessageToComplianceReport("property gr:hasMinValueFloat must be associated with a literal");
								throw new ComplianceException("For instance URI: " + instanceUri + " property gr:hasMinValueFloat must be associated with a literal");
							} else if (floatMinValueNode.isLiteral()) {
								writeMessageToComplianceReport("OK - For instance URI:");
								writeMessageToComplianceReport(instanceUri);
								writeMessageToComplianceReport("property gr:hasMinValueFloat is correctly associated with a literal");
								if (!floatMinValueNode.asLiteral().getDatatype()
										.equals(XSDDatatype.XSDfloat)) {
									writeMessageToComplianceReport("Error - For instance URI:");
									writeMessageToComplianceReport(instanceUri);
									writeMessageToComplianceReport("property gr:hasMinValueFloat must be associated with xsd:float type");
									throw new ComplianceException("For instance URI: " + instanceUri + " property gr:hasMinValueFloat must be associated with xsd:float type");
								} else {
									try {
										floatMinValue = floatMinValueNode
												.asLiteral().getFloat();
									} catch (DatatypeFormatException e) {
										writeMessageToComplianceReport("Error - For instance URI:");
										writeMessageToComplianceReport(instanceUri);
										writeMessageToComplianceReport("property gr:hasMinValueFloat must be associated with xsd:float value");
										throw new ComplianceException("For instance URI: " + instanceUri + " property gr:hasMinValueFloat must be associated with xsd:float value");
									}
									writeMessageToComplianceReport("OK - For instance URI:");
									writeMessageToComplianceReport(instanceUri);
									writeMessageToComplianceReport("property gr:hasMinValueFloat is correctly associated with a xsd:float value");
								}
							}

							// Max
							RDFNode floatMaxValueNode = oneVarOneSolutionQuery("{<"
									+ instanceUri
									+ "> gr:hasMaxValueFloat ?var}");
							if (floatMaxValueNode.isResource()) {
								writeMessageToComplianceReport("Error - For instance URI:");
								writeMessageToComplianceReport(instanceUri);
								writeMessageToComplianceReport("property gr:hasMaxValueFloat must be associated with a literal");
								throw new ComplianceException("For instance URI: " + instanceUri + " property gr:hasMaxValueFloat must be associated with a literal");
							} else if (floatMaxValueNode.isLiteral()) {
								writeMessageToComplianceReport("OK - For instance URI:");
								writeMessageToComplianceReport(instanceUri);
								writeMessageToComplianceReport("property gr:hasMaxValueFloat is correctly associated with a literal");
								if (!floatMaxValueNode.asLiteral().getDatatype()
										.equals(XSDDatatype.XSDfloat)) {
									writeMessageToComplianceReport("Error - For instance URI:");
									writeMessageToComplianceReport(instanceUri);
									writeMessageToComplianceReport("property gr:hasMaxValueFloat must be associated with xsd:float type");
									throw new ComplianceException("For instance URI: " + instanceUri + " property gr:hasMaxValueFloat must be associated with xsd:float type");
								} else {
									try {
										floatMaxValue = floatMaxValueNode
												.asLiteral().getFloat();
									} catch (DatatypeFormatException e) {
										writeMessageToComplianceReport("Error - For instance URI:");
										writeMessageToComplianceReport(instanceUri);
										writeMessageToComplianceReport("property gr:hasMaxValueFloat must be associated with xsd:float value");
										throw new ComplianceException("For instance URI: " + instanceUri + " property gr:hasMaxValueFloat must be associated with xsd:float value");
									}
									writeMessageToComplianceReport("OK - For instance URI:");
									writeMessageToComplianceReport(instanceUri);
									writeMessageToComplianceReport("property gr:hasMaxValueFloat is correctly associated with a xsd:float value");
								}
							}
							if (floatMinValue >= floatMaxValue) {
								writeMessageToComplianceReport("Error - For instance URI:");
								writeMessageToComplianceReport(instanceUri);
								writeMessageToComplianceReport("the value associated with property gr:hasMaxValueFloat must be greater than the value associated with property gr:hasMinValueFloat");
								throw new ComplianceException("For instance URI: " + instanceUri + " the value associated with property gr:hasMaxValueFloat must be greater than the value associated with property gr:hasMinValueFloat");
							} else {
								writeMessageToComplianceReport("OK - For instance URI:");
								writeMessageToComplianceReport(instanceUri);
								writeMessageToComplianceReport("the value associated with property gr:hasMaxValueFloat is greater than the value associated with property gr:hasMinValueFloat");
								vObj = new ValueObject(instanceUri,
										floatMinValue, floatMaxValue);
							}
						}
					}

					// Classification to ranges
					if (vObj != null) {
						// In this case the SP has provided a single value
						if (vObj.getValue() != null) {
							boolean flag = false; // this boolean variable will
													// be true if the value
													// provided is inside a bp
													// range
							QuantitativeValueInstance bpQvInstance = null;

							bpInstancesIter = bpInstancesCollection.iterator();

							// Iterate through all available ranges in bp to
							// check if the value provided is inside a
							// permissible range
							while (bpInstancesIter.hasNext()) {
								bpQvInstance = bpInstancesIter.next();
								if (((Float) vObj.getValue() < (Float) bpQvInstance
										.getMaxValue())
										&& ((Float) vObj.getValue() >= (Float) bpQvInstance
												.getMinValue())) {
									writeMessageToComplianceReport("OK - Instance URI:");
									writeMessageToComplianceReport(instanceUri);
									writeMessageToComplianceReport("is compliant to broker policy and classified as:");
									writeMessageToComplianceReport(bpQvInstance.getUri());
									flag = true;
									break;
								}
							}
							if (flag == false) {
								writeMessageToComplianceReport("Error - Instance URI:");
								writeMessageToComplianceReport(instanceUri);
								writeMessageToComplianceReport("is not compliant to any of the permissible value ranges offered by the broker policy");
								throw new ComplianceException("Instance URI: " + instanceUri + " is not compliant to any of the permissible value ranges offered by the broker policy");
							}
						} else {
							// In this case the SP has provided a value range
							boolean flag = false;// this boolean variable will
													// be true if the range
													// provided is inside a bp
													// range
							QuantitativeValueInstance bpQvInstance = null;

							// Iterate through all available ranges in bp to
							// check if the range provided is inside a
							// permissible range
							while (bpInstancesIter.hasNext()) {
								bpQvInstance = bpInstancesIter.next();
								if (((Float) vObj.getMaxValue() < (Float) bpQvInstance
										.getMaxValue())
										&& ((Float) vObj.getMinValue() >= (Float) bpQvInstance
												.getMinValue())) {
									writeMessageToComplianceReport("OK - Instance URI:");
									writeMessageToComplianceReport(instanceUri);
									writeMessageToComplianceReport("is compliant to broker policy and classified as:");
									writeMessageToComplianceReport(bpQvInstance.getUri());
									flag = true;
									break;
								}
							}
							if (flag == false) {
								writeMessageToComplianceReport("Error - Instance URI:");
								writeMessageToComplianceReport(instanceUri);
								writeMessageToComplianceReport("is not compliant to any of the permissible value ranges offered by the broker policy");
								throw new ComplianceException("Instance URI: " + instanceUri + " is not compliant to any of the permissible value ranges offered by the broker policy");
							}
						}
					}
				}
				
				// UnitOfMeasurement start

				// -----------------------------------------------------
				// Get the corresponding unit of measurement value from bp
				bpInstancesIter = bpInstancesCollection
						.iterator();
				String bpUomValue = bpInstancesIter.next()
						.getUnitOfMeasurement();
				// ------------------------------------------------------

				// the fact that bpUomValue is not null implies that a unit
				// of measurement
				// value should have been provided by the SP so proceed with
				// checks
				if (bpUomValue != null) {
					int uomValueCount = countQuery("{<" + instanceUri
							+ "> gr:hasUnitOfMeasurement ?someValue}");
					if (uomValueCount > 1) {
						writeMessageToComplianceReport("Error - Instance URI:");
						writeMessageToComplianceReport(instanceUri);
						writeMessageToComplianceReport("has multiple values associated with gr:hasUnitOfMeasurement property");
						throw new ComplianceException("Instance URI: " + instanceUri + " has multiple values associated with gr:hasUnitOfMeasurement property");
					} else if (uomValueCount == 0) {
						writeMessageToComplianceReport("Error - Instance URI:");
						writeMessageToComplianceReport(instanceUri);
						writeMessageToComplianceReport("must have a value associated with gr:hasUnitOfMeasurement property");
						throw new ComplianceException("Instance URI: " + instanceUri + " must have a value associated with gr:hasUnitOfMeasurement property");
					} else if (uomValueCount == 1) {
						RDFNode uomValueNode = oneVarOneSolutionQuery("{<"
								+ instanceUri
								+ "> gr:hasUnitOfMeasurement ?var}");
						if (uomValueNode.isResource()) {
							writeMessageToComplianceReport("Error - For instance URI:");
							writeMessageToComplianceReport(instanceUri);
							writeMessageToComplianceReport("property gr:hasUnitOfMeasurement must be associated with a literal");
							throw new ComplianceException("For instance URI: " + instanceUri + " property gr:hasUnitOfMeasurement must be associated with a literal");
						} else if (uomValueNode.isLiteral()) {
							writeMessageToComplianceReport("OK - For instance URI:");
							writeMessageToComplianceReport(instanceUri);
							writeMessageToComplianceReport("property gr:hasUnitOfMeasurement is correctly associated with a literal");
							if (!uomValueNode.asLiteral().getDatatype()
									.equals(XSDDatatype.XSDstring)) {
								writeMessageToComplianceReport("Error - For instance URI:");
								writeMessageToComplianceReport(instanceUri);
								writeMessageToComplianceReport("property gr:hasUnitOfMeasurement must be associated with xsd:string type");
								throw new ComplianceException("For instance URI: " + instanceUri + " property gr:hasUnitOfMeasurement must be associated with xsd:string type");
							} else {
								String uomValue = null;
								try {
									uomValue = uomValueNode.asLiteral()
											.getString();
								} catch (DatatypeFormatException e) {
									writeMessageToComplianceReport("Error - For instance URI:");
									writeMessageToComplianceReport(instanceUri);
									writeMessageToComplianceReport("property gr:hasUnitOfMeasurement must be associated with xsd:string value");
									throw new ComplianceException("For instance URI: " + instanceUri + " property gr:hasUnitOfMeasurement must be associated with xsd:string value");
								}
								writeMessageToComplianceReport("OK - For instance URI:");
								writeMessageToComplianceReport(instanceUri);
								writeMessageToComplianceReport("property gr:hasUnitOfMeasurement is correctly associated with a xsd:string value");

								if (!uomValue.equals(bpUomValue)) {
									writeMessageToComplianceReport("Error - For instance URI:");
									writeMessageToComplianceReport(instanceUri);
									writeMessageToComplianceReport("property gr:hasUnitOfMeasurement must be associated with the xsd:string value defined in the broker policy: "
													+ bpUomValue);
									throw new ComplianceException("For instance URI: " + instanceUri + " property gr:hasUnitOfMeasurement must be associated with the xsd:string value defined in the broker policy: " + bpUomValue);
								}
								writeMessageToComplianceReport("OK - For instance URI:");
								writeMessageToComplianceReport(instanceUri);
								writeMessageToComplianceReport("property gr:hasUnitOfMeasurement is correctly associated with the xsd:string value defined in the broker policy");
								vObj.setUnitOfMeasurement(uomValue);

							}
						}
					}
				}
				// UnitOfMeasurement end
			}
			writeMessageToComplianceReport("-------------------");
		}
	}

	private Integer countQuery(String subQuery) {
		QueryExecution qexec = returnQueryExecObject("SELECT (COUNT(*) as ?count) WHERE "
				+ subQuery);
		Integer num = null;
		try {
			ResultSet countResultSet = qexec.execSelect();

			while (countResultSet.hasNext()) {
				QuerySolution soln = countResultSet.nextSolution();
				RDFNode countNode = soln.get("?count");

				num = countNode.asLiteral().getInt();
			}
		} finally {
			qexec.close();
		}

		return num;
	}

	private RDFNode oneVarOneSolutionQuery(String subQuery) {
		QueryExecution qexec = returnQueryExecObject("SELECT ?var WHERE "
				+ subQuery);
		RDFNode node = null;
		try {
			ResultSet rs = qexec.execSelect();

			while (rs.hasNext()) {
				QuerySolution soln = rs.nextSolution();
				node = soln.get("?var");
			}
		} finally {
			qexec.close();
		}
		return node;
	}

	private RDFNode[] oneVarManySolutionsQuery(String subQuery) {
		QueryExecution qexec = returnQueryExecObject("SELECT ?var WHERE "
				+ subQuery);
		List<RDFNode> node = new ArrayList<RDFNode>();
		try {
			ResultSet rs = qexec.execSelect();

			while (rs.hasNext()) {
				QuerySolution soln = rs.nextSolution();
				node.add(soln.get("?var"));
			}
		} finally {
			qexec.close();
		}
		return node.toArray(new RDFNode[0]);
	}

	private QueryExecution returnQueryExecObject(String coreQuery) {
		StringBuffer queryStr = new StringBuffer();
		// Establish Prefixes

		queryStr.append("PREFIX owl: <http://www.w3.org/2002/07/owl#>");
		queryStr.append("PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>");
		queryStr.append("PREFIX xml: <http://www.w3.org/XML/1998/namespace>");
		queryStr.append("PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>");
		queryStr.append("PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>");
		queryStr.append("PREFIX usdl-core: <http://www.linked-usdl.org/ns/usdl-core#>");
		queryStr.append("PREFIX usdl-sla: <http://www.linked-usdl.org/ns/usdl-sla#>");
		queryStr.append("PREFIX usdl-core-cb: <http://www.linked-usdl.org/ns/usdl-core/cloud-broker#>");
		//queryStr.append("PREFIX brokerpolicy: <http://www.broker-cloud.eu/d043567/linked-usdl-ontologies/SAP-HANA-Cloud-Apps-Broker/2014/01/brokerpolicy#>");
		//queryStr.append("PREFIX cas: <http://www.broker-cloud.eu/service-descriptions/CAS/broker#>");
		queryStr.append("PREFIX gr: <http://purl.org/goodrelations/v1#>");
		queryStr.append("PREFIX fc: <http://www.broker-cloud.eu/service-descriptions/CAS/categories#>");

		queryStr.append(coreQuery);

		Query query = QueryFactory.create(queryStr.toString());
		QueryExecution qexec = QueryExecutionFactory.create(query, modelMem);

		return qexec;
	}
	
	// Write a String message to a TeeOutputStream
	private void writeMessageToTee(TeeOutputStream teeos, String message)
	{
		try {
			teeos.write((message + "\n").getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	// Write a String message to the Broker Policy report TeeOutputStream
	private void writeMessageToBrokerPolicyReport(String message)
	{
		this.writeMessageToTee(brokerPolicyReportTos, message);
	}
	
	// Write a String message to the Completeness report TeeOutputStream
	private void writeMessageToCompletenessReport(String message)
	{
		this.writeMessageToTee(completenessReportTos, message);
	}
	
	// Write a String message to the Compliance report TeeOutputStream
	private void writeMessageToComplianceReport(String message)
	{
		this.writeMessageToTee(complianceReportTos, message);
	}
	
	
	public String getSDServiceInstanceURI(Object sdFileData) throws IOException, CompletenessException
	{
		// Initial Creation
		acquireMemoryForData(OntModelSpec.RDFS_MEM);

		// Add the SD into the Jena model
		addDataToJenaModel(sdFileData);

		try 
		{
			String si_uri = null; // service instance uri
	
			RDFNode node = oneVarOneSolutionQuery("{?var rdf:type usdl-core:Service}");
			si_uri = node.toString();
			
			return si_uri;
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new CompletenessException("Problem getting service instance URI. Enclosed message: " + e.getMessage());
		}
	}

	/*
	 * Returns the Service Model class URI from a SD
	 */
	public String getSDServiceModelURI(Object sdFileData) throws IOException, CompletenessException {
		// Initial Creation
		acquireMemoryForData(OntModelSpec.RDFS_MEM);

		// Add the SD into the Jena model
		addDataToJenaModel(sdFileData);

		try
		{
			String si_uri = oneVarOneSolutionQuery("{?var rdf:type usdl-core:Service}").toString(); // Service instance URI
			String smi_uri = oneVarOneSolutionQuery("{<" + si_uri + "> usdl-core:hasServiceModel ?var}").toString(); // Service model instance URI
			String smc_uri = oneVarOneSolutionQuery("{<" + smi_uri + "> a ?var}").toString(); // Service model class URI
			
			return smc_uri;
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new CompletenessException("Problem getting service model URI. Enclosed message: " + e.getMessage());
		}
	}

	/*
	 * Returns the Service Model class URI from a BP
	 */
	public String getBPServiceModelURI(Object bpFileData) throws IOException, BrokerPolicyException 
	{
		// Initial Creation
		acquireMemoryForData(OntModelSpec.RDFS_MEM);

		// Add the BP into the Jena model
		addDataToJenaModel(bpFileData);

		String smc_uri;
		
		try
		{
			smc_uri = oneVarOneSolutionQuery("{?var rdfs:subClassOf usdl-core:ServiceModel}").toString(); // Service model class URI
		}
		catch (Exception e) 
		{
			e.printStackTrace();
			throw new BrokerPolicyException("Could not find the Service Model Class of the Broker Policy.");
		}
		
		return smc_uri;
	}

	/*
	 * Returns the MakeAndModel that this SD refers to, that is the BP URI it should be checked against
	 */
	public String getSDMakeAndModelURI(InputStream sdis) throws IOException, CompletenessException 
	{
		// Initial Creation
		acquireMemoryForData(OntModelSpec.RDFS_MEM);

		// Add the SD into the Jena model
		addDataToJenaModel(sdis);

		try
		{
			// the Service Individual instance 
			RDFNode siInstance = oneVarOneSolutionQuery("{?var a usdl-core:ServiceIndividual}");
			// BP URI
			String bpUri = oneVarOneSolutionQuery("{<" + siInstance.toString() + "> gr:hasMakeAndModel ?var}").toString();
			
			return bpUri;
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new CompletenessException("Problem getting make and model URI. Enclosed message: " + e.getMessage());
		}

	}

	/*
	 * Returns the URI of a BP instance
	 */
	public String getBPInstanceUri(InputStream bpFileData) throws IOException, BrokerPolicyException {
		String bpServiceModelClassUri = this.getBPServiceModelURI(bpFileData);

		// Data is already added to Jena Model from getBPServiceModelURI call

		String bpi_uri;
		
		try
		{
			bpi_uri = oneVarOneSolutionQuery("{?var a <" + bpServiceModelClassUri + ">}").toString(); // BP instance URI
		}
		catch (Exception e) 
		{
			e.printStackTrace();
			throw new BrokerPolicyException("Could not find the URI of the Broker Policy instance.");
		}
		
		return bpi_uri;
	}

	/*
	 * Returns the isVariantOf of an SD
	 */
	public String getSDIsVariantOfURI(InputStream sdis) throws IOException, CompletenessException {
		// Initial Creation
		acquireMemoryForData(OntModelSpec.RDFS_MEM);
	
		// Add the SD into the Jena model
		addDataToJenaModel(sdis);
	
		try {
			String si_uri = oneVarOneSolutionQuery("{?var rdf:type usdl-core:Service}").toString(); // Service instance URI
			String smi_uri = oneVarOneSolutionQuery("{<" + si_uri + "> usdl-core-cb:hasServiceModel ?var}").toString(); // Service model instance URI
			String isVariantOfUri = oneVarOneSolutionQuery("{<" + smi_uri + "> gr:isVariantOf ?var}").toString(); // Service model instance URI
			
			return isVariantOfUri;
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new CompletenessException("Problem getting gr:isVariantOf for service model instance. Enclosed message: " + e.getMessage());
		}
	}

	public BrokerPolicy getBP() 
	{
		return this.bp;
	}

	public boolean qvIsInRange(String qvToCheck, Float qvValue)
	{
		Float minValueRange;
		Float maxValueRange;
		
		if(this.bp.getQuantitativeValueIntegerMap().containsKey(qvToCheck))
		{	// it's integer
			String minInteger = oneVarOneSolutionQuery("{<" + qvToCheck + "> gr:hasMinValueInteger ?var}").toString();
			String maxInteger = oneVarOneSolutionQuery("{<" + qvToCheck + "> gr:hasMaxValueInteger ?var}").toString();
			minValueRange = Float.parseFloat(minInteger);
			maxValueRange = Float.parseFloat(maxInteger);
		}
		else
		{	// it's float
			String minFloat = oneVarOneSolutionQuery("{<" + qvToCheck + "> gr:hasMinValueFloat ?var}").toString();
			String maxFloat = oneVarOneSolutionQuery("{<" + qvToCheck + "> gr:hasMaxValueFloat ?var}").toString();
			minValueRange = Float.parseFloat(minFloat.substring(0, minFloat.indexOf("^^")));
			maxValueRange = Float.parseFloat(maxFloat.substring(0, maxFloat.indexOf("^^")));
		}
		
		if(qvValue.floatValue() >= minValueRange && qvValue.floatValue() <= maxValueRange)
		{	// OK it's in range
			return true;
		}
		else
		{	// out of range
			return false;
		}
	}
}
