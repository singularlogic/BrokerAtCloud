package org.broker.orbi.util.entities;

import java.util.Map;

/**
 *
 * @author smantzouratos
 */
public class Host {

    private String hostID;
    private int userID;
    private int purchaseID;
    private int offerID;
    private String hostName;
    private String hostIP;
    private Map<String, ZabbixItem> zabbixItems;

    public String getHostID() {
        return hostID;
    }

    public void setHostID(String hostID) {
        this.hostID = hostID;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getHostIP() {
        return hostIP;
    }

    public void setHostIP(String hostIP) {
        this.hostIP = hostIP;
    }

    public Map<String, ZabbixItem> getZabbixItems() {
        return zabbixItems;
    }

    public void setZabbixItems(Map<String, ZabbixItem> zabbixItems) {
        this.zabbixItems = zabbixItems;
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public int getPurchaseID() {
        return purchaseID;
    }

    public void setPurchaseID(int purchaseID) {
        this.purchaseID = purchaseID;
    }

    public int getOfferID() {
        return offerID;
    }

    public void setOfferID(int offerID) {
        this.offerID = offerID;
    }

    public Host() {
    }

}
