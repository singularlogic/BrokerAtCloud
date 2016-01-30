package org.broker.orbi.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.broker.orbi.models.Consumer;
import org.broker.orbi.util.database.DatabaseHandler;

/**
 *
 * @author Christos Paraskeva <ch.paraskeva at gmail dot com>
 */
public class ClientService {

    public static List<Consumer> getAllConsumers() {
        List<Consumer> consumers = new ArrayList<>();

        Connection con = DatabaseHandler.INSTANCE.getDatasource();
        PreparedStatement stm = null;
        ResultSet rs = null;
        try {
            stm = con.prepareStatement("SELECT U.uid,U.username,U.surname,U.name,U.email,U.date_created FROM `User` as U,`UserRole` as UR where U.uid=UR.uid and UR.rolename='enduser'");
            rs = stm.executeQuery();

            while (rs.next()) {
                consumers.add(new Consumer(rs.getInt("uid"), rs.getString("username"), rs.getString("email"), rs.getString("name"), rs.getString("surname"), rs.getString("date_created")));
            }
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseHandler.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } finally {
            DatabaseHandler.INSTANCE.closeDBStreams(con, stm, rs);
        }
        return consumers;
    }


    public static boolean removeConsumer(int consumerID) {
        Connection con = DatabaseHandler.INSTANCE.getDatasource();
        PreparedStatement stm = null;

        try {
            stm = con.prepareStatement("DELETE FROM  `User` WHERE uid=?");
            stm.setInt(1, consumerID);
            stm.executeUpdate();

            stm = con.prepareStatement("DELETE FROM  `UserRole` WHERE uid=?");
            stm.setInt(1, consumerID);
            stm.executeUpdate();

        } catch (SQLException ex) {
            Logger.getLogger(ClientService.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        } finally {
            DatabaseHandler.INSTANCE.closeDBStreams(con, stm, null);
        }
        return true;
    }

}
