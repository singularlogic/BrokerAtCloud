package org.broker.orbi.monitoring;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.broker.orbi.topic.BrokerTopicSubscriber;
import org.broker.orbi.util.database.DatabaseHandler;
import org.broker.orbi.util.entities.Host;
import org.broker.orbi.util.entities.ZabbixItem;
import org.broker.orbi.util.impl.ZabbixUtil;
import org.json.JSONObject;

/**
 *
 * @author smantzouratos
 */
public class Util {

    static final private BrokerTopicSubscriber topicSub = new BrokerTopicSubscriber();
    private static int UPTIME = 0;

    public static List<Host> initializeZabbixHosts() {
        List<Host> listOfZabbixHosts = null;
        Host host = null;
        Connection con = DatabaseHandler.INSTANCE.getDatasource("broker");
        PreparedStatement stm = null;
        ResultSet rs = null;
        try {

            stm = con.prepareStatement("SELECT * FROM Purchase;");
            rs = stm.executeQuery();
            listOfZabbixHosts = new ArrayList<>();
            while (rs.next()) {
                host = new Host();
                host.setHostID(rs.getString("zabbix_host_id"));
                host.setHostIP(rs.getString("vm_public_ip"));
                host.setUserID(rs.getInt("user_id"));
                host.setPurchaseID(rs.getInt("id"));
                host.setOfferID(rs.getInt("spoffer_id"));
                host.setHostName("P" + host.getPurchaseID() + "U" + host.getUserID() + "O" + host.getOfferID());

                // Retrieve Zabbix Items per HostID
                Map<String, ZabbixItem> mapOfZabbixItems = ZabbixUtil.getItemsFromZabbix(Integer.valueOf(host.getHostID()));

                host.setZabbixItems(mapOfZabbixItems);

                listOfZabbixHosts.add(host);
            }

        } catch (SQLException ex) {
            Logger.getLogger(Util.class.getName()).log(Level.SEVERE, null, ex);
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
                Logger.getLogger(Util.class.getName()).log(Level.SEVERE, null, ex);
                return null;
            }
        }

