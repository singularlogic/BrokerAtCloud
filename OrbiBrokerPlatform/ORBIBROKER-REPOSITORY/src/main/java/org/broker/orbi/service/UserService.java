package org.broker.orbi.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.broker.orbi.models.Consumer;
import org.broker.orbi.util.database.DatabaseHandler;

/**
 *
 * @author Christos Paraskeva <ch.paraskeva at gmail dot com>
 */
public class UserService {

    public static boolean registerNewUser(String username, String name, String surname, String email, String password, String role, String plain_password) {

//        Connection con = DatabaseHandler.INSTANCE.getDatasource();
        Connection con = DatabaseHandler.INSTANCE.getConnection("");
        PreparedStatement stm = null;
        int primaryKeyUID = 0;
        try {
            con.setAutoCommit(false);
            //Store Policy
            try {
                stm = con.prepareStatement("INSERT INTO User (username, name, surname, password, email,password_plain) VALUES (?,?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
                //Add Foreign key
                stm.setString(1, username);
                stm.setString(2, name);
                stm.setString(3, surname);
                stm.setString(4, password);
                stm.setString(5, email);
                stm.setString(6, plain_password);
                stm.executeUpdate();
                ResultSet rs = stm.getGeneratedKeys();
                if (rs.next()) {
                    primaryKeyUID = rs.getInt(1);
                }

                System.out.println("Primary Key for User: " + username + ": " + primaryKeyUID);

                //Store User Roles
                stm = con.prepareStatement("INSERT INTO UserRole (rolename,username,uid) VALUES (?,?,?)");
                stm.setString(1, role);
                stm.setString(2, username);
                stm.setInt(3, primaryKeyUID);

                stm.executeUpdate();

                //Commit all transactions
                con.commit();

            } catch (SQLException ex) {
                Logger.getLogger(UserService.class.getName()).log(Level.SEVERE, null, ex);
                return false;
            }

        } catch (SQLException ex) {
            Logger.getLogger(UserService.class.getName()).log(Level.SEVERE, null, ex);
            return false;

            //Close all Streams  
        } finally {
            DatabaseHandler.INSTANCE.closeDBStreams(con, stm, null);
        }

        return true;
    }

//        public static Consumer getUser(String username) {
//        Consumer consumer = null;
//
//        Connection con = DatabaseHandler.INSTANCE.getDatasource();
//        PreparedStatement stm = null;
//        ResultSet rs = null;
//        try {
//            stm = con.prepareStatement("SELECT U.uid,U.username,U.surname,U.name,U.email,U.date_created FROM `User` as U,`UserRole` as UR where U.uid=UR.uid and UR.rolename='enduser'");
//            rs = stm.executeQuery();
//
//            if (rs.next()) {
//                consumer = new Consumer(rs.getInt("uid"), rs.getString("username"), rs.getString("email"), rs.getString("name"), rs.getString("surname"), rs.getString("date_created"));
//            }
//        } catch (SQLException ex) {
//            Logger.getLogger(DatabaseHandler.class.getName()).log(Level.SEVERE, null, ex);
//            return null;
//        } finally {
//            DatabaseHandler.INSTANCE.closeDBStreams(con, stm, rs);
//        }
//        return consumer;
//    }
    public static Consumer getUser(String username) {
        Consumer consumer = null;

        Connection con = DatabaseHandler.INSTANCE.getDatasource();
        PreparedStatement stm = null;
        ResultSet rs = null;
        try {
            stm = con.prepareStatement("SELECT * FROM `User` as U WHERE username=?");
            stm.setString(1, username);
            rs = stm.executeQuery();

            if (rs.next()) {
                consumer = new Consumer(rs.getInt("uid"), rs.getString("username"), rs.getString("email"), rs.getString("name"), rs.getString("surname"), rs.getString("date_created"));
            }
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseHandler.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } finally {
            DatabaseHandler.INSTANCE.closeDBStreams(con, stm, rs);
        }
        return consumer;
    }

    public static String getUserPassword(String username) {
        String password = "";
        Connection con = DatabaseHandler.INSTANCE.getDatasource();
        PreparedStatement stm = null;
        ResultSet rs = null;
        try {
            stm = con.prepareStatement("SELECT password_plain from User WHERE username=?");
            stm.setString(1, username);
            rs = stm.executeQuery();
            if (rs.next()) {
                password = rs.getString("password_plain");
            }

        } catch (SQLException ex) {
            Logger.getLogger(DatabaseHandler.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            DatabaseHandler.INSTANCE.closeDBStreams(con, stm, rs);
        }
        return password;
    }

    public static int getUserID(String username) {
        int userID = 0;
        Connection con = DatabaseHandler.INSTANCE.getDatasource();
        PreparedStatement stm = null;
        ResultSet rs = null;
        try {
            stm = con.prepareStatement("SELECT uid from User WHERE username=?");
            stm.setString(1, username);
            rs = stm.executeQuery();
            if (rs.next()) {
                userID = rs.getInt("uid");
            }

        } catch (SQLException ex) {
            Logger.getLogger(DatabaseHandler.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            DatabaseHandler.INSTANCE.closeDBStreams(con, stm, rs);
        }
        return userID;
    }

    public static String getUserRole(String username) {
        String role = "";
        Connection con = DatabaseHandler.INSTANCE.getDatasource();
        PreparedStatement stm = null;
        ResultSet rs = null;
        try {
            stm = con.prepareStatement("SELECT rolename from UserRole WHERE username=?");
            stm.setString(1, username);
            rs = stm.executeQuery();

            if (rs.next()) {
                role = rs.getString("rolename");
            }
            System.out.println("Getting role for username: " + username + " , role is: " + role);

        } catch (SQLException ex) {
            Logger.getLogger(DatabaseHandler.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } finally {
            DatabaseHandler.INSTANCE.closeDBStreams(con, stm, rs);
        }

        return role;
    }

}
