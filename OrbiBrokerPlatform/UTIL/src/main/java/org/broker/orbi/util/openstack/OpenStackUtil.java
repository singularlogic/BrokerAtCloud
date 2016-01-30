package org.broker.orbi.util.openstack;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.broker.orbi.util.database.DatabaseHandler;

/**
 *
 * @author Christos Paraskeva <ch.paraskeva at gmail dot com>
 */
public class OpenStackUtil {
    
    
        public static Map<String, String> getOpenStackIntegrationDetails(int offerID) {
        Map<String, String> openstackIntegrationDetails = new HashMap<>();

        Connection con = DatabaseHandler.INSTANCE.getDatasource();
        PreparedStatement stm = null;
        ResultSet rs = null;
        try {

            stm = con.prepareStatement("SELECT IM.hex_id ,IP.pid as provider_id, IP.endpoint,IP.tenant_name as tenant,IP.username,IP.password,O.image_template_id as templateid, O.flavor_id as flavorid , O.name as offer_name FROM `User` U, `SPOffer` O ,`IaaSProvider` IP, `ImageTemplate` IM  WHERE O.username=U.username and O.id=? and IP.pid=U.uid AND IM.id=O.image_template_id limit 1");
            stm.setInt(1, offerID);
            rs = stm.executeQuery();
            if (rs.next()) {
                openstackIntegrationDetails.put("username", rs.getString("username"));
                openstackIntegrationDetails.put("password", rs.getString("password"));
                openstackIntegrationDetails.put("tenant", rs.getString("tenant"));
                openstackIntegrationDetails.put("endpoint", rs.getString("endpoint"));
                openstackIntegrationDetails.put("flavorid", rs.getString("flavorid"));
                openstackIntegrationDetails.put("templateid", rs.getString("hex_id"));
                Random rn = new Random();
                openstackIntegrationDetails.put("instancename", "User" + rn.nextInt(1000) + rs.getString("offer_name") + "Provider" + rs.getString("provider_id"));
                System.out.println("OpenStack Integration Details\n\nusername: " + openstackIntegrationDetails.get("username") + "\n"
                        + "password: " + openstackIntegrationDetails.get("password") + "\n"
                        + "tenant: " + openstackIntegrationDetails.get("tenant") + "\n"
                        + "endpoint: " + openstackIntegrationDetails.get("endpoint") + "\n"
                        + "flavorid: " + openstackIntegrationDetails.get("flavorid") + "\n"
                        + "templateid: " + openstackIntegrationDetails.get("templateid") + "\n"
                        + "instancename: " + openstackIntegrationDetails.get("instancename") + "\n"
                );
            }

        } catch (SQLException ex) {
            Logger.getLogger(OpenStackUtil.class.getName()).log(Level.SEVERE, null, ex);
            //Close all Streams  
        } finally {
            DatabaseHandler.INSTANCE.closeDBStreams(con, stm, null);
        }

        //
        return openstackIntegrationDetails;
    }
    
}
