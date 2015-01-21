package org.broker.orbi.util.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.broker.orbi.util.database.DatabaseHandler;
import org.broker.orbi.util.entities.ZabbixHistory;
import org.broker.orbi.util.entities.ZabbixItem;

/**
 *
 * @author smantzouratos
 */
public class ZabbixUtil {

    public static final Double NANO_TO_MILLIS = 1000000.0;

    public static boolean deleteZabbixHost(int hostID) {
        Connection con = DatabaseHandler.INSTANCE.getDatasource("zabbix");
        PreparedStatement stm = null;
        try {

            stm = con.prepareStatement("DELETE FROM hosts WHERE hostid=?");

            stm.setInt(1, hostID);
            stm.executeUpdate();

        } catch (SQLException ex) {
            Logger.getLogger(ZabbixUtil.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        } finally {
            try {
                if (null != stm) {
                    stm.close();
                }
                if (null != con) {
                    con.close();
                }
            } catch (SQLException ex) {
                Logger.getLogger(ZabbixUtil.class.getName()).log(Level.SEVERE, null, ex);
                return false;
            }
        }

        return true;

    }

    /*
     * Inserts a new VM in Zabbix Database
     */
    public static int insertNEWZabbixHost(String hostname, String IP) {

        int newHostID = getNextID("hosts", "hostid");

        // Insert to hosts (hostname - > hostID)
        Connection con = DatabaseHandler.INSTANCE.getDatasource("zabbix");
        PreparedStatement stm = null;
        try {
            con.setAutoCommit(false);

            stm = con.prepareStatement("INSERT INTO  hosts (hostid, host, name) VALUES (?,?,?)");
            stm.setInt(1, newHostID);
            stm.setString(2, hostname);
            stm.setString(3, hostname);
            stm.executeUpdate();

            // Insert to interface (IP)
            int newInterfaceID = getNextID("interface", "interfaceid");

            stm = con.prepareStatement("INSERT INTO interface (interfaceid, hostid, main, type, useip, ip, dns, port) VALUES (?,?,?,?,?,?,?,?)");
            stm.setInt(1, newInterfaceID);
            stm.setInt(2, newHostID);
            stm.setInt(3, 1);
            stm.setInt(4, 1);
            stm.setInt(5, 1);
            stm.setString(6, IP);
            stm.setString(7, "");
            stm.setString(8, "10050");
            stm.executeUpdate();

            // Insert to HostGroup (hostID, groupID)
            int newHostGroupID = getNextID("hosts_groups", "hostgroupid");

            stm = con.prepareStatement("INSERT INTO hosts_groups (hostgroupid, hostid, groupid) VALUES (?,?,?);");
            stm.setInt(1, newHostGroupID);
            stm.setInt(2, newHostID);
            stm.setInt(3, 2);
            stm.executeUpdate();

            // Insert to HostTemplate (hostID, templateID) // 10001, 10117, 10118
            int newHostTemplateID = getNextID("hosts_templates", "hosttemplateid");

            stm = con.prepareStatement("INSERT INTO hosts_templates (hosttemplateid, hostid, templateid) VALUES (?,?,?);");
            stm.setInt(1, newHostTemplateID);
            stm.setInt(2, newHostID);
            stm.setInt(3, 10001);
            stm.executeUpdate();

            stm = con.prepareStatement("INSERT INTO hosts_templates (hosttemplateid, hostid, templateid) VALUES (?,?,?);");
            stm.setInt(1, newHostTemplateID + 1);
            stm.setInt(2, newHostID);
            stm.setInt(3, 10117);
            stm.executeUpdate();

            stm = con.prepareStatement("INSERT INTO hosts_templates (hosttemplateid, hostid, templateid) VALUES (?,?,?);");
            stm.setInt(1, newHostTemplateID + 2);
            stm.setInt(2, newHostID);
            stm.setInt(3, 10118);
            stm.executeUpdate();

            int finalHostTemplateID = newHostTemplateID + 2;

            // Retrieve Zabbix Items
            Map<String, ZabbixItem> mapOfZabbixItems = getZabbixItems();

            // For each Zabbix Item insert data to ZABBIX Database
            for (Map.Entry pairs : mapOfZabbixItems.entrySet()) {
                ZabbixItem tempZabbixItem = (ZabbixItem) pairs.getValue();

                int newItemID = getNextID("items", "itemid");

//                System.out.println("Key: " + newItemID + " , " + newHostID + ", " + newInterfaceID + ", " + tempZabbixItem.getZabbixKey());
                stm = con.prepareStatement("INSERT INTO items (itemid, hostid, name, key_, delay, history, trends, value_type, templateid, interfaceid, description, params) VALUES (?,?,?,?,?,?,?,?,?,?,?,?);");
                stm.setInt(1, newItemID);
                stm.setInt(2, newHostID);
                stm.setString(3, tempZabbixItem.getZabbixName());
                stm.setString(4, tempZabbixItem.getZabbixKey());
                stm.setInt(5, tempZabbixItem.getZabbixDelay());
                stm.setInt(6, tempZabbixItem.getZabbixHistory());
                stm.setInt(7, tempZabbixItem.getZabbixTrends());
                stm.setInt(8, tempZabbixItem.getZabbixValueType());

                stm.setString(9, null);

                stm.setInt(10, newInterfaceID);
                stm.setString(11, tempZabbixItem.getZabbixName());
                stm.setString(12, "");
                stm.executeUpdate();

                if (!updateNextID("items", "itemid", newItemID)) {
                    throw new SQLException("Could not update table ids.");
                }

            }

            if (updateNextID("hosts_templates", "hosttemplateid", finalHostTemplateID)) {
                if (updateNextID("hosts_groups", "hostgroupid", newHostGroupID)) {
                    if (updateNextID("interface", "interfaceid", newInterfaceID)) {
                        if (updateNextID("hosts", "hostid", newHostID)) {
                            con.commit();

                            System.out.println("New host added successfully! hostID: " + newHostID);

                            return newHostID;

                        } else {
                            throw new SQLException("Could not update table ids.");
                        }
                    } else {
                        throw new SQLException("Could not update table ids.");
                    }
                } else {
                    throw new SQLException("Could not update table ids.");
                }
            } else {
                throw new SQLException("Could not update table ids.");
            }

        } catch (SQLException ex) {
            Logger.getLogger(ZabbixUtil.class.getName()).log(Level.SEVERE, null, ex);
            return 0;
        } finally {
            try {
                if (null != stm) {
                    stm.close();
                }
                if (null != con) {
                    con.close();
                }
            } catch (SQLException ex) {
                Logger.getLogger(ZabbixUtil.class.getName()).log(Level.SEVERE, null, ex);
                return 0;
            }
        }
    }

    private static int getNextID(String tableName, String fieldName) {
        int nextID = 0;
        Connection con = DatabaseHandler.INSTANCE.getDatasource("zabbix");
        PreparedStatement stm = null;
        ResultSet rs = null;
        try {

            stm = con.prepareStatement("SELECT nextid FROM ids WHERE table_name = ? AND field_name = ?;");
            //Add Foreign key
            stm.setString(1, tableName);
            stm.setString(2, fieldName);
            rs = stm.executeQuery();
            if (rs.next()) {
                nextID = rs.getInt("nextid") + 1;
            }

        } catch (SQLException ex) {
            Logger.getLogger(ZabbixUtil.class.getName()).log(Level.SEVERE, null, ex);
            return 0;
        } finally {
            try {
                if (null != rs) {
                    rs.close();
                }
                if (null != stm) {
                    stm.close();
                }
                if (null != con) {
                    con.close();
                }
            } catch (SQLException ex) {
                Logger.getLogger(ZabbixUtil.class.getName()).log(Level.SEVERE, null, ex);
                return 0;
            }
        }

        return nextID;
    }

    public static int getHostID(int purchaseID) {
        int hostID = 0;
        Connection con = DatabaseHandler.INSTANCE.getDatasource("broker");
        PreparedStatement stm = null;
        ResultSet rs = null;
        try {

            stm = con.prepareStatement("SELECT zabbix_host_id FROM Purchase WHERE id = ? order by date_purchased desc limit 1;");
            //Add Foreign key
            stm.setInt(1, purchaseID);
            rs = stm.executeQuery();
            if (rs.next()) {
                hostID = rs.getInt("zabbix_host_id");
            }

        } catch (SQLException ex) {
            Logger.getLogger(ZabbixUtil.class.getName()).log(Level.SEVERE, null, ex);
            return 0;
        } finally {
            try {
                if (null != rs) {
                    rs.close();
                }
                if (null != stm) {
                    stm.close();
                }
                if (null != con) {
                    con.close();
                }
            } catch (SQLException ex) {
                Logger.getLogger(ZabbixUtil.class.getName()).log(Level.SEVERE, null, ex);
                return 0;
            }
        }

        return hostID;
    }

    public static int getOfferID(int purchaseID) {
        int offerID = 0;
        Connection con = DatabaseHandler.INSTANCE.getDatasource("broker");
        PreparedStatement stm = null;
        ResultSet rs = null;
        try {

            stm = con.prepareStatement("SELECT spoffer_id FROM Purchase WHERE id = ? order by date_purchased desc limit 1;");
            //Add Foreign key
            stm.setInt(1, purchaseID);
            rs = stm.executeQuery();
            if (rs.next()) {
                offerID = rs.getInt("spoffer_id");
            }

        } catch (SQLException ex) {
            Logger.getLogger(ZabbixUtil.class.getName()).log(Level.SEVERE, null, ex);
            return 0;
        } finally {
            try {
                if (null != rs) {
                    rs.close();
                }
                if (null != stm) {
                    stm.close();
                }
                if (null != con) {
                    con.close();
                }
            } catch (SQLException ex) {
                Logger.getLogger(ZabbixUtil.class.getName()).log(Level.SEVERE, null, ex);
                return 0;
            }
        }

        return offerID;
    }

    private static boolean updateNextID(String tableName, String fieldName, int nextID) {
        Connection con = DatabaseHandler.INSTANCE.getDatasource("zabbix");
        PreparedStatement stm = null;
        try {

            stm = con.prepareStatement("UPDATE ids SET nextid = ? WHERE table_name = ? AND field_name = ?;");
            //Add Foreign key
            stm.setInt(1, nextID);
            stm.setString(2, tableName);
            stm.setString(3, fieldName);
            stm.executeUpdate();

        } catch (SQLException ex) {
            Logger.getLogger(ZabbixUtil.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        } finally {
            try {
                if (null != stm) {
                    stm.close();
                }
                if (null != con) {
                    con.close();
                }
            } catch (SQLException ex) {
                Logger.getLogger(ZabbixUtil.class.getName()).log(Level.SEVERE, null, ex);
                return false;
            }
        }

        return true;
    }

    private static Map<String, ZabbixItem> getZabbixItems() {
        Map<String, ZabbixItem> mapOfZabbixItems = null;
        ZabbixItem zabbixItem = null;
        Connection con = DatabaseHandler.INSTANCE.getDatasource("broker");
        PreparedStatement stm = null;
        ResultSet rs = null;
        try {

            stm = con.prepareStatement("SELECT * FROM PolicyVariable WHERE has_zabbix= 1;");
            rs = stm.executeQuery();
            mapOfZabbixItems = new HashMap<>();

            while (rs.next()) {

                zabbixItem = new ZabbixItem();
                zabbixItem.setItemName(rs.getString("name"));
                zabbixItem.setZabbixName(rs.getString("metric_unit"));
                zabbixItem.setZabbixKey(rs.getString("zabbix_key"));
                zabbixItem.setZabbixDelay(rs.getInt("zabbix_delay"));
                zabbixItem.setZabbixHistory(rs.getInt("zabbix_history"));
                zabbixItem.setZabbixTrends(rs.getInt("zabbix_trends"));
                zabbixItem.setZabbixValueType(rs.getInt("zabbix_value_type"));
                zabbixItem.setZabbixTemplateID(rs.getInt("zabbix_templateid"));
                zabbixItem.setType(rs.getString("zabbix_type"));
                zabbixItem.setZabbixCategory(rs.getString("zabbix_category"));

                mapOfZabbixItems.put(zabbixItem.getZabbixKey(), zabbixItem);

            }

        } catch (SQLException ex) {
            Logger.getLogger(ZabbixUtil.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } finally {
            try {
                if (null != rs) {
                    rs.close();
                }
                if (null != stm) {
                    stm.close();
                }
                if (null != con) {
                    con.close();
                }
            } catch (SQLException ex) {
                Logger.getLogger(ZabbixUtil.class.getName()).log(Level.SEVERE, null, ex);
                return null;
            }
        }

        return mapOfZabbixItems;

    }

    public static Map<String, ZabbixItem> retrieveHistory(int hostID, int limit) {

        Map<String, ZabbixItem> mapOfZabbixItems = getItemsFromZabbix(hostID);

        // For each Zabbix Item retrieve data from ZABBIX Database
        for (Map.Entry pairs : mapOfZabbixItems.entrySet()) {
            ZabbixItem tempZabbixItem = (ZabbixItem) pairs.getValue();
            Map<String, ZabbixHistory> mapOfZabbixHistory = getItemHistory(Integer.valueOf(tempZabbixItem.getItemID()), tempZabbixItem.getType(), limit);

            tempZabbixItem.setMapOfZabbixHistory(mapOfZabbixHistory);
            mapOfZabbixItems.put(tempZabbixItem.getZabbixKey(), tempZabbixItem);
        }

        //
        return mapOfZabbixItems;
    }

    public static Map<String, ZabbixItem> getItemsFromZabbix(int hostID) {
        Map<String, ZabbixItem> mapOfZabbixItems = getZabbixItems();
        Connection con = DatabaseHandler.INSTANCE.getDatasource("zabbix");
        PreparedStatement stm = null;
        ResultSet rs = null;
        try {

            stm = con.prepareStatement("SELECT * FROM items WHERE hostid = ?;");
            stm.setInt(1, hostID);
            rs = stm.executeQuery();

            while (rs.next()) {
                if (mapOfZabbixItems.containsKey(rs.getString("key_"))) {
                    ZabbixItem zabbixItem = (ZabbixItem) mapOfZabbixItems.get(rs.getString("key_"));
                    zabbixItem.setItemID(String.valueOf(rs.getInt("itemid")));
                    mapOfZabbixItems.put(zabbixItem.getZabbixKey(), zabbixItem);

                }
            }

        } catch (SQLException ex) {
            Logger.getLogger(ZabbixUtil.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } finally {
            try {
                if (null != rs) {
                    rs.close();
                }
                if (null != stm) {
                    stm.close();
                }
                if (null != con) {
                    con.close();
                }
            } catch (SQLException ex) {
                Logger.getLogger(ZabbixUtil.class.getName()).log(Level.SEVERE, null, ex);
                return null;
            }
        }

        return mapOfZabbixItems;
    }

    public static Map<String, ZabbixHistory> getItemHistory(int itemID, String type, int limit) {
        Map<String, ZabbixHistory> mapOfZabbixHistory = null;
        ZabbixHistory zabbixHistory = null;
        Connection con = DatabaseHandler.INSTANCE.getDatasource("zabbix");
        PreparedStatement stm = null;
        ResultSet rs = null;
        try {
            String sql;
            if (type.equalsIgnoreCase("str")) {
                sql = "SELECT itemid, clock, value from history_str where itemid = ? order by clock desc limit " + limit + ";";
            } else if (type.equalsIgnoreCase("int")) {
                sql = "SELECT itemid, clock, value from history_uint where itemid = ? order by clock desc limit " + limit + ";";
            } else {
                sql = "SELECT itemid, clock, value from history where itemid = ? order by clock desc limit " + limit + ";";
            }

            stm = con.prepareStatement(sql);
            stm.setInt(1, itemID);
            rs = stm.executeQuery();
            mapOfZabbixHistory = new HashMap<>();

            while (rs.next()) {
                zabbixHistory = new ZabbixHistory();
                zabbixHistory.setItemID(itemID);
                zabbixHistory.setTimestamp(rs.getString("clock"));
                zabbixHistory.setValue(rs.getString("value"));
                mapOfZabbixHistory.put(rs.getString("clock"), zabbixHistory);
            }

        } catch (SQLException ex) {
            Logger.getLogger(ZabbixUtil.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } finally {
            try {
                if (null != rs) {
                    rs.close();
                }
                if (null != stm) {
                    stm.close();
                }
                if (null != con) {
                    con.close();
                }
            } catch (SQLException ex) {
                Logger.getLogger(ZabbixUtil.class.getName()).log(Level.SEVERE, null, ex);
                return null;
            }
        }

        return mapOfZabbixHistory;
    }

    public static Map<String, String> getAllPolicyProfileVariablesDependOnOffer(String offer_id) {
        Map<String, String> policyVariables = null;
        Connection con = DatabaseHandler.INSTANCE.getDatasource();
        PreparedStatement stm = null;
        ResultSet rs = null;
        try {
            //SELECT PV.zabbix_key FROM  `PolicyRelation` as PR,  `VariableProfile` as VP, `PolicyVariable` as PV, `VariableType` as VT, SPOffer as O WHERE PV.type_id=VT.id and ( VP.policy_id=O.policy_id  OR ( VP.profile_id= PR.slp_id and PR.policy_id=O.policy_id) OR VP.profile_id=O.profile_id) and  VP.variable_id = PV.id and O.id=? group by PV.name
            stm = con.prepareStatement("SELECT PV.zabbix_key, PV.name, PV.metric_unit FROM  `PolicyRelation` as PR,  `VariableProfile` as VP, `PolicyVariable` as PV, `VariableType` as VT, SPOffer as O WHERE PV.type_id=VT.id and ( VP.policy_id=O.policy_id  OR ( VP.profile_id= PR.slp_id and PR.policy_id=O.policy_id) OR VP.profile_id=O.profile_id) and  VP.variable_id = PV.id and O.id=? group by PV.name");
            stm.setInt(1, Integer.parseInt(offer_id));
            rs = stm.executeQuery();
            policyVariables = new HashMap<>();
            while (rs.next()) {
                if (null != rs.getString("zabbix_key")) {

                    policyVariables.put(rs.getString("zabbix_key"), rs.getString("name") + " - " + rs.getString("metric_unit"));
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseHandler.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } finally {
            DatabaseHandler.INSTANCE.closeDBStreams(con, stm, rs);
        }
        return policyVariables;
    }

    public static ZabbixItem calculateResponseTimeAndSendJSON(String IP) throws MalformedURLException, IOException, Exception {
        String res = calculateResponseTime("http://" + IP, 3000);
        return calculateformatedResponse("HTTPResponseTime", res, "int", "ms");

    }

    private static ZabbixItem calculateformatedResponse(String metricName, String metricValue, String metricType, String metricUnit) {
        long epoch = System.currentTimeMillis() / 1000;

        ZabbixItem item = new ZabbixItem();
        item.setZabbixCategory("System");
        item.setZabbixKey("http");
        item.setItemName(metricName);
        item.setValue(metricValue);
        item.setType(metricType);
        item.setZabbixName(metricUnit);
        item.setDate(String.valueOf(epoch));

        return item;
    } //EoM

    private static String calculateResponseTime(String url, int timeoutmillis) throws Exception {
        // start timer
        long nanoStart = System.nanoTime();
        long milliStart = System.currentTimeMillis();

        try {
            // do work to be timed
            doWork(url, timeoutmillis);
        } catch (IOException ex) {
            Logger.getLogger(ZabbixUtil.class.getName()).log(Level.SEVERE, null, ex);
            throw new Exception("Timout exception");
        }

        // stop timer
        long nanoEnd = System.nanoTime();
        long milliEnd = System.currentTimeMillis();

        // report response times
        long nanoTime = nanoEnd - nanoStart;
        long milliTime = milliEnd - milliStart;
        return reportResponseTimes(nanoTime, milliTime);
    }

    private static String reportResponseTimes(long nanoTime, long milliTime) {
        // convert nanoseconds to milliseconds and display both times with three digits of precision (microsecond)
        //String nanoFormatted = String.format("%,.3f", nanoTime / NANO_TO_MILLIS);
        String milliFormatted = String.format("%,.0f", milliTime / 1.0);

        return milliFormatted;
    }

    private static void doWork(String uri, int milliseconds) throws MalformedURLException, IOException {
        URL url = new URL(uri);
        URLConnection conn = url.openConnection();
        conn.setConnectTimeout(milliseconds);
        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        while (in.readLine() != null) {
        
        }
        in.close();
    }

    public static void main(String[] args) {
        int zabbixHostID = ZabbixUtil.insertNEWZabbixHost("OrbiInstance213.249.38.70", "213.249.38.70");
        System.out.println("ZabbixHost: " + zabbixHostID);
    }

}
