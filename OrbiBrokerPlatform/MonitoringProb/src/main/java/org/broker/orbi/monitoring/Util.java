package org.broker.orbi.monitoring;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.io.IOUtils;
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

    private static int UPTIME = 0;
    private static final String failPreventionMechanismURL = "http://192.168.3.34:8080/org.seerc.brokeratcloud.webservice/rest/topics/monitoring/MemoryLoad/Sintef";
    private static final String failPreventionCustomURL = "http://192.168.3.34:8080/org.seerc.brokeratcloud.webservice/rest/topics/monitoring/";

    public static List<Host> initializeZabbixHosts() {
        List<Host> listOfZabbixHosts = null;
        Host host = null;
        Connection con = DatabaseHandler.INSTANCE.getDatasource("broker");
        PreparedStatement stm = null;
        ResultSet rs = null;
        try {

            stm = con.prepareStatement("SELECT S.name,P.* FROM Purchase P,SPOffer O,ServiceDescription S where P.spoffer_id = O.id and O.profile_id=S.id;");
            rs = stm.executeQuery();
            listOfZabbixHosts = new ArrayList<>();
            while (rs.next()) {
                host = new Host();
                host.setHostID(rs.getString("zabbix_host_id"));
                host.setHostIP(rs.getString("vm_public_ip"));
                host.setUserID(rs.getInt("user_id"));
                host.setPurchaseID(rs.getInt("id"));
                host.setOfferID(rs.getInt("spoffer_id"));
                host.setHostName("P" + host.getPurchaseID() + "U" + host.getUserID() + "O" + host.getOfferID()+"_"+rs.getString("name"));

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
        } else if (_zabbixItem.getZabbixKey().equalsIgnoreCase("system.cpu.load[,avg1]")) {
            jsonObject.put("Offering", _host.getHostName());
            offering = _host.getHostName();
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
                case "system.cpu.load[,avg1]":
                    float cpuLoad = Float.valueOf(_zabbixItem.getValue()) * 125;
                    if (cpuLoad > 100) {
                        cpuLoad = 99F;
                    }
                    value = String.valueOf((int) cpuLoad);

                    System.out.println("CPU Load: " + value);
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
                + "\",\"timestamp\":\"" + _zabbixItem.getDate() + "\"}";

//        return jsonObject.toString();
        return jsonSTR;

    }

    public static String sendTopicNotificationMsg(String _JSONObject, String zabbixKey) {

        boolean isSuccess = false;

        switch (zabbixKey) {
            case "system.memoryload":
                isSuccess = postToFailPreventionMechanism(_JSONObject, zabbixKey, false);
                break;
            case "system.storageload":
                isSuccess = postToFailPreventionMechanism(_JSONObject, zabbixKey, false);
                break;
            case "apache[ReqPerSec]":
                isSuccess = postToFailPreventionMechanism(_JSONObject, zabbixKey, false);
                break;
            case "mysql.querytime":
                isSuccess = postToFailPreventionMechanism(_JSONObject, zabbixKey, false);
                break;
            case "mysql.threads":
                isSuccess = postToFailPreventionMechanism(_JSONObject, zabbixKey, false);
                break;
            case "system.cpu.load[,avg1]":
                isSuccess = postToFailPreventionMechanism(_JSONObject, zabbixKey, false);
                break;
            case "http":
                isSuccess = postToFailPreventionMechanism(_JSONObject, zabbixKey, false);
                break;
            default:
                isSuccess = postToFailPreventionMechanism(_JSONObject, zabbixKey, true);
                break;
        }

//        System.out.println("JSON: " + _JSONObject);
        if (isSuccess) {
            return "SUCCESS";
        } else {
            return "ERROR";
        }
    }

    private static boolean postToFailPreventionMechanism(String message, String zabbixKey, boolean mainURL) {

        PostMethod method = null;

        try {
            // Create a method instance.

            final HttpClient client = new HttpClient();

            if (mainURL) {

                method = new PostMethod(failPreventionMechanismURL);

            } else {

                String URL = "";
                switch (zabbixKey) {
                    case "system.memoryload":
                        URL = failPreventionCustomURL + "MemoryLoad/SiLo/";
                        break;
                    case "system.storageload":
                        URL = failPreventionCustomURL + "StorageLoad/SiLo/";
                        break;
                    case "apache[ReqPerSec]":
                        URL = failPreventionCustomURL + "RequestsPerSec/SiLo/";
                        break;
                    case "mysql.querytime":
                        URL = failPreventionCustomURL + "QueryTime/SiLo/";
                        break;
                    case "mysql.threads":
                        URL = failPreventionCustomURL + "Threads/SiLo/";
                        break;
                    case "system.cpu.load[,avg1]":
                        URL = failPreventionCustomURL + "CPULoadAvgPerCore/SiLo/";
                        break;
                    case "http":
                        URL = failPreventionCustomURL + "HTTPResponseTime/SiLo/";
                        break;
                    default:
                        URL = failPreventionMechanismURL;
                        break;
                }

                method = new PostMethod(URL);
            }

            RequestEntity re = new StringRequestEntity(message, "application/json", "UTF-8");

            method.setRequestEntity(re);

            int statusCode = client.executeMethod(method);

            if (statusCode != HttpStatus.SC_OK) {
                Logger.getLogger(Util.class.getName()).log(Level.SEVERE, "Method failed: {0}", method.getStatusLine());
                return false;
            } else {
                byte[] bytesArray = IOUtils.toByteArray(method.getResponseBodyAsStream());
//                Logger.getLogger(Util.class.getName()).log(Level.INFO, "Method response:  {0}", new String(bytesArray));
            }

//            method.releaseConnection();
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(Util.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        } catch (IOException ex) {
            Logger.getLogger(Util.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        } finally {
            if (null != method) {
                method.releaseConnection();
            }
        }

        return true;

    }

}
