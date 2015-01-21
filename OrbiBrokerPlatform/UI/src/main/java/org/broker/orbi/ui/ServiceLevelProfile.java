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
public class ServiceLevelProfile {

    int id;
    String name;
    String date_created;
    int policy_id;
    String policy_name;

    public String getPolicy_name() {
        return policy_name;
    }

    public void setPolicy_name(String policy_name) {
        this.policy_name = policy_name;
    }

    public int getPolicy_id() {
        return policy_id;
    }

    public void setPolicy_id(int policy_id) {
        this.policy_id = policy_id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

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

    public ServiceLevelProfile(int id, int policy_id, String name, String date_created,String policy_name) {
        this.id = id;
        this.name = name;
        this.date_created = date_created;
        this.policy_id = policy_id;
        this.policy_name = policy_name;
    }

}
