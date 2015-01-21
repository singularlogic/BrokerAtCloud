package org.broker.orbi.util.entities;

import java.io.Serializable;

/**
 *
 * @author smantzouratos
 */
public class ZabbixHistory implements Serializable {
    
    private int hostID;
    private int itemID;
    private String timestamp;
    private String value;

    public int getHostID() {
        return hostID;
    }

    public void setHostID(int hostID) {
        this.hostID = hostID;
    }

    public int getItemID() {
        return itemID;
    }

    public void setItemID(int itemID) {
        this.itemID = itemID;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public ZabbixHistory() {
    }

}
