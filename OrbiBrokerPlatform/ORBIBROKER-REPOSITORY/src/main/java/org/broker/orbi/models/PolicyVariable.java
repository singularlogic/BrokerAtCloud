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
public class PolicyVariable {

    int id;
    String name;
    String type_id;
    String type_name;
    String date_created;
    String metric_unit;

    public String getMetric_unit() {
        return metric_unit;
    }

    public void setMetric_unit(String metric_unit) {
        this.metric_unit = metric_unit;
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

    public String getType_id() {
        return type_id;
    }

    public void setType_id(String type_id) {
        this.type_id = type_id;
    }

    public String getType_name() {
        return type_name;
    }

    public void setType_name(String type_name) {
        this.type_name = type_name;
    }

    public String getDate_created() {
        return date_created;
    }

    public void setDate_created(String date_created) {
        this.date_created = date_created;
    }

    public PolicyVariable(int id, String name, String type_id, String type_name, String date_created, String metric_unit) {
        this.id = id;
        this.name = name;
        this.type_id = type_id;
        this.type_name = type_name;
        this.date_created = date_created;
        this.metric_unit = metric_unit;
    }

}
