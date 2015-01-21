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
public class Policy {
    
    int id;
    String name;
    String date_created;
    String date_edited;
    int variables;
    int profiles;
    

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDate_created() {
        return date_created;
    }

    public void setDate_created(String date_created) {
        this.date_created = date_created;
    }

    public String getDate_edited() {
        return date_edited;
    }

    public void setDate_edited(String date_edited) {
        this.date_edited = date_edited;
    }

    public int getVariables() {
        return variables;
    }

    public void setVariables(int variables) {
        this.variables = variables;
    }

    public int getProfiles() {
        return profiles;
    }

    public void setProfiles(int profiles) {
        this.profiles = profiles;
    }
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Policy(int id, String name, String date_created, String date_edited, int variables, int profiles) {
        this.id = id;
        this.name = name;
        this.date_created = date_created;
        this.date_edited = date_edited;
        this.variables = variables;
        this.profiles = profiles;
    }
    
    
    
        public Policy(int id, String name, String date_created, String date_edited) {
        this.id = id;
        this.name = name;
        this.date_created = date_created;
        this.date_edited = date_edited;
        this.variables = variables;
        this.profiles = profiles;
    }
    
    
}
