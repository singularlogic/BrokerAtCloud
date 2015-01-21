package org.broker.orbi.util.entities;

import java.io.Serializable;
import java.util.Map;

/**
 *
 * @author smantzouratos
 */
public class ZabbixHost implements Serializable {
    
    private String id;
    private String type;
    private Map<String, ZabbixItem> zabbixItems;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Map<String, ZabbixItem> getZabbixItems() {
        return zabbixItems;
    }

    public void setZabbixItems(Map<String, ZabbixItem> zabbixItems) {
        this.zabbixItems = zabbixItems;
    }

    public ZabbixHost() {
    }

    public ZabbixHost(String id, String type, Map<String, ZabbixItem> zabbixItems) {
        this.id = id;
        this.type = type;
        this.zabbixItems = zabbixItems;
    }

    public ZabbixHost(String id, String type) {
        this.id = id;
        this.type = type;
    }
    
}
