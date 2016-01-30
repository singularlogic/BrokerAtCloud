package org.broker.orbi.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.broker.orbi.models.Purchase;
import org.broker.orbi.models.VariableType;
import org.broker.orbi.util.database.DatabaseHandler;
import org.broker.orbi.util.impl.ZabbixUtil;
import org.broker.orbi.util.openstack.OpenStackIntegration;
import org.broker.orbi.util.other.VMUtilization;

/**
 *
 * @author Christos Paraskeva <ch.paraskeva at gmail dot com>
 */
public class PurchaseService {

    public static List<Purchase> getAllPurchasedSPOffers(int userID) {
        List<Purchase> purchases = new ArrayList<>();

        Connection con = DatabaseHandler.INSTANCE.getDatasource();
        PreparedStatement stm = null;
        ResultSet rs = null;
        try {
            stm = con.prepareStatement("SELECT a.vm_public_ip publicIP,a.id, a.date_purchased, a.spoffer_id, b.policy_id, d.name as policy_name, b.profile_id, c.name as profile_name, b.name as spoffer_name, b.date_created, b.username as provider_username FROM Purchase a, SPOffer b, ServiceDescription c, Policy d WHERE a.spoffer_id = b.id and b.profile_id = c.id and b.policy_id = d.id and a.user_id = ?");
            stm.setInt(1, userID);
            rs = stm.executeQuery();

            while (rs.next()) {
                Map<String, String> variables = ZabbixUtil.getAllPolicyProfileVariablesDependOnOffer(rs.getString("spoffer_id"));
                List<VariableType> listOfVariables = new ArrayList<>();
                for (Map.Entry<String, String> entry : variables.entrySet()) {
                    String tempKey = entry.getKey();
                    String tempValue = entry.getValue();
                    listOfVariables.add(new VariableType(tempKey, tempValue));
                }
                Purchase tmpPurachse = new Purchase(rs.getInt("id"), rs.getInt("spoffer_id"), rs.getInt("policy_id"), rs.getString("policy_name"), rs.getInt("profile_id"), rs.getString("profile_name"), rs.getString("spoffer_name"), rs.getString("date_created"), rs.getString("date_purchased"), rs.getString("provider_username"), listOfVariables, rs.getString("publicIP"));
                String vmUtilization = VMUtilization.getServerInfo("http://".concat(tmpPurachse.getPublicIP().concat("/info.php")));
                tmpPurachse.setRamUtil(vmUtilization.isEmpty() ? "N/A" : vmUtilization.split("@")[0]);
                tmpPurachse.setCpuUtil(vmUtilization.isEmpty() ? "N/A" : vmUtilization.split("@")[1]);
                tmpPurachse.setVmStatus();
                purchases.add(tmpPurachse);
            }
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseHandler.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } finally {
            DatabaseHandler.INSTANCE.closeDBStreams(con, stm, rs);
        }
        return purchases;
    }

    public static boolean purchaseServiceOffering(String username, int offerID, String publicIP, int zabbixHostID, String serverID) {

        // Steps 
        // Call Openstack Library to create the new VM
        // Register the new host to the ZABBIX Database
        // Insert user purchase in Broker DB    
        int userID = UserService.getUserID(username);

        Connection con = DatabaseHandler.INSTANCE.getDatasource();
        PreparedStatement stm = null;
        try {

            stm = con.prepareStatement("INSERT INTO  `Purchase` (user_id, spoffer_id, vm_public_ip, zabbix_host_id,serverID) VALUES (?,?,?,?,?)");
            //Add Foreign key
            stm.setInt(1, userID);
            stm.setInt(2, offerID);
//            stm.setNull(2, Types.INTEGER);
            stm.setString(3, publicIP);
            stm.setInt(4, zabbixHostID);
            stm.setString(5, serverID);
            stm.executeUpdate();

        } catch (SQLException ex) {
            Logger.getLogger(PurchaseService.class.getName()).log(Level.SEVERE, null, ex);
            return false;
            //Close all Streams  
        } finally {
            DatabaseHandler.INSTANCE.closeDBStreams(con, stm, null);
        }
        return true;
    }

    public static boolean removePurchase(int purchaseID) {
        Connection con = DatabaseHandler.INSTANCE.getDatasource();
        PreparedStatement stm = null;

        try {
            String serverID = "";
            String offerID = "";
            int hostID = 0;
            String username = "";
            String password = "";
            String tenant_name = "";
            String endpoint = "";

            stm = con.prepareStatement("select  IP.*, P.serverID,P.spoffer_id,P.zabbix_host_id FROM  `Purchase` P,`IaaSProvider` IP, `SPOffer` O WHERE P.id=? AND O.id=P.spoffer_id AND IP.pid in (select U.uid from User U where username = O.username) limit 1");
            stm.setInt(1, purchaseID);
            ResultSet rs = stm.executeQuery();
            if (rs.next()) {
                serverID = rs.getString("serverID");
                offerID = rs.getString("spoffer_id");
                hostID = rs.getInt("zabbix_host_id");
                username = rs.getString("username");
                password = rs.getString("password");
                tenant_name = rs.getString("tenant_name");
                endpoint = rs.getString("endpoint");
            }

            OpenStackIntegration osi = new OpenStackIntegration();
            osi.destroyInstance(endpoint, username, password, tenant_name, serverID);

            // Remove From ZABBIX Server
            boolean isSuccess = ZabbixUtil.deleteZabbixHost(hostID);

            stm = con.prepareStatement("DELETE FROM `Purchase` WHERE id=?");
            stm.setInt(1, purchaseID);
            stm.executeUpdate();

        } catch (SQLException ex) {
            Logger.getLogger(PurchaseService.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        } finally {
            DatabaseHandler.INSTANCE.closeDBStreams(con, stm, null);
        }
        return true;
    }

}
