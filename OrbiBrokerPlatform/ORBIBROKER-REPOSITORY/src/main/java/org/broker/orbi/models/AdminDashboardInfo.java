package org.broker.orbi.models;

/**
 *
 * @author Christos Paraskeva <ch.paraskeva at gmail dot com>
 */
public class AdminDashboardInfo {

    String numOfPolicies="0";
    String numOfProviders="0";
    String numOfClients="0";

    
    public AdminDashboardInfo(){
        
    }
    
    public AdminDashboardInfo(String numOfPolicies, String numOfProviders, String numOfClients) {
        this.numOfPolicies = numOfPolicies;
        this.numOfProviders = numOfProviders;
        this.numOfClients = numOfClients;
    }

    public String getNumOfPolicies() {
        return numOfPolicies;
    }

    public void setNumOfPolicies(String numOfPolicies) {
        this.numOfPolicies = numOfPolicies;
    }

    public String getNumOfProviders() {
        return numOfProviders;
    }

    public void setNumOfProviders(String numOfProviders) {
        this.numOfProviders = numOfProviders;
    }

    public String getNumOfClients() {
        return numOfClients;
    }

    public void setNumOfClients(String numOfClients) {
        this.numOfClients = numOfClients;
    }

}