        return listOfZabbixHosts;

    }

    public static String startRetrievalFromZabbix(List<Host> listOfHosts) {
        String response = null;

        // Create Datasource
        Connection con = DatabaseHandler.INSTANCE.getDatasource("zabbix");
        PreparedStatement ps = null;
        ResultSet rs = null;
        int total = 0;
        int counter = 0;

        try {

            for (Host tempHost : listOfHosts) {

                Map<String, String> mapOfZabbixKeys = ZabbixUtil.getAllPolicyProfileVariablesDependOnOffer(String.valueOf(tempHost.getOfferID()));

                for (Map.Entry pairs1 : tempHost.getZabbixItems().entrySet()) {
                    ZabbixItem tempZabbixItem = (ZabbixItem) pairs1.getValue();

                    if (mapOfZabbixKeys.containsKey(tempZabbixItem.getZabbixKey())) {
                        
                        // Map<String, String> 
                        total++;
                        if (ps != null) {
                            ps = null;
                        }
                        if (rs != null) {
                            rs = null;
                        }

                        String sql = "";

                        if (tempZabbixItem.getType().equalsIgnoreCase("str")) {
                            sql = "SELECT itemid, clock, value from history_str where itemid = ? order by clock desc limit 1;";
                        } else if (tempZabbixItem.getType().equalsIgnoreCase("int")) {
                            sql = "SELECT itemid, clock, value from history_uint where itemid = ? order by clock desc limit 1;";
                        } else {
                            sql = "SELECT itemid, clock, value from history where itemid = ? order by clock desc limit 1;";
                        }

                        ps = con.prepareStatement(sql);
                        ps.setString(1, tempZabbixItem.getItemID());
                        rs = ps.executeQuery();
                        while (rs.next()) {

                            if (tempZabbixItem.getZabbixKey().equalsIgnoreCase("apache[Uptime]")) {
                                float upTimeFloat = Float.valueOf(rs.getString("value"));
                                UPTIME = (int) upTimeFloat;
                            }

                            tempZabbixItem.setValue(rs.getString("value"));
                            tempZabbixItem.setDate(rs.getString("clock"));

                            // For each metric create a JSON Object and send a notification
                            String tempJSONObject = createJSONObject(tempHost, tempZabbixItem);
                            String tempResponse = sendTopicNotificationMsg(tempJSONObject, tempZabbixItem.getZabbixKey());

                            if (tempResponse.equalsIgnoreCase("SUCCESS")) {
                                counter++;
                            }

                        }

                    }

                }
                // HTTP Response Time Metric
                String httpJSONObject;
                try {
                    ZabbixItem httpZabbixItem = ZabbixUtil.calculateResponseTimeAndSendJSON(tempHost.getHostIP());
                    httpJSONObject = createJSONObject(tempHost, httpZabbixItem);
                    String httpResponse = sendTopicNotificationMsg(httpJSONObject, httpZabbixItem.getZabbixKey());
                    if (httpResponse.equalsIgnoreCase("SUCCESS")) {
                        counter++;
                    }

                } catch (Exception ex) {
                    Logger.getLogger(Util.class.getName()).log(Level.SEVERE, null, ex);
                }

            }

        } catch (SQLException ex) {
            Logger.getLogger(ZabbixUtil.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }//Ensure tha all streams will be closed//Ensure tha all streams will be closed
        finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (ps != null) {
                    ps.close();
                }
                con.close();
            } catch (SQLException ex) {
                Logger.getLogger(ZabbixUtil.class.getName()).log(Level.SEVERE, null, ex);
                return null;
            }
        }

        response = counter + "/" + total + " notifications sent successfully!";

        return response;
    }

    public static String createJSONObject(Host _host, ZabbixItem _zabbixItem) {
        
        String jsonSTR = "";
        
        String offering = "";
        
        JSONObject jsonObject = new JSONObject();

        if (_zabbixItem.getZabbixCategory().equalsIgnoreCase("System")) {
            jsonObject.put("Offering", _host.getHostName());
            offering = _host.getHostName();
        } else if (_zabbixItem.getZabbixCategory().equalsIgnoreCase("Database")) {
            jsonObject.put("Offering", _host.getHostName() + "_Database");
            offering = _host.getHostName() + "_Database";
        } else {
            jsonObject.put("Offering", _host.getHostName() + "_AS");
            offering = _host.getHostName() + "_AS";
        }

        String value = "";
        String type = "";

        if (_zabbixItem.getType().equalsIgnoreCase("str")) {
            type = "String";
        } else if (_zabbixItem.getType().equalsIgnoreCase("int")) {
            type = "Integer";
        } else if (_zabbixItem.getType().equalsIgnoreCase("float")) {
            type = "Float";
        } else {
            type = _zabbixItem.getType();
        }

        if (null != _zabbixItem.getZabbixKey()) {

            switch (_zabbixItem.getZabbixKey()) {
                case "apache[ReqPerSec]":
                    float reqPerSec = Float.valueOf(_zabbixItem.getValue());
                    value = String.valueOf((int) reqPerSec);
                    type = "Integer";
                    break;
                case "mysql.threads":
                    float threads = Float.valueOf(_zabbixItem.getValue());
                    value = String.valueOf((int) threads);
                    type = "Integer";
                    break;
                case "mysql.questions":
                    float questions = Float.valueOf(_zabbixItem.getValue());
                    value = String.valueOf((int) ((float) questions));
                    type = "Integer";
                    break;
                case "mysql.slowqueries":
                    float slowQueries = Float.valueOf(_zabbixItem.getValue());
                    value = String.valueOf((int) slowQueries);
                    type = "Integer";
                    break;
                case "system.cpu.util[,user]":
                    float cpuLoad = Float.valueOf(_zabbixItem.getValue());
                    value = String.valueOf((int) cpuLoad);
                    type = "Integer";
                    break;
                case "apache[Uptime]":
                    float apacheUptime = Float.valueOf(_zabbixItem.getValue());
                    value = String.valueOf((int) apacheUptime);
                    type = "Integer";
                    break;
                default:
                    value = _zabbixItem.getValue();
                    break;
            }
        } else {
            value = _zabbixItem.getValue();
        }

        jsonObject.put(_zabbixItem.getItemName(), value);

        jsonObject.put(_zabbixItem.getItemName() + "Type", type);
        jsonObject.put(_zabbixItem.getItemName() + "Unit", _zabbixItem.getZabbixName());
        jsonObject.put("Timestamp", _zabbixItem.getDate());
        
        
        jsonSTR = "{\"" + _zabbixItem.getItemName() + "\":\"" + value + "\",\"Offering\":\"" + offering + "\",\""
                + _zabbixItem.getItemName() + "Type\":\"" + type + "\",\"" 
                + _zabbixItem.getItemName() + "Unit\":\"" + _zabbixItem.getZabbixName() 
                +"\",\"timestamp\":\"" + _zabbixItem.getDate() +"\"}";        

//        return jsonObject.toString();
        return jsonSTR;

    }

    public static String sendTopicNotificationMsg(String _JSONObject, String zabbixKey) {

        String response = topicSub.sendMsg(_JSONObject, zabbixKey);

        System.out.println("JSON: " + _JSONObject);

        if (null != response) {
            return response;
        } else {
            return "SUCCESS";
        }
    }

}
