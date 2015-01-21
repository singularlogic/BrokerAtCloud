package org.broker.orbi.ui;

/**
 *
 * @author ermis
 */
public class ImageTemplate {

    int id;
    String hex_id;
    String name;

    public ImageTemplate(int id, String name, String hex_id) {
        this.id = id;
        this.name = name;
        this.hex_id = hex_id;
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
