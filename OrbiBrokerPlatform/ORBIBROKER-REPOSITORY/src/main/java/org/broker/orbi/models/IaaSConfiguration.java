/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.broker.orbi.models;

/**
 *
 * @author ermis
 */
public class IaaSConfiguration {

    int id = 0;
    int provider_id = 0;
    String end_point = "";
    String tenant_name = "";
    String username = "";
    String password = "";
    String date_edited;
    String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDate_edited() {
        return date_edited;
    }

    public void setDate_edited(String date_edited) {
        this.date_edited = date_edited;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public IaaSConfiguration(int id, int provider_id, String name, String end_point, String tenant_name, String username, String password, String date_edited) {
        this.id = id;
        this.provider_id = provider_id;
        this.end_point = end_point;
        this.tenant_name = tenant_name;
        this.username = username;
        this.password = password;
        this.date_edited = date_edited;
        this.name = name;

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
