/**
 * Copyright 2014 SINTEF <brice.morin@sintef.no>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.brokeratcloud.fpr.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceImpl;

import diva.BoolVariableValue;
import diva.BooleanVariable;
import diva.ConfigVariant;
import diva.Configuration;
import diva.Context;
import diva.ContextExpression;
import diva.Dimension;
import diva.DivaFactory;
import diva.EnumLiteral;
import diva.EnumVariable;
import diva.EnumVariableValue;
import diva.Priority;
import diva.PriorityRule;
import diva.Property;
import diva.PropertyPriority;
import diva.PropertyValue;
import diva.Scenario;
import diva.SimulationModel;
import diva.Term;
import diva.VariabilityModel;
import diva.Variable;
import diva.Variant;
import diva.VariantExpression;
import diva.helpers.DivaHelper;
import diva.parser.DivaExpressionParser;
import eu.brokeratcloud.fpr.Main;
import eu.brokeratcloud.fpr.input.abstracts.AdaptRule;
import eu.brokeratcloud.fpr.input.abstracts.ConsumerProfile;
import eu.brokeratcloud.fpr.input.abstracts.ServiceAttribute;
import eu.brokeratcloud.fpr.input.abstracts.ServiceCategory;
import eu.brokeratcloud.fpr.input.abstracts.ServiceDependency;
import eu.brokeratcloud.fpr.input.json.ConsumerProfileJson;
import eu.brokeratcloud.fpr.input.sparql.ServiceDependencySparql;
import eu.brokeratcloud.fpr.jms.Subscriber;


public class DivaRoot {

	private static final String NONE = "NoImpendingFailure";
	private static final String LOW = "ImpendingFailureLow";
	private static final String MEDIUM = "ImpendingFailureMedium";
	private static final String HIGH = "ImpendingFailureHigh";
	private static final String FAILED = "OccurredFailure";
	private static final String RECOVERED = "FailureRecovered";
	
	public static Set<String> environment = new HashSet<String>();
	

	private int getFailureNumValue(String s) {
		switch (s) {
		case NONE:
			return 0;
		case LOW:
			return 1;
		case MEDIUM:
			return 2;
		case HIGH:
			return 4;
		case FAILED:
			return 0x4000;
		case RECOVERED:
			return 0;
		}
		return -1;
	}

	private DivaFactory factory = DivaFactory.eINSTANCE;

	private String combinedId = null;
	private Date timeQueried = null;

	public Date getTimeQueried() {
		return timeQueried;
	}

	public void setTimeQueried(Date timeQueried) {
		this.timeQueried = timeQueried;
	}

	public String getCombinedId() {
		return combinedId;
	}

	public void setCombinedId(String combinedId) {
		this.combinedId = combinedId;
	}

	ConfigurationsPool configPool = null;
	protected VariabilityModel root = null;

	protected DivaRoot(VariabilityModel root) {
		this.root = root;
	}

	public DivaRoot(URI uri) {
		ResourceSet rs = new ResourceSetImpl();
		rs.getResourceFactoryRegistry().getExtensionToFactoryMap().put("diva", new XMIResourceFactoryImpl());
		Resource res = rs.createResource(uri);
		try {
			res.load(Collections.EMPTY_MAP);
			this.root = (VariabilityModel) res.getContents().get(0);
		} catch (Exception e) {
			this.root = DivaFactory.eINSTANCE.createVariabilityModel();
			SimulationModel simu = DivaFactory.eINSTANCE.createSimulationModel();
			Scenario scenario = DivaFactory.eINSTANCE.createScenario();
			Context ctx = DivaFactory.eINSTANCE.createContext();
			scenario.getContext().add(ctx);
			simu.getScenario().add(scenario);
			root.setSimulation(simu);
			res.getContents().add(this.root);
			try {
				res.save(Collections.EMPTY_MAP);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			// e.printStackTrace();
		}
	}

	public Collection<Scenario> getScenarios() {
		return root.getSimulation().getScenario();
	}

	public void runSimulation() {
		runSimulation(false);
	}

	public void runSimulation(Boolean isAdmin) {
		ServiceCategory serviceCategory = ServiceCategory.INSTANCE;
		Map<String, String> remember = new HashMap<String, String>();
		if (isAdmin) {

			for (Dimension dim : root.getDimension())
				for (Variant variant : dim.getVariant()) {
					String dep = null;
					try {
						dep = variant.getDependency().getText();
					} catch (NullPointerException e) {
						continue;
					}
					if (dep == null || dep.length() == 0)
						continue;

					List<String> atoms = ServiceDependency.INSTANCE.getDependency(variant.getId());
					List<String> strauss = new ArrayList<String>();
					for (String atom : atoms) {
						List<String> group = serviceCategory.getGroup(atom.trim());
						if (group != null && group.size() != 0) {
							String alternative = org.apache.commons.lang.StringUtils.join(group, " or ");
							strauss.add("(" + alternative + ")");

						}
					}
					String alternative = org.apache.commons.lang.StringUtils.join(strauss, " and ");
					remember.put(variant.getName(), dep);
					variant.getDependency().setText(alternative);
					try {
						Term term = DivaExpressionParser.parse(root, alternative.trim());
						variant.getDependency().setTerm(term);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
		}

		_runSimulation();

		if (isAdmin) {
			for (Dimension dim : root.getDimension())
				for (Variant variant : dim.getVariant()) {
					String original = remember.get(variant);
					if (original == null || original.length() == 0)
						continue;
					variant.getDependency().setText(original);
					try {
						Term term = DivaExpressionParser.parse(root, original.trim());
						variant.getDependency().setTerm(term);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
		}

	}

	public String fileNamePrefix = null;

	public void _runSimulation() {

		if (root.getSimulation() == null)
			return;
		root.getSimulation().populatePriorities();
		root.getSimulation().populateScores();
		root.getSimulation().populateVerdicts();
		DivaHelper.computeSuitableConfigurations(root, 0);
		// root.getSimulation().getScenario().get(0).getContext().get(0).
		setTimeQueried(Calendar.getInstance().getTime());
		// root.getSimulation().
		root.getSimulation().populateScores();
		root.getSimulation().populateVerdicts();
		configPool = new ConfigurationsPool(root.getSimulation().getScenario().get(0).getContext().get(0));

		this.saveModel(this.fileNamePrefix);
	}

	private void updateCategoryAndService() {
		List<String> cats = ServiceCategory.INSTANCE.getCategories();
		for (String cat : cats) {
			String catAvail = null;
			Dimension dim = factory.createDimension();
			dim.setId(cat);
			dim.setName(cat);
			dim.setLower(0);
			if ("FC".equals(cat))
				dim.setUpper(5);
			else {
				dim.setUpper(1);
			}
			// BooleanVariable bv = factory.createBooleanVariable();
			// catAvail = cat + "Avail";
			// bv.setId(catAvail);
			// bv.setName(catAvail);
			// root.getContext().add(bv);
			//
			root.getDimension().add(dim);

			for (Property p : root.getProperty()) {
				dim.getProperty().add(p);
			}

			for (String svc : ServiceCategory.INSTANCE.getServices(cat)) {
				Variant var = factory.createVariant();
				var.setId(svc);
				var.setName(svc);
				dim.getVariant().add(var);
				var.setType(dim);

				for (Property property : root.getProperty()) {
					PropertyValue value = factory.createPropertyValue();
					value.setProperty(property);
					var.getPropertyValue().add(value);
				}

				// if(! cat.equals("FC")){
				// var.setAvailable(factory.createContextExpression());
				// var.getAvailable().setText(catAvail);
				// try {
				// Term term = DivaExpressionParser.parse(root,
				// catAvail.trim());
				// var.getAvailable().setTerm(term);
				// } catch (Exception e) {
				// // TODO Auto-generated catch block
				// e.printStackTrace();
				// }
				// }

			}
		}
	}

	/* Before Category */
	private void updatePropertyDef() {

		for (String s : ServiceAttribute.INSTANCE.listCommonAttributes()) {
			Property p = factory.createProperty();
			// p.setDirection(1);
			p.setDirection(1);
			p.setName(s);
			p.setId(s);
			root.getProperty().add(p);
		}
		String s = "Failure";
		Property p = factory.createProperty();
		p.setDirection(0);
		p.setName(s);
		p.setId(s);
		root.getProperty().add(p);

		s = "Cost";
		p = factory.createProperty();
		p.setDirection(0);
		p.setName(s);
		p.setId(s);
		root.getProperty().add(p);
	}

	private void updateFixed() {

		AdaptRule rules = AdaptRule.INSTANCE;
		for (String ruleName : rules.allRuleNames()) {
			PriorityRule rule = factory.createPriorityRule();
			rule.setId(ruleName);
			rule.setName(ruleName);
			for (Property p : root.getProperty()) {
				Priority priority = factory.createPriority();
				priority.setProperty(p);
				priority.setPriority(rules.getPriority(ruleName, p.getId()));
			}
		}
	}

	private void updateAutoFullAvailability() {
		// for(Dimension dim : root.getDimension())
		// for(Variant vrt : dim.getVariant()){
		//
		// String name = vrt.getId();
		//
		// BooleanVariable variable = factory.createBooleanVariable();
		// variable.setName(name+"Available");
		// variable.setId(name+"Available");
		//
		// root.getContext().add(variable);
		//
		// if(vrt.getAvailable() == null){
		// vrt.setAvailable(factory.createContextExpression());
		// }
		// ContextExpression expr = vrt.getAvailable();
		// expr.setText(String.format("%sAvailable", name));
		// try{
		// Term term = DivaExpressionParser.parse(root, expr.getText().trim());
		// expr.setTerm(term);
		// }
		// catch(Exception e){
		// e.printStackTrace();
		// }
		//
		//
		// }

//		BooleanVariable bv = factory.createBooleanVariable();
//		bv.setName("cpuOverload");
//		bv.setId("cpuOverload");
//		root.getContext().add(bv);
//
//		bv = factory.createBooleanVariable();
//		bv.setName("memoryOverload");
//		bv.setId("memoryOverload");
//		root.getContext().add(bv);
//
//		bv = factory.createBooleanVariable();
//		bv.setName("Normal");
//		bv.setId("Normal");
//		root.getContext().add(bv);

	}

	/**
	 * Not used?
	 */
	private void updateAvailable() {
		for (Dimension dim : root.getDimension())
			for (Variant vrt : dim.getVariant()) {
				List<String> requiredIds = new ArrayList<String>();
				for (Variable var : root.getContext()) {
					Object val = ServiceAttribute.INSTANCE.get(vrt.getId(), var.getId());
					if (val == null || !(val instanceof Boolean) || !((Boolean) val).booleanValue())
						continue;
					requiredIds.add(var.getId());
				}
				String res = "";
				Iterator<String> it = requiredIds.iterator();
				if (it.hasNext()) {
					res = res + it.next();
					while (it.hasNext()) {
						res = res + " or " + it.next();
					}

					if (vrt.getAvailable() == null) {
						vrt.setAvailable(factory.createContextExpression());
					}
					ContextExpression expr = vrt.getAvailable();
					expr.setText(res);
					try {
						Term term = DivaExpressionParser.parse(root, expr.getText().trim());
						expr.setTerm(term);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

			}

	}

	private void updateProperty() {

		for (Dimension dim : root.getDimension()) {
			for (Variant var : dim.getVariant()) {
				for (PropertyValue pv : var.getPropertyValue()) {
					Object res = ServiceAttribute.INSTANCE.get(var.getId(), pv.getProperty().getId());
					if (res != null && res instanceof Integer)
						pv.setValue((Integer) res);

					if(pv.getProperty().getId().equals("Cost"))
						pv.setValue(4);
				}
			}
		}
	}

	private void updateProfileContext(String consumer, String profile) {
		Context context = root.getSimulation().getScenario().get(0).getContext().get(0);
		context.getVariable().clear();
		Map<String, Object> prf = (Map<String, Object>) ConsumerProfile.INSTANCE.getRequired(consumer, profile);

		if (prf != null) {
			for (Variable v : root.getContext()) {
				Object value = prf.get(v.getName());
				if (v instanceof EnumVariable) {
					EnumLiteral el = null;
					if (value == null) {
						String pubValue = ConsumerProfileJson.INSTANCE.publicStatus.get(v.getName());

						if (pubValue == null)
							el = ((EnumVariable) v).getLiteral().get(0);
						value = pubValue;

					}
					for (EnumLiteral literal : ((EnumVariable) v).getLiteral()) {
						if (literal.getName().equals(value))
							el = literal;
					}
					EnumVariableValue vv = factory.createEnumVariableValue();
					vv.setVariable(v);
					vv.setLiteral(el);
					context.getVariable().add(vv);
				} else {
					BoolVariableValue vv = factory.createBoolVariableValue();
					vv.setVariable(v);
					if (Boolean.valueOf(true).equals(value)) {
						vv.setBool(true);
					} else if ("true".equals(ConsumerProfileJson.INSTANCE.publicStatus.get(v.getName())))
						vv.setBool(true);
					else if ("false".equals(ConsumerProfileJson.INSTANCE.publicStatus.get(v.getName())))
						vv.setBool(false);
					else
						vv.setBool(false);
					context.getVariable().add(vv);
				}
			}

			for (Dimension d : root.getDimension()) {
				if (Boolean.valueOf(true).equals(prf.get(d.getName()))) {
					d.setLower(1);
				}
			}

			// this.generateRule();
		}
	}

	private void updateRequirement() {

		for (Dimension dim : root.getDimension()) {
			for (Variant var : dim.getVariant()) {
				ServiceDependency servDep = ServiceDependency.INSTANCE;
				List<String> req = servDep.getRequirement(var.getId());

				for (String r : req) {
					Variable found = null;
					for (Variable v : root.getContext()) {
						if (v.getId().equals(r)) {
							found = v;
							break;
						}
					}
					if (found == null) {
						Variable v = DivaFactory.eINSTANCE.createBooleanVariable();
						v.setId(r);
						v.setName(r);
						root.getContext().add(v);
					}
				}

				if (req == null || req.isEmpty())
					continue;
				if (var.getRequired() == null) {
					ContextExpression expr = factory.createContextExpression();
					var.setRequired(expr);
				}
				String operator = " and ";

				Iterator<String> it = req.iterator();
				String s = it.next();
				while (it.hasNext()) {
					String aDep = it.next();
					s = s + operator + aDep;
				}
				var.getRequired().setText(s);
				try {
					Term term = DivaExpressionParser.parse(root, s.trim());
					var.getRequired().setTerm(term);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	private void updateDependency() {
		for (Dimension dim : root.getDimension()) {
			for (Variant var : dim.getVariant()) {
				ServiceDependency servDep = ServiceDependency.INSTANCE;
				List<String> dep = servDep.getDependency(var.getId());
				if (dep == null || dep.isEmpty())
					continue;
				if (var.getDependency() == null) {
					VariantExpression expr = factory.createVariantExpression();
					var.setDependency(expr);
				}
				String operator = " and ";
				if (servDep.isAlternative(var.getId()))
					operator = " or ";

				Iterator<String> it = dep.iterator();
				String s = it.next();
				while (it.hasNext()) {
					String aDep = it.next();
					// Could allow user to define dependency on categories, but
					// not finished yet!
					// if(aDep.equals("CASCalenderApp"))
					// System.out.println("something");
					// for(Dimension d : this.root.getDimension()){
					// if(d.getId().equals(aDep)){
					// Iterator<Variant> itv = dim.getVariant().iterator();
					// String result = itv.next().getId();
					// while(itv.hasNext()){
					// result = result + " or " + itv.next().getId();
					// }
					// aDep ="(" + result + ")";
					// }
					// }
					s = s + operator + aDep;
				}
				var.getDependency().setText(s);
				try {
					Term term = DivaExpressionParser.parse(root, s.trim());
					var.getDependency().setTerm(term);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	public void updateModel() {

		root.getDimension().clear();
		root.getContext().clear();
		root.getRule().clear();
		root.getProperty().clear();
		updatePropertyDef();
		updateCategoryAndService();
		updateDependency();
		updateRequirement();
		updateProperty();
		updateAutoFullAvailability();
		updateFixed();
		generateRule();
	}

	public String updateFailureLikelihood(String service, String likelihood) {
		if ("cpuOverload".equals(service)) {
			if ("recovered".equals(likelihood.toLowerCase()))
				ConsumerProfileJson.INSTANCE.publicStatus.remove(service);
			else
				ConsumerProfileJson.INSTANCE.publicStatus.put(service, "true");
			return "CPU updated";
		}
		if ("memoryOverload".equals(service)) {
			if ("recovered".equals(likelihood.toLowerCase()))
				ConsumerProfileJson.INSTANCE.publicStatus.remove(service);
			else
				ConsumerProfileJson.INSTANCE.publicStatus.put(service, "true");
			return "Ram updated";
		}
		// if(FAILED.toLowerCase().equals(likelihood.toLowerCase()) ||
		// RECOVERED.toLowerCase().equals(likelihood.toLowerCase())){
		//// for(VariableValue v :
		// root.getSimulation().getScenario().get(0).getContext().get(0).getVariable()){
		//// if(v.getVariable().equals(service+"S")){
		//// for(EnumLiteral l : ((EnumVariable)v.getVariable()).getLiteral()){
		//// if(l.getName().equals(service+"F") || "Failed".equals(likelihood))
		//// ((EnumVariableValue)v).setLiteral(l);
		//// else if(l.getName().equals(service+"A") ||
		// "Recovered".equals(likelihood))
		//// ((EnumVariableValue)v).setLiteral(l);
		//// }
		//// }
		//// }
		// //TODO: Not done by updating DivaRoot...
		// if(FAILED.toLowerCase().equals(likelihood.toLowerCase()))
		// ConsumerProfileLocal.INSTANCE.publicStatus.put(service+"Available",
		// service+"false");
		// else
		// ConsumerProfileLocal.INSTANCE.publicStatus.remove(service+"Available");
		// return "updated";
		// }
		System.out.println(String.format("--------%s is set to %s", service, likelihood));
		int nlikelihood = this.getFailureNumValue(likelihood);
		if (nlikelihood < 0)
			return "Not a valid level name";
		Main.inUse.clear();
		Main.inUse.add(service);
		Subscriber.dirty = true;
		for (Dimension d : root.getDimension()) {
			for (Variant v : d.getVariant()) {
				if (v.getName().equals(service)) {

					for (PropertyValue p : v.getPropertyValue()) {
						if ("Failure".equals(p.getProperty().getName())) {
							p.setValue(nlikelihood);
							if (FAILED.equals(likelihood))
								return String.format("%s is set to be failed", v.getName());
							else if (RECOVERED.equals(likelihood))
								return String.format("%s is recovered from failure", v.getName());
							else
								return String.format("Failure likelihood of %s is changed to %d", v.getName(),
										nlikelihood);

						}

					}
				}
			}
		}
		return "No specified service found";
	}

	public void updateOnRequest(String consumer, String profile) {
		this.updateProfileContext(consumer, profile);
		// this.updateProperty();
	}

	public DivaRoot fork() {
		VariabilityModel model = EcoreUtil.copy(root);
		return new DivaRoot(model);
	}

	public ConfigurationsPool getConfigurationPool() {
		return configPool;
	}

	public List<String> getRecommQuery_old(String consumer, List<String> srvs) {
		List<String> result = new ArrayList<String>();
		Set<String> requirements = new HashSet<String>();
		ServiceDependencySparql dc = new ServiceDependencySparql();

		for (Dimension dim : root.getDimension()) {
			if (!"FC".equals(dim.getId())) {
				// boolean found = false;
				// dim.setUpper(1);
				// for(String s : srvs){
				// if(dim.getId().startsWith(s))
				// dim.setUpper(1);
				// }
				continue;
			}

			for (Variant v : dim.getVariant()) {
				String fc = v.getId();
				for (String dep : dc.getDependency(fc)) {
					for (String input : srvs) {
						if (input.equals(dep))
							requirements.addAll(dc.getRequirement(fc));
					}
				}
			}
		}



		Context ctx = root.getSimulation().getScenario().get(0).getContext().get(0);
		ctx.getVariable().clear();

		for (Variable var : root.getContext()) {
			if (var instanceof BooleanVariable) {
				BoolVariableValue varval = DivaFactory.eINSTANCE.createBoolVariableValue();
				ctx.getVariable().add(varval);
				varval.setVariable(var);
				String id = var.getId();
				if (id.startsWith("RFC")) {
					if (requirements.contains(var.getId()))
						varval.setBool(true);
					else
						varval.setBool(false);
				} else if (id.startsWith("For")) {
					if (var.getId().equals("ForCustomer"))
						varval.setBool(true);
					else
						varval.setBool(false);
				} else if (id.endsWith("Avail")) {
					varval.setBool(false);
					for (String s : srvs) {
						if (id.startsWith(s.substring(0, s.length() - 2))) {
							varval.setBool(true);
						}
					}
					for (String s : requirements) {
						if (id.startsWith(s.substring(1)))
							varval.setBool(true);
					}
				}
			}
		}

		
		List<Dimension> notToRemove = new ArrayList<Dimension>();

		for (Dimension dim : root.getDimension()) {
			boolean remitted = false;
			for (Variant v : dim.getVariant()) {
				for (String srv : srvs) {
					if (srv.equals(v.getId())) {
						dim.setLower(1);
						remitted = true;
						break;
					}
				}
			}
			if(remitted) 
				notToRemove.add(dim);
		}
		
		for(Dimension dim : root.getDimension()){
			for(Dimension dim2 : root.getDimension()){
				boolean remitted = false;
				for(Dimension dim3 : root.getDimension()){
					if(notToRemove.contains(dim3)){
						for(Variant v2 : dim3.getVariant()){
							 
							 try{
								 if(v2.getDependency().getText().contains(dim2.getId()))
										 remitted = true;
							 }
							 catch(Exception e){
								 
							 }
							if(remitted)
								break;
						}
					}
				}
				if(remitted)
					notToRemove.add(dim2);
			}
		}
		List<Dimension> toRemove = new ArrayList<Dimension>();
		for(Dimension dim : root.getDimension()){
			if(! notToRemove.contains(dim)){
				toRemove.add(dim);
			}
		}

		root.getDimension().removeAll(toRemove);
		saveModel(consumer + "-before");
		this.fileNamePrefix = consumer;
		_runSimulation();

		try {
			Configuration config = root.getSimulation().getScenario().get(0).getContext().get(0).getConfiguration()
					.get(0);
			for (ConfigVariant v : config.getVariant()) {
				result.add(v.getVariant().getId());
			}
		} catch (Exception e) {
			e.printStackTrace();
			result.add(e.getStackTrace().toString());
		}
		return result;
	}
	
	public List<String> getRecommQuery(String consumer, List<String> srvs) {
		
		/**--- By default, for Customer----*/
		if(environment.isEmpty())
			environment.add("ForCustomer");
		
		List<String> result = new ArrayList<String>();
		Set<String> requirements = new HashSet<String>();
		ServiceDependencySparql dc = new ServiceDependencySparql();

		

		Context ctx = root.getSimulation().getScenario().get(0).getContext().get(0);
		ctx.getVariable().clear();
		
		List<Dimension> notToRemove = new ArrayList<Dimension>();

		for (Dimension dim : root.getDimension()) {
			boolean remitted = false;
			for (Variant v : dim.getVariant()) {
				for (String srv : srvs) {
					if (srv.equals(v.getId())) {
						dim.setLower(1);
						remitted = true;
						break;
					}
				}
			}
			if(remitted) 
				notToRemove.add(dim);
		}
		
		for(Dimension dim : root.getDimension()){
			for(Dimension dim2 : root.getDimension()){
				boolean remitted = false;
				for(Dimension dim3 : root.getDimension()){
					if(notToRemove.contains(dim3)){
						for(Variant v2 : dim3.getVariant()){
							 
							 try{
								 if(v2.getDependency().getText().toLowerCase().contains(dim2.getId().substring(2)))
										 remitted = true;
							 }
							 catch(Exception e){
								 
							 }
							if(remitted)
								break;
						}
					}
				}
				if(remitted)
					notToRemove.add(dim2);
			}
		}
		List<Dimension> toRemove = new ArrayList<Dimension>();
		for(Dimension dim : root.getDimension()){
			if(! notToRemove.contains(dim)){
				toRemove.add(dim);
			}
		}
		root.getDimension().removeAll(toRemove);
		
//		List<Dimension> toRemove = new ArrayList<Dimension>();
//		for (Dimension dim : root.getDimension()) {
//			for (Variant v : dim.getVariant()) {
//				for (String srv : srvs) {
//					if (srv.equals(v.getId())) {
//						dim.setLower(1);
//						break;
//					}
//				}
//				
//			}
//			if(dim.getId().endsWith("todos")){
//				toRemove.add(dim);
//			}
//		}
//		root.getDimension().removeAll(toRemove);
		for (Variable var : root.getContext()) {
			if (var instanceof BooleanVariable) {
				BoolVariableValue varval = DivaFactory.eINSTANCE.createBoolVariableValue();
				
				String id = var.getId();

				
				ctx.getVariable().add(varval);
				varval.setVariable(var);
				if(environment.contains(id))
					varval.setBool(true);
				else
					varval.setBool(false);
			}
		}
		
		//saveModel(consumer + "-before");
		this.fileNamePrefix = consumer;
		_runSimulation();

		try {
			Configuration config = null;
			List<Configuration> configs = root.getSimulation().getScenario().get(0).getContext().get(0).getConfiguration();
			int max = -100000;
			for(Configuration c : configs){
				if(c.getTotalScore() > max){
					config = c;
					max = c.getTotalScore();
				}
			}
			for (ConfigVariant v : config.getVariant()) {
				result.add(v.getVariant().getId());
			}
		} catch (Exception e) {
			e.printStackTrace();
			result.add(e.getStackTrace().toString());
		}
		
		return result;
	}

	private static org.eclipse.emf.common.util.URI previousSaveUri = null;

	public void saveModel(org.eclipse.emf.common.util.URI uri) {
		if (previousSaveUri == null)
			previousSaveUri = uri;
		Resource res = new XMIResourceImpl(uri);
		res.getContents().add(root);
		try {
			res.save(Collections.EMPTY_MAP);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void saveModel(String prefix) {
		if (previousSaveUri == null) {
			throw new RuntimeException("saveModel() cannot be called for the first time - must have a parameter");
		}
		if (prefix == null) {
			saveModel(previousSaveUri);
		}
		if(previousSaveUri.toPlatformString(true)==null){
			String uriString = previousSaveUri.toFileString();
			uriString = uriString.substring(0, uriString.length() - 5) + "-" + prefix + ".diva";
			URI uri = URI.createFileURI(uriString);
			saveModel(uri);
			return;
		}
		String uriString = previousSaveUri.toPlatformString(true);
		uriString = uriString.substring(0, uriString.length() - 5) + "-" + prefix + ".diva";
		URI uri = URI.createURI(uriString);
		saveModel(uri);
	}

	public void generateRule() {

		Map<String, Integer> priorities = new HashMap<String, Integer>();
		AdaptRule adaptRule = AdaptRule.INSTANCE;

		for (String r : adaptRule.involvedContext()) {
			Variable found = null;
			for (Variable v : root.getContext()) {
				if (v.getId().equals(r)) {
					found = v;
					break;
				}
			}
			if (found == null) {
				Variable v = DivaFactory.eINSTANCE.createBooleanVariable();
				v.setId(r);
				v.setName(r);
				root.getContext().add(v);
			}
		}

		List<String> propertyNames = ServiceAttribute.INSTANCE.listCommonAttributes();
		for (String name : adaptRule.allRuleNames()) {
			priorities.clear();
			for (String propName : propertyNames) {
				priorities.put(propName, adaptRule.getPriority(name, propName));
			}
			priorities.put("Failure", 4);
			priorities.put("Cost", 8);
			this.fillRule(name, adaptRule.getRule(name), priorities);
		}

	}

	private void fillRule(String name, String text, Map<String, Integer> priorities) {
		PriorityRule rule = factory.createPriorityRule();
		root.getRule().add(rule);
		rule.setName(name);
		rule.setId(name);
		ContextExpression expr = factory.createContextExpression();
		expr.setText(text);
		rule.setContext(expr);
		try {
			expr.setTerm(DivaExpressionParser.parse(root, expr.getText()));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for (Property p : root.getProperty()) {
			PropertyPriority priority = factory.createPropertyPriority();
			priority.setProperty(p);
			Integer pri = priorities.get(p.getName());
			if (pri == null)
				priority.setPriority(0);
			else
				priority.setPriority(pri.intValue());
			rule.getPriority().add(priority);
		}
	}

}
