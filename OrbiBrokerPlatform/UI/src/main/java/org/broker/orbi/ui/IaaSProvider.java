/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.broker.orbi.ui;

/**
 *
 * @author ermis
 */
public class IaaSProvider {

    int id = 0;
    int provider_id = 0;
    String end_point = "";
    String tenant_name = "";
    String username = "";
    String password = "";

    String prevention_mechanism_endpoint="";

    public String getPrevention_mechanism_endpoint() {
        return prevention_mechanism_endpoint;
    }

    public void setPrevention_mechanism_endpoint(String prevention_mechanism_endpoint) {
        this.prevention_mechanism_endpoint = prevention_mechanism_endpoint;
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public IaaSProvider() {

    }


    public IaaSProvider(int id, int provider_id, String end_point, String tenant_name, String username, String password,String prevention_mechanism_endpoint) {


        this.id = id;
        this.provider_id = provider_id;
        this.end_point = end_point;
        this.tenant_name = tenant_name;
        this.username = username;
        this.password = password;
        this.prevention_mechanism_endpoint = prevention_mechanism_endpoint;

    }

    public int getProvider_id() {
        return provider_id;
    }

    public void setProvider_id(int provider_id) {
        this.provider_id = provider_id;
    }

    public String getEnd_point() {
        return end_point;
    }

    public void setEnd_point(String end_point) {
        this.end_point = end_point;
    }

    public String getTenant_name() {
        return tenant_name;
    }

    public void setTenant_name(String tenant_name) {
        this.tenant_name = tenant_name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
   

}
