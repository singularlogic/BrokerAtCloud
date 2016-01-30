package org.broker.orbi.models;

/**
 *
 * @author Christos Paraskeva <ch.paraskeva at gmail dot com>
 */
public class Application {

    int id;
    String hex_id;
    String name;
    String date_edited;
    String metadata;
    String thumbnail;

    public Application(int id, String hex_id, String name, String date_edited, String metadata, String thumbnail) {
        this.id = id;
        this.hex_id = hex_id;
        this.name = name;
        this.date_edited = date_edited;
        this.metadata=metadata;
        this.thumbnail=thumbnail;
    }

    public Application(int id, String name, String hex_id) {
        this.id = id;
        this.name = name;
        this.hex_id = hex_id;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public String getDate_edited() {
        return date_edited;
    }

    public void setDate_edited(String date_edited) {
        this.date_edited = date_edited;
    }

    public String getHex_id() {
        return hex_id;
    }

    public void setHex_id(String hex_id) {
        this.hex_id = hex_id;
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
