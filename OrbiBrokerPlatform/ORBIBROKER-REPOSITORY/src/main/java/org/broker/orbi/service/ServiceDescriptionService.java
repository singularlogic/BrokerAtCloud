package org.broker.orbi.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.broker.orbi.models.Consumer;
import org.broker.orbi.models.ServiceDescription;
import org.broker.orbi.util.database.DatabaseHandler;

/**
 *
 * @author Christos Paraskeva <ch.paraskeva at gmail dot com>
 */
public class ServiceDescriptionService {

    public static ServiceDescription getServiceDescription(String servicedesc_id) {
        ServiceDescription sd = null;

        Connection con = DatabaseHandler.INSTANCE.getDatasource();
        PreparedStatement stm = null;
        ResultSet rs = null;
        try {
            stm = con.prepareStatement("SELECT SD.*, P.name as policy_name  FROM `Policy` P,ServiceDescription SD  WHERE  P.id=SD.policy_id and SD.id=?");
            stm.setInt(1, Integer.parseInt(servicedesc_id));
            rs = stm.executeQuery();
            if (rs.next()) {
                sd = new ServiceDescription(rs.getInt("id"), rs.getInt("policy_id"), rs.getInt("pid"), rs.getString("name"), rs.getString("full_name"), rs.getString("date_created"), rs.getString("date_edited"), rs.getString("policy_name"), rs.getString("content"));
            }
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseHandler.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } finally {
            DatabaseHandler.INSTANCE.closeDBStreams(con, stm, rs);
        }
        return sd;
    }

    public static ServiceDescription getServiceDescriptionByPurchaseId(int purchase_id) {
        ServiceDescription sd = null;
        Connection con = DatabaseHandler.INSTANCE.getDatasource();
        PreparedStatement stm = null;
        ResultSet rs = null;
        try {
            stm = con.prepareStatement("SELECT S.* FROM `SPOffer` as O,`Purchase` as P,`ServiceDescription`  as S WHERE S.id=O.profile_id AND O.id = P.spoffer_id AND P.id=?");
            stm.setInt(1, purchase_id);
            rs = stm.executeQuery();
            if (rs.next()) {
                sd = new ServiceDescription(rs.getInt("id"), rs.getInt("policy_id"), rs.getInt("pid"), rs.getString("name"), rs.getString("full_name"), rs.getString("date_created"), rs.getString("date_edited"), "", rs.getString("content"));
            }
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseHandler.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } finally {
            DatabaseHandler.INSTANCE.closeDBStreams(con, stm, rs);
        }
        return sd;
    }

    public static ServiceDescription getServiceDescriptionByOfferId(int offer_id) {
        ServiceDescription sd = null;
        Connection con = DatabaseHandler.INSTANCE.getDatasource();
        PreparedStatement stm = null;
        ResultSet rs = null;
        try {
            stm = con.prepareStatement("SELECT S.* FROM `SPOffer` as O,`ServiceDescription`  as S WHERE S.id=O.profile_id AND O.id =?");
            stm.setInt(1, offer_id);
            rs = stm.executeQuery();
            if (rs.next()) {
                sd = new ServiceDescription(rs.getInt("id"), rs.getInt("policy_id"), rs.getInt("pid"), rs.getString("name"), rs.getString("full_name"), rs.getString("date_created"), rs.getString("date_edited"), "N/A", rs.getString("content"));
            }
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseHandler.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } finally {
            DatabaseHandler.INSTANCE.closeDBStreams(con, stm, rs);
        }
        return sd;
    }

    //TODO: Refactor code to accept only valid integer
    public static List<ServiceDescription> getServiceDescriptions(String username) {
        List<ServiceDescription> serviceLevelProfiles = new ArrayList<>();
        Consumer user = UserService.getUser(username);

        Connection con = DatabaseHandler.INSTANCE.getDatasource();
        PreparedStatement stm = null;
        ResultSet rs = null;
        try {
            stm = con.prepareStatement("SELECT SP.*,P.name as policy_name FROM `ServiceDescription` SP, Policy P WHERE  P.id=SP.policy_id AND pid=?");
            stm.setInt(1, user.getId());
            rs = stm.executeQuery();
            while (rs.next()) {
                serviceLevelProfiles.add(new ServiceDescription(rs.getInt("id"), rs.getInt("policy_id"), rs.getInt("pid"), rs.getString("name"), rs.getString("full_name"), rs.getString("date_created"), rs.getString("date_edited"), rs.getString("policy_name")));
            }
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseHandler.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } finally {
            DatabaseHandler.INSTANCE.closeDBStreams(con, stm, rs);
        }

        return serviceLevelProfiles;
    }

    public static boolean storeServiceDescription(ServiceDescription serviceDescription) {
        Connection con = DatabaseHandler.INSTANCE.getDatasource();

        PreparedStatement stm = null;
        try {
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = new Date();
            String current_date = dateFormat.format(date);
            stm = con.prepareStatement("INSERT INTO  `ServiceDescription` (policy_id,pid,name,full_name,date_created,date_edited,content) VALUES (?,?,?,?,?,?,?)");
            stm.setInt(1, serviceDescription.getPolicy_id());
            stm.setInt(2, serviceDescription.getPid());
            stm.setString(3, serviceDescription.getName());
            stm.setString(4, serviceDescription.getFull_name());
            stm.setString(5, current_date);
            stm.setString(6, current_date);
            stm.setString(7, serviceDescription.getContent());
            stm.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(ServiceDescriptionService.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        } finally {
            DatabaseHandler.INSTANCE.closeDBStreams(con, stm, null);
        }
        return true;
    }

    public static boolean updateServiceDescription(ServiceDescription serviceDescription) {
        Connection con = DatabaseHandler.INSTANCE.getDatasource();
        PreparedStatement stm = null;
        try {
            stm = con.prepareStatement("UPDATE `ServiceDescription` set name=?,content=?");
            stm.setString(1, serviceDescription.getName());
            stm.setString(2, serviceDescription.getContent());
            stm.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(ServiceDescriptionService.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        } finally {
            DatabaseHandler.INSTANCE.closeDBStreams(con, stm, null);
        }
        return true;
    }

    public static boolean removeServiceDescription(int serviceDescriptionID) {
        Connection con = DatabaseHandler.INSTANCE.getDatasource();
        PreparedStatement stm = null;
        try {
            stm = con.prepareStatement("DELETE FROM `ServiceDescription` WHERE id=?");
            stm.setInt(1, serviceDescriptionID);
            stm.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(ServiceDescriptionService.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        } finally {
            DatabaseHandler.INSTANCE.closeDBStreams(con, stm, null);
        }
        return true;
    }
}
