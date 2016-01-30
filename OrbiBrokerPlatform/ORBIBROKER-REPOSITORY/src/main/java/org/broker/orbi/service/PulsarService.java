package org.broker.orbi.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.broker.orbi.util.database.DatabaseHandler;

/**
 *
 * @author Christos Paraskeva <ch.paraskeva at gmail dot com>
 */
public class PulsarService {

    public final static String IN_USE = "IN-USE";
    public final static String NOT_USED = "NOT-USED";

    public static boolean editUsedServices(String consumerId, String serviceId, String status) {
        Connection con = DatabaseHandler.INSTANCE.getConnection("pulsar");
        PreparedStatement stm = null;
        try {
            if (IN_USE.equals(status)) {
                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date date = new Date();
                String current_date = dateFormat.format(date);
                stm = con.prepareStatement("INSERT INTO  `used_services` (consumerId,serviceId,lastUsedTimestamp,status) VALUES (?,?,?,?)");
                stm.setString(1, consumerId);
                stm.setString(2, serviceId);
                stm.setString(3, current_date);
                stm.setString(4, status);
                stm.executeUpdate();
            } else if (NOT_USED.equals(status)) {
                stm = con.prepareStatement("UPDATE `used_services` SET status=? WHERE serviceId=?");
                stm.setString(1, status);
                stm.setString(2, serviceId);
                stm.executeUpdate();
            } else {
                Logger.getLogger(PulsarService.class.getName()).warning("Status: " + status + " is not suppoeted..");
            }

        } catch (SQLException ex) {
            Logger.getLogger(PulsarService.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        } finally {
            DatabaseHandler.INSTANCE.closeDBStreams(con, stm, null);
        }
        return true;
    }


}
