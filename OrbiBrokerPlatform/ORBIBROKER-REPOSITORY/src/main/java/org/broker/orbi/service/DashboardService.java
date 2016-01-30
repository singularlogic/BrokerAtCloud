package org.broker.orbi.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.broker.orbi.models.AdminDashboardInfo;
import org.broker.orbi.models.Consumer;
import org.broker.orbi.models.ProviderDashboardInfo;
import org.broker.orbi.util.database.DatabaseHandler;

/**
 *
 * @author Christos Paraskeva <ch.paraskeva at gmail dot com>
 */
public class DashboardService {

    public static AdminDashboardInfo getAdminDashboardInfo() {
        Connection con = DatabaseHandler.INSTANCE.getDatasource();
        PreparedStatement stm = null;
        ResultSet rs = null;
        AdminDashboardInfo dashboardInfo = new AdminDashboardInfo();

        try {
            stm = con.prepareStatement("select count(P.id) policies, (select count(*) from UserRole where rolename =?)  as providers, (select count(*) from UserRole where rolename =?) as clients from  Policy P ");

            stm.setString(1, "serviceprovider");
            stm.setString(2, "enduser");
            rs = stm.executeQuery();
            if (rs.next()) {
                dashboardInfo = new AdminDashboardInfo(rs.getString("policies"), rs.getString("providers"), rs.getString("clients"));
            }
        } catch (SQLException ex) {
            Logger.getLogger(DashboardService.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } finally {
            DatabaseHandler.INSTANCE.closeDBStreams(con, stm, rs);
        }
        return dashboardInfo;
    }

    public static ProviderDashboardInfo getProviderDashboardInfo(String username) {
        Connection con = DatabaseHandler.INSTANCE.getDatasource();
        PreparedStatement stm = null;
        ResultSet rs = null;
        ProviderDashboardInfo dashboardInfo = new ProviderDashboardInfo();
        int providerID = UserService.getUserID(username);
        try {
            stm = con.prepareStatement("select count(SD.id) servicedescs, (select count(*) from Flavor where pid=? ) flavors,(select count(*) from ImageTemplate where uid=? ) images, (select count(*) from SPOffer where username = ?) offers from  ServiceDescription SD where SD.pid=?");
            stm.setInt(1, providerID);
            stm.setInt(2, providerID);
            stm.setString(3, username);
            stm.setInt(4, providerID);
            rs = stm.executeQuery();
            if (rs.next()) {
                dashboardInfo = new ProviderDashboardInfo(rs.getString("servicedescs"), rs.getString("flavors"), rs.getString("images"), rs.getString("offers"));
            }
        } catch (SQLException ex) {
            Logger.getLogger(DashboardService.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } finally {
            DatabaseHandler.INSTANCE.closeDBStreams(con, stm, rs);
        }
        return dashboardInfo;
    }

}
