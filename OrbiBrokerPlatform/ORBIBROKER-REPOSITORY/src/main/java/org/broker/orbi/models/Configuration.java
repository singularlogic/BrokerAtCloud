package org.broker.orbi.models;

/**
 *
 * @author Christos Paraskeva <ch.paraskeva at gmail dot com>
 */
public class Configuration {

    int id;
    String name;
    String value;
    String friendly_name;
    String date_edited;

    public Configuration(int id, String name, String value, String friendly_name, String date_edited) {
        this.id = id;
        this.name = name;
        this.value = value;
        this.friendly_name = friendly_name;
        this.date_edited = date_edited;

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getFriendly_name() {
        return friendly_name;
    }

    public void setFriendly_name(String friendly_name) {
        this.friendly_name = friendly_name;
    }

}
