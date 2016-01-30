package org.broker.orbi.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.broker.orbi.models.Flavor;
import org.broker.orbi.util.database.DatabaseHandler;

/**
 *
 * @author Christos Paraskeva <ch.paraskeva at gmail dot com>
 */
public class FlavorService {

    public static List<Flavor> getAllFlavors() {
        List<Flavor> flavors = new ArrayList<>();

        Connection con = DatabaseHandler.INSTANCE.getDatasource();
        PreparedStatement stm = null;
        ResultSet rs = null;
        try {

            stm = con.prepareStatement("SELECT * from Flavor");
            rs = stm.executeQuery();

            while (rs.next()) {
                flavors.add(new Flavor(rs.getInt("id"),rs.getInt("pid"), rs.getString("name"), rs.getString("parameters"), rs.getString("date_edited")));
            }
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseHandler.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            DatabaseHandler.INSTANCE.closeDBStreams(con, stm, rs);
        }
        return flavors;
    }

    public static List<Flavor> getAllFlavors(String username) {
        List<Flavor> flavors = new ArrayList<>();
        Connection con = DatabaseHandler.INSTANCE.getDatasource();
        PreparedStatement stm = null;
        ResultSet rs = null;
        try {
            stm = con.prepareStatement("SELECT * from Flavor WHERE pid= (select uid from User where username=?)");
            stm.setString(1, username);
            rs = stm.executeQuery();
            while (rs.next()) {
                flavors.add(new Flavor(rs.getInt("id"),rs.getInt("pid"), rs.getString("name"), rs.getString("parameters"), rs.getString("date_edited")));
            }
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseHandler.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            DatabaseHandler.INSTANCE.closeDBStreams(con, stm, rs);
        }
        return flavors;
    }

    public static boolean removeFlavor(int flavorID) {
        Connection con = DatabaseHandler.INSTANCE.getDatasource();
        PreparedStatement stm = null;
        try {
            stm = con.prepareStatement("DELETE FROM `Flavor` WHERE id=?");
            stm.setInt(1, flavorID);
            stm.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(FlavorService.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        } finally {
            DatabaseHandler.INSTANCE.closeDBStreams(con, stm, null);
        }
        return true;
    }

}
