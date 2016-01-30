package org.broker.orbi.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.broker.orbi.models.Consumer;
import org.broker.orbi.util.database.DatabaseHandler;

/**
 *
 * @author Christos Paraskeva <ch.paraskeva at gmail dot com>
 */
public class ProviderService {
    
    public static List<Consumer> getAllProviders() {
        List<Consumer> consumers = new ArrayList<>();
        
        Connection con = DatabaseHandler.INSTANCE.getDatasource();
        PreparedStatement stm = null;
        ResultSet rs = null;
        try {
            
            stm = con.prepareStatement("SELECT GROUP_CONCAT(O.name) as offers, U.uid,U.username,U.surname,U.name,U.email,U.date_created FROM `User` as U,`UserRole` as UR,`SPOffer` as O where U.uid=UR.uid and UR.rolename='serviceprovider' and O.username = U.username group by uid");
//stm = con.prepareStatement("SELECT U.uid,U.username,U.surname,U.name,U.email,U.date_created FROM `User` as U,`UserRole` as UR where U.uid=UR.uid and UR.rolename='serviceprovider'");
            rs = stm.executeQuery();
            
            while (rs.next()) {
                consumers.add(new Consumer(rs.getInt("uid"), rs.getString("username"), rs.getString("email"), rs.getString("name"), rs.getString("surname"), rs.getString("date_created"), rs.getString("offers")));
            }
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseHandler.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } finally {
            DatabaseHandler.INSTANCE.closeDBStreams(con, stm, rs);
        }
        return consumers;
    }
    
    public static boolean storeServiceProviderOffer(int policy_id, int profile_id, String offer_name, String username, String offer_parameters) {
        int primaryKeyForSPOffer = 0;
        
        Connection con = DatabaseHandler.INSTANCE.getDatasource();
        PreparedStatement stm = null;
        try {
            con.setAutoCommit(false);

            //Store Policy
            try {
                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date date = new Date();
                String current_date = dateFormat.format(date);
                
                stm = con.prepareStatement("INSERT INTO  `SPOffer` (policy_id,profile_id,name,date_created,username) VALUES (?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
                //Add Foreign key
                stm.setInt(1, policy_id);
                stm.setInt(2, profile_id);
                stm.setString(3, offer_name);
                stm.setString(4, current_date);
                stm.setString(5, username);
                stm.executeUpdate();
                ResultSet rs = stm.getGeneratedKeys();
                if (rs.next()) {
                    primaryKeyForSPOffer = rs.getInt(1);
                }
//                System.out.println("Primary Key for SPOffer: " + offer_name + ": " + primaryKeyForSPOffer);

            } catch (SQLException ex) {
                Logger.getLogger(ProviderService.class.getName()).log(Level.SEVERE, null, ex);
                return false;
            }
            
            if (primaryKeyForSPOffer < 1) {
                return false;
            }

            //Store VariableProfile Relations
            try {
                
                String[] paramsPerVariable = offer_parameters.split("\\^");
                
                for (String parameter : paramsPerVariable) {
                    int variableID = Integer.parseInt(parameter.split("\\|")[0]);
                    String variableValue = parameter.split("\\|")[1];
//                    System.out.println("OfferID: " + primaryKeyForSPOffer + "PolicyID: " + policy_id + " ProfileID: " + profile_id + " VariableID: " + variableID + " VariableValue: " + variableValue);
                    stm = con.prepareStatement("INSERT INTO  `SPOfferDetails` (offer_id,variable_id,variable_value) VALUES (?,?,?)");
                    stm.setInt(1, primaryKeyForSPOffer);
                    stm.setInt(2, variableID);
                    stm.setString(3, variableValue);
                    stm.executeUpdate();
                }
            } catch (SQLException ex) {
                Logger.getLogger(ProviderService.class.getName()).log(Level.SEVERE, null, ex);
                return false;
            }

            //Commit all transactions
            con.commit();
        } catch (SQLException ex) {
            Logger.getLogger(ProviderService.class.getName()).log(Level.SEVERE, null, ex);
            return false;

            //Close all Streams  
        } finally {
            DatabaseHandler.INSTANCE.closeDBStreams(con, stm, null);
        }
        return true;
    }
    
    public static boolean removeProvider(int providerID) {
        Connection con = DatabaseHandler.INSTANCE.getDatasource();
        PreparedStatement stm = null;
        
        try {
            stm = con.prepareStatement("DELETE FROM  `User` WHERE uid=?");
            stm.setInt(1, providerID);
            stm.executeUpdate();
            
            stm = con.prepareStatement("DELETE FROM  `UserRole` WHERE uid=?");
            stm.setInt(1, providerID);
            stm.executeUpdate();
            
        } catch (SQLException ex) {
            Logger.getLogger(ProviderService.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        } finally {
            DatabaseHandler.INSTANCE.closeDBStreams(con, stm, null);
        }
        return true;
    }
    
}
