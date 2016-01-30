/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.broker.orbi.models;

import java.util.List;

/**
 *
 * @author ermis
 */
public class Purchase {

    private int purchaseID;
    private int spofferID;
    private int policyID;
    private String policyName;
    private int profileID;
    private String profileName;
    private String name;
    private String dateCreated;
    private String datePurchased;
    private String providerName;
    private List<VariableType> variables;
    private String publicIP;
    private String cpuUtil;
    private String ramUtil;
    private String vmStatus;

    public String getVmStatus() {
        return vmStatus;
    }

    public void setVmStatus() {
        float mem = this.getRamUtil().equals("N/A") ? 0 : Float.parseFloat(this.getRamUtil());
        float cpu = this.getCpuUtil().equals("N/A") ? 0 : Float.parseFloat(this.getCpuUtil());
        this.vmStatus = (mem > 80 && cpu > 80 ? "Imminent failure" : "OK");
    }

    public String getCpuUtil() {
        return cpuUtil;
    }

    public void setCpuUtil(String cpuUtil) {
        this.cpuUtil = cpuUtil;
    }

    public String getRamUtil() {
        return ramUtil;
    }

    public void setRamUtil(String ramUtil) {
        this.ramUtil = ramUtil;
    }

    public String getPublicIP() {
        return publicIP;
    }

    public void setPublicIP(String publicIP) {
        this.publicIP = publicIP;
    }

    public int getPurchaseID() {
        return purchaseID;
    }

    public void setPurchaseID(int purchaseID) {
        this.purchaseID = purchaseID;
    }

    public int getSpofferID() {
        return spofferID;
    }

    public void setSpofferID(int spofferID) {
        this.spofferID = spofferID;
    }

    public int getPolicyID() {
        return policyID;
    }

    public void setPolicyID(int policyID) {
        this.policyID = policyID;
    }

    public int getProfileID() {
        return profileID;
    }

    public void setProfileID(int profileID) {
        this.profileID = profileID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(String dateCreated) {
        this.dateCreated = dateCreated;
    }

    public String getDatePurchased() {
        return datePurchased;
    }

    public void setDatePurchased(String datePurchased) {
        this.datePurchased = datePurchased;
    }

    public String getProviderName() {
        return providerName;
    }

    public void setProviderName(String providerName) {
        this.providerName = providerName;
    }

    public String getPolicyName() {
        return policyName;
    }

    public void setPolicyName(String policyName) {
        this.policyName = policyName;
    }

    public String getProfileName() {
        return profileName;
    }

    public void setProfileName(String profileName) {
        this.profileName = profileName;
    }

    public List<VariableType> getVariables() {
        return variables;
    }

    public void setVariables(List<VariableType> variables) {
        this.variables = variables;
    }

    public Purchase(int purchaseID, int spofferID, int policyID, String policyName, int profileID, String profileName, String name, String dateCreated, String datePurchased, String providerName, List<VariableType> variables, String publicIP) {
        this.purchaseID = purchaseID;
        this.spofferID = spofferID;
        this.policyID = policyID;
        this.policyName = policyName;
        this.profileID = profileID;
        this.profileName = profileName;
        this.name = name;
        this.dateCreated = dateCreated;
        this.datePurchased = datePurchased;
        this.providerName = providerName;
        this.variables = variables;
        this.publicIP = publicIP;
    }

}
