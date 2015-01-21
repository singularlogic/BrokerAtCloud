package org.broker.orbi.util.entities;

import java.io.Serializable;
import java.util.Map;

/**
 *
 * @author smantzouratos
 */
public class ZabbixItem implements Serializable {
    
    private String itemID;
    private String itemName;
    private String type; // str , int, float
    private String date;
    private String value;
    private String category;
    private String measurementType;
    private String zabbixKey;
    private String zabbixName;
    private int zabbixDelay;
    private int zabbixHistory;
    private int zabbixTrends;
    private int zabbixValueType;
    private int zabbixTemplateID;
    private String zabbixCategory;
    private Map<String, ZabbixHistory> mapOfZabbixHistory;

    public String getMeasurementType() {
        return measurementType;
    }

    public void setMeasurementType(String measurementType) {
        this.measurementType = measurementType;
    }

    public String getItemID() {
        return itemID;
    }

    public void setItemID(String itemID) {
        this.itemID = itemID;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getZabbixKey() {
        return zabbixKey;
    }

    public void setZabbixKey(String zabbixKey) {
        this.zabbixKey = zabbixKey;
    }

    public String getZabbixName() {
        return zabbixName;
    }

    public void setZabbixName(String zabbixName) {
        this.zabbixName = zabbixName;
    }

    public int getZabbixDelay() {
        return zabbixDelay;
    }

    public void setZabbixDelay(int zabbixDelay) {
        this.zabbixDelay = zabbixDelay;
    }

    public int getZabbixHistory() {
        return zabbixHistory;
    }

    public void setZabbixHistory(int zabbixHistory) {
        this.zabbixHistory = zabbixHistory;
    }

    public int getZabbixTrends() {
        return zabbixTrends;
    }

    public void setZabbixTrends(int zabbixTrends) {
        this.zabbixTrends = zabbixTrends;
    }

    public int getZabbixValueType() {
        return zabbixValueType;
    }

    public void setZabbixValueType(int zabbixValueType) {
        this.zabbixValueType = zabbixValueType;
    }

    public int getZabbixTemplateID() {
        return zabbixTemplateID;
    }

    public void setZabbixTemplateID(int zabbixTemplateID) {
        this.zabbixTemplateID = zabbixTemplateID;
    }

    public Map<String, ZabbixHistory> getMapOfZabbixHistory() {
        return mapOfZabbixHistory;
    }

    public void setMapOfZabbixHistory(Map<String, ZabbixHistory> mapOfZabbixHistory) {
        this.mapOfZabbixHistory = mapOfZabbixHistory;
    }

    public String getZabbixCategory() {
        return zabbixCategory;
    }

    public void setZabbixCategory(String zabbixCategory) {
        this.zabbixCategory = zabbixCategory;
    }    

    public ZabbixItem() {
    }

    public ZabbixItem(String itemID, String itemName, String category, String type, String measurementType) {
        this.itemID = itemID;
        this.itemName = itemName;
        this.type = type;
        this.category = category;
        this.measurementType = measurementType;
    }
    
}
