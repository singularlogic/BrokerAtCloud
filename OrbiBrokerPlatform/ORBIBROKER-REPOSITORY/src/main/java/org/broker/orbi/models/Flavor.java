package org.broker.orbi.models;

/**
 *
 * @author Christos Paraskeva <ch.paraskeva at gmail dot com>
 */
public class Flavor {

    int id;
    int uid;
    String name;
    String parameters;
    String date_edited;

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public Flavor(int id, int uid, String name, String parameters, String date_edited) {
        this.id = id;
        this.uid = uid;
        this.name = name;
        this.parameters = parameters;
        this.date_edited = date_edited;
    }

    public Flavor(int id, String name, String parameters, String date_edited) {
        this.id = id;
        this.name = name;
        this.parameters = parameters;
        this.date_edited = date_edited;
    }

    public String getParameters() {
        return parameters;
    }

    public void setParameters(String parameters) {
        this.parameters = parameters;
    }

    public String getDate_edited() {
        return date_edited;
    }

    public void setDate_edited(String date_edited) {
        this.date_edited = date_edited;
    }

    public Flavor(int id, String name) {
        this.id = id;
        this.name = name;
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

}
