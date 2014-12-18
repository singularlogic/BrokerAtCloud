package org.seerc.brokeratcloud.policycompletenesscompliance;

import java.util.Map;

// Object of this class holds the BP in an object structure using maps
public class BrokerPolicy {

	//key=URI value=Subclass object
	private Map<String, BrokerPolicyClass> serviceModelMap; // there is always 1 k-v pair here
	private Map<String, BrokerPolicyClass> serviceLevelProfileMap;
	private Map<String, BrokerPolicyClass> serviceLevelMap;
	private Map<String, BrokerPolicyClass> serviceLevelExpressionMap;
	private Map<String, BrokerPolicyClass> expressionVariableMap;
	private Map<String, BrokerPolicyClass> quantitativeValueIntegerMap;
	private Map<String, BrokerPolicyClass> quantitativeValueFloatMap;
	private Map<String, QuantitativeValue> quantitativeValueMap;
	private Map<String, BrokerPolicyClass> qualitativeValueMap;
	private Map<String, QualitativeValue> qualitativeValueMapWithInstances;
	
	public Map<String, BrokerPolicyClass> getServiceModelMap() {
		return serviceModelMap;
	}
	public void setServiceModelMap(
			Map<String, BrokerPolicyClass> serviceModelMap) {
		this.serviceModelMap = serviceModelMap;
	}
	public Map<String, BrokerPolicyClass> getServiceLevelProfileMap() {
		return serviceLevelProfileMap;
	}
	public void setServiceLevelProfileMap(
			Map<String, BrokerPolicyClass> serviceLevelProfileMap) {
		this.serviceLevelProfileMap = serviceLevelProfileMap;
	}
	public Map<String, BrokerPolicyClass> getServiceLevelMap() {
		return serviceLevelMap;
	}
	public void setServiceLevelMap(
			Map<String, BrokerPolicyClass> serviceLevelMap) {
		this.serviceLevelMap = serviceLevelMap;
	}
	public Map<String, BrokerPolicyClass> getServiceLevelExpressionMap() {
		return serviceLevelExpressionMap;
	}
	public void setServiceLevelExpressionMap(
			Map<String, BrokerPolicyClass> serviceLevelExpressionMap) {
		this.serviceLevelExpressionMap = serviceLevelExpressionMap;
	}
	public Map<String, BrokerPolicyClass> getExpressionVariableMap() {
		return expressionVariableMap;
	}
	public void setExpressionVariableMap(
			Map<String, BrokerPolicyClass> expressionVariableMap) {
		this.expressionVariableMap = expressionVariableMap;
	}
	public Map<String, BrokerPolicyClass> getQuantitativeValueIntegerMap() {
		return quantitativeValueIntegerMap;
	}
	public void setQuantitativeValueIntegerMap(
			Map<String, BrokerPolicyClass> quantitativeValueIntegerMap) {
		this.quantitativeValueIntegerMap = quantitativeValueIntegerMap;
	}
	public Map<String, BrokerPolicyClass> getQuantitativeValueFloatMap() {
		return quantitativeValueFloatMap;
	}
	public void setQuantitativeValueFloatMap(
			Map<String, BrokerPolicyClass> quantitativeValueFloatMap) {
		this.quantitativeValueFloatMap = quantitativeValueFloatMap;
	}
	public Map<String, QuantitativeValue> getQuantitativeValueMap() {
		return quantitativeValueMap;
	}
	public void setQuantitativeValueMap(
			Map<String, QuantitativeValue> quantitativeValueMap) {
		this.quantitativeValueMap = quantitativeValueMap;
	}
	public Map<String, BrokerPolicyClass> getQualitativeValueMap() {
		return qualitativeValueMap;
	}
	public void setQualitativeValueMap(
			Map<String, BrokerPolicyClass> qualitativeValueMap) {
		this.qualitativeValueMap = qualitativeValueMap;
	}
	public Map<String, QualitativeValue> getQualitativeValueMapWithInstances() {
		return qualitativeValueMapWithInstances;
	}
	public void setQualitativeValueMapWithInstances(
			Map<String, QualitativeValue> qualitativeValueMapWithInstances) {
		this.qualitativeValueMapWithInstances = qualitativeValueMapWithInstances;
	}
	
}
