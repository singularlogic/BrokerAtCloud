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
import org.broker.orbi.models.Policy;
import org.broker.orbi.util.database.DatabaseHandler;

/**
 *
 * @author Christos Paraskeva <ch.paraskeva at gmail dot com>
 */
public class PolicyService {

    public static List<Policy> getAllPolicies() {

        List<Policy> policies = new ArrayList<>();
        Connection con = DatabaseHandler.INSTANCE.getDatasource();
        PreparedStatement stm = null;
        ResultSet rs = null;
        try {
            stm = con.prepareStatement("SELECT *  FROM `Policy`");
            rs = stm.executeQuery();
            while (rs.next()) {
                policies.add(new Policy(rs.getInt("id"), rs.getString("name"), rs.getString("date_created"), rs.getString("date_edited"), rs.getString("content")));
            }
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseHandler.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } finally {
            DatabaseHandler.INSTANCE.closeDBStreams(con, stm, rs);
        }
        return policies;

    }

    public static Policy getPolicy(String policyID) {
        Connection con = DatabaseHandler.INSTANCE.getDatasource();
        PreparedStatement stm = null;
        ResultSet rs = null;
        Policy policy = null;
        try {
            stm = con.prepareStatement("SELECT P . *  FROM `Policy` AS P WHERE P.id=? ");
            stm.setInt(1, Integer.parseInt(policyID));
            rs = stm.executeQuery();
            if (rs.next()) {
                policy = new Policy(rs.getInt("id"), rs.getString("name"), rs.getString("date_created"), rs.getString("date_edited"), rs.getString("content"));
            }
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseHandler.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } finally {
            DatabaseHandler.INSTANCE.closeDBStreams(con, stm, rs);
        }
        return policy;
    }

    public static boolean storePolicy(Policy policy) {
        Connection con = DatabaseHandler.INSTANCE.getDatasource();
        PreparedStatement stm = null;
        try {
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = new Date();
            String current_date = dateFormat.format(date);
            stm = con.prepareStatement("INSERT INTO  `Policy` (name,date_created,date_edited,content) VALUES (?,?,?,?)");
            stm.setString(1, policy.getName());
            stm.setString(2, current_date);
            stm.setString(3, current_date);
            stm.setString(4, policy.getContent());
            stm.executeUpdate();

        } catch (SQLException ex) {
            Logger.getLogger(PolicyService.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        } finally {
            DatabaseHandler.INSTANCE.closeDBStreams(con, stm, null);
        }
        return true;
    }

    public static boolean updateBrokerPolicy(Policy policy) {
        Connection con = DatabaseHandler.INSTANCE.getDatasource();
        PreparedStatement stm = null;
        try {
            stm = con.prepareStatement("UPDATE `Policy` set content=?");
            stm.setString(1, policy.getContent());
            stm.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(PolicyService.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        } finally {
            DatabaseHandler.INSTANCE.closeDBStreams(con, stm, null);
        }
        return true;
    }

    public static boolean removePolicy(int policyID) {
        Connection con = DatabaseHandler.INSTANCE.getDatasource();
        PreparedStatement stm = null;
        try {
            stm = con.prepareStatement("DELETE FROM `Policy` WHERE id=?");
            stm.setInt(1, policyID);
            stm.executeUpdate();

        } catch (SQLException ex) {
            Logger.getLogger(PolicyService.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        } finally {
            DatabaseHandler.INSTANCE.closeDBStreams(con, stm, null);
        }
        return true;
    }
}
