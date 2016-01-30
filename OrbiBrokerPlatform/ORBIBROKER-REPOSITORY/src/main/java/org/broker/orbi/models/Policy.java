package org.broker.orbi.models;

/**
 *
 * @author Christos Paraskeva <ch.paraskeva at gmail dot com>
 */
public class Policy {

    int id;
    String name;
    String date_created;
    String date_edited;
    String content;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
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

    public Policy(int id, String name, String date_created, String date_edited) {
        this.id = id;
        this.name = name;
        this.date_created = date_created;
        this.date_edited = date_edited;
    }

    public Policy(int id, String name, String date_created, String date_edited, String content) {
        this(id, name, date_created, date_edited);
        this.content = content;
    }

}
