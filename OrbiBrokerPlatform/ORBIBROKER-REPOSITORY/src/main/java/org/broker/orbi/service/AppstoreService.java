package org.broker.orbi.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.broker.orbi.models.Application;
import org.broker.orbi.util.database.DatabaseHandler;

/**
 *
 * @author Christos Paraskeva <ch.paraskeva at gmail dot com>
 */
public class AppstoreService {

    public static List<Application> getAllImages() {
        List<Application> imageTemplates = new ArrayList<>();

        Connection con = DatabaseHandler.INSTANCE.getDatasource();
        PreparedStatement stm = null;
        ResultSet rs = null;
        try {

            stm = con.prepareStatement("SELECT * from ImageTemplate");
            rs = stm.executeQuery();

            while (rs.next()) {
                imageTemplates.add(new Application(rs.getInt("id"), rs.getString("name"), rs.getString("hex_id")));
            }
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseHandler.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } finally {
            DatabaseHandler.INSTANCE.closeDBStreams(con, stm, rs);
        }
        return imageTemplates;
    }

    public static List<Application> getAllImages(String username) {
        List<Application> imageTemplates = new ArrayList<>();
               
        Connection con = DatabaseHandler.INSTANCE.getDatasource();
        PreparedStatement stm = null;
        ResultSet rs = null;
        try {

            stm = con.prepareStatement("SELECT * from ImageTemplate WHERE uid= (select uid from User where username=?)");
            stm.setString(1, username);
            rs = stm.executeQuery();

            while (rs.next()) {
                imageTemplates.add(new Application(rs.getInt("id"), rs.getString("hex_id"),rs.getString("name"), rs.getString("date_edited") ,rs.getString("metadata"),rs.getString("thumbnail") ));
            }
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseHandler.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } finally {
            DatabaseHandler.INSTANCE.closeDBStreams(con, stm, rs);
        }
        return imageTemplates;
    }
    
    
        public static boolean removeImage(int imageID) {
        Connection con = DatabaseHandler.INSTANCE.getDatasource();
        PreparedStatement stm = null;
        try {
            stm = con.prepareStatement("DELETE FROM `ImageTemplate` WHERE id=?");
            stm.setInt(1, imageID);
            stm.executeUpdate();

        } catch (SQLException ex) {
            Logger.getLogger(AppstoreService.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        } finally {
            DatabaseHandler.INSTANCE.closeDBStreams(con, stm, null);
        }
        return true;
    }

}
