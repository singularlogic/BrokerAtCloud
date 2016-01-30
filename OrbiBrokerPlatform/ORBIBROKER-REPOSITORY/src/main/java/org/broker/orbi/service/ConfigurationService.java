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
import org.broker.orbi.models.Configuration;
import org.broker.orbi.util.database.DatabaseHandler;

/**
 *
 * @author Christos Paraskeva <ch.paraskeva at gmail dot com>
 */
public class ConfigurationService {

    public static List<Configuration> getConfigurations() {
        List<Configuration> configurationList = new ArrayList<>();
        Connection con = DatabaseHandler.INSTANCE.getDatasource();
        PreparedStatement stm = null;
        ResultSet rs = null;
        try {
            stm = con.prepareStatement("SELECT C.* FROM `Configuration` C");
            rs = stm.executeQuery();
            while (rs.next()) {
                configurationList.add(new Configuration(rs.getInt("id"), rs.getString("name"), rs.getString("value"), rs.getString("friendly_name"), rs.getString("date_edited")));
            }
        } catch (SQLException ex) {
            Logger.getLogger(ConfigurationService.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } finally {
            DatabaseHandler.INSTANCE.closeDBStreams(con, stm, rs);
        }
        return configurationList;
    }

    public static Configuration getConfiguration(String configID) {
        Configuration configuration = null;
        Connection con = DatabaseHandler.INSTANCE.getDatasource();
        PreparedStatement stm = null;
        ResultSet rs = null;
        try {
            stm = con.prepareStatement("SELECT C.* FROM `Configuration` C WHERE C.id =?");
            stm.setInt(1, Integer.valueOf(configID));
            rs = stm.executeQuery();
            if (rs.next()) {
                configuration = new Configuration(rs.getInt("id"), rs.getString("name"), rs.getString("value"), rs.getString("friendly_name"), rs.getString("date_edited"));
            }
        } catch (SQLException ex) {
            Logger.getLogger(ConfigurationService.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } finally {
            DatabaseHandler.INSTANCE.closeDBStreams(con, stm, rs);
        }
        return configuration;
    }
    
    
        public static boolean storeConfiguration(Configuration config) {
        Connection con = DatabaseHandler.INSTANCE.getDatasource();
        PreparedStatement stm = null;
        try {
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = new Date();
            String current_date = dateFormat.format(date);
            stm = con.prepareStatement("INSERT INTO  `Configuration` (name,value,friendly_name,date_edited) VALUES (?,?,?,?)");
            stm.setString(1, config.getName());
            stm.setString(2, config.getValue());
            stm.setString(3, config.getFriendly_name());
            stm.setString(4, current_date);
            stm.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(Configuration.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        } finally {
            DatabaseHandler.INSTANCE.closeDBStreams(con, stm, null);
        }
        return true;
    }

    public static boolean updateConfiguration(Configuration config) {
        Connection con = DatabaseHandler.INSTANCE.getDatasource();
        PreparedStatement stm = null;
        try {
            stm = con.prepareStatement("UPDATE `Configuration` set name=?,value=?,friendly_name=?");
            stm.setString(1, config.getName());
            stm.setString(2, config.getValue());
            stm.setString(3, config.getFriendly_name());
            stm.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(Configuration.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        } finally {
            DatabaseHandler.INSTANCE.closeDBStreams(con, stm, null);
        }
        return true;
    }
    

    public static boolean removeConfiguration(int configurationID) {
        Connection con = DatabaseHandler.INSTANCE.getDatasource();
        PreparedStatement stm = null;
        try {
            stm = con.prepareStatement("DELETE FROM `Configuration` WHERE id=?");
            stm.setInt(1, configurationID);
            stm.executeUpdate();

        } catch (SQLException ex) {
            Logger.getLogger(Configuration.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        } finally {
            DatabaseHandler.INSTANCE.closeDBStreams(con, stm, null);
        }
        return true;
    }

}
