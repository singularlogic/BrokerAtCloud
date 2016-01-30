package org.broker.orbi.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.broker.orbi.models.IaaSConfiguration;
import static org.broker.orbi.service.UserService.getUserID;
import org.broker.orbi.util.database.DatabaseHandler;

/**
 *
 * @author Christos Paraskeva <ch.paraskeva at gmail dot com>
 */
public class IaaSConfigurationService {

    public static List<IaaSConfiguration> getIaaSProviders(String username) {
        List<IaaSConfiguration> iaasProvidersList = new ArrayList<>();
        Connection con = DatabaseHandler.INSTANCE.getDatasource();
        PreparedStatement stm = null;
        ResultSet rs = null;
        try {
            stm = con.prepareStatement("SELECT * FROM `IaaSProvider` WHERE pid = (select uid from User WHERE username=?);");
            stm.setString(1, username);
            rs = stm.executeQuery();
            while (rs.next()) {
                iaasProvidersList.add(new IaaSConfiguration(rs.getInt("id"), rs.getInt("pid"), rs.getString("name"), rs.getString("endpoint"), rs.getString("tenant_name"), rs.getString("username"), rs.getString("password").replaceAll(".", "*"), rs.getString("date_edited")));
            }
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseHandler.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } finally {
            DatabaseHandler.INSTANCE.closeDBStreams(con, stm, rs);
        }
        return iaasProvidersList;
    }

    public static IaaSConfiguration getIaaSProvider(String iaasProviderID) {
        IaaSConfiguration iaasProvider = null;
        Connection con = DatabaseHandler.INSTANCE.getDatasource();
        PreparedStatement stm = null;
        ResultSet rs = null;
        try {
            stm = con.prepareStatement("SELECT * FROM `IaaSProvider` WHERE id = ?");
            stm.setInt(1, Integer.parseInt(iaasProviderID));
            rs = stm.executeQuery();
            if (rs.next()) {
                iaasProvider = new IaaSConfiguration(rs.getInt("id"), rs.getInt("pid"), rs.getString("name"), rs.getString("endpoint"), rs.getString("tenant_name"), rs.getString("username"), rs.getString("password").replaceAll(".", "*"), rs.getString("date_edited"));
            }
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseHandler.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } finally {
            DatabaseHandler.INSTANCE.closeDBStreams(con, stm, rs);
        }
        return iaasProvider;
    }

    public static boolean modifyIaaSProviderDetails(int iaas_id, String endpoint, String tenant_name, String username, String password, String name) {

        System.out.println("Id: " + iaas_id + " endpoint: " + endpoint + " Name: " + tenant_name + " username: " + username + " password: " + password);

        boolean isSuccess = false;
        Connection con = DatabaseHandler.INSTANCE.getDatasource();
        PreparedStatement stm = null;

        //Basic Validation for Input
        if (endpoint.isEmpty() || tenant_name.isEmpty() || username.isEmpty() || password.isEmpty()) {
            return isSuccess;
        }
        try {
            //Update existing record
            if (iaas_id > 0) {
                stm = con.prepareStatement("UPDATE `IaaSProvider` SET endpoint=?,tenant_name=?,username=?,password=?,name=? WHERE id=?");
                stm.setString(1, endpoint);
                stm.setString(2, tenant_name);
                stm.setString(3, username);
                stm.setString(4, password);
                stm.setInt(5, iaas_id);
                stm.setString(6, name);
                stm.executeUpdate();
                isSuccess = !isSuccess;
            } //New Record
            else {
                int providerID = getUserID(username);
                stm = con.prepareStatement("INSERT INTO `IaaSProvider` (`pid`, `endpoint`, `tenant_name`, `username`, `password`,`name`) VALUES ( ?,?,?,?,?,?)");
                stm.setInt(1, providerID);
                stm.setString(2, endpoint);
                stm.setString(3, tenant_name);
                stm.setString(4, username);
                stm.setString(5, password);
                stm.setString(6, name);
                stm.executeUpdate();
                isSuccess = !isSuccess;
            }
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseHandler.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            DatabaseHandler.INSTANCE.closeDBStreams(con, stm, null);
        }
        return isSuccess;
    }

    public static boolean removeIaaSProvider(int iaasProviderID) {
        Connection con = DatabaseHandler.INSTANCE.getDatasource();
        PreparedStatement stm = null;
        try {
            stm = con.prepareStatement("DELETE FROM `IaaSProvider` WHERE id=?");
            stm.setInt(1, iaasProviderID);
            stm.executeUpdate();

        } catch (SQLException ex) {
            Logger.getLogger(IaaSConfigurationService.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        } finally {
            DatabaseHandler.INSTANCE.closeDBStreams(con, stm, null);
        }
        return true;
    }

}
