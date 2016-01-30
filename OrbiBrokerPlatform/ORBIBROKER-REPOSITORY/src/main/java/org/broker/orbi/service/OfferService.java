package org.broker.orbi.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.broker.orbi.models.Offer;
import org.broker.orbi.util.database.DatabaseHandler;

/**
 *
 * @author Christos Paraskeva <ch.paraskeva at gmail dot com>
 */
public class OfferService {

    public static List<Offer> getAllOffers() {
        return getAllOffers("");
    }

    public static List<Offer> getAllOffers(String username) {

        List<Offer> offers = new ArrayList<>();

        Connection con = DatabaseHandler.INSTANCE.getDatasource();
        PreparedStatement stm = null;
        ResultSet rs = null;
        try {

            //Get all SPOffer for all Service Providers
            if (username.isEmpty()) {
                stm = con.prepareStatement("SELECT SPO.iaas_id,SPO.image_template_id,SPO.flavor_id,SPO.id,SPO.name,SPO.date_created,P.name as policy_name ,SLP.name as offer_name,SPO.username, F.name as flavor, IT.name as image_template, IP.name as iaas FROM `SPOffer` as SPO,`Policy` as P,`ServiceDescription` as SLP, IaaSProvider as IP, ImageTemplate as IT, Flavor F WHERE SPO.policy_id = P.id and SPO.profile_id=SLP.id and IP.id = SPO.iaas_id AND IT.id = SPO.image_template_id AND F.id = SPO.flavor_id order by SPO.username");

//                stm = con.prepareStatement("SELECT SPO.iaas_id,SPO.image_template_id,SPO.flavor_id,SPO.id,SPO.name,SPO.date_created,P.name as policy_name ,SLP.name as offer_name,SPO.username FROM `SPOffer` as SPO,`Policy` as P,`ServiceDescription` as SLP WHERE SPO.policy_id = P.id and SPO.profile_id=SLP.id order by SPO.username");
            } else {
                stm = con.prepareStatement("SELECT SPO.iaas_id,SPO.image_template_id,SPO.flavor_id,SPO.id,SPO.name,SPO.date_created,P.name as policy_name ,SLP.name as offer_name,SPO.username, F.name as flavor, IT.name as image_template, IP.name as iaas FROM `SPOffer` as SPO,`Policy` as P,`ServiceDescription` as SLP, IaaSProvider as IP, ImageTemplate as IT, Flavor F WHERE SPO.username=? AND SPO.policy_id = P.id and SPO.profile_id=SLP.id and IP.id = SPO.iaas_id AND IT.id = SPO.image_template_id AND F.id = SPO.flavor_id order by SPO.username");
//                stm = con.prepareStatement("SELECT SPO.iaas_id,SPO.image_template_id,SPO.flavor_id, SPO.id,SPO.name,SPO.date_created,P.name as policy_name ,SLP.name as offer_name,SPO.username FROM `SPOffer` as SPO,`Policy` as P,`ServiceDescription` as SLP WHERE SPO.username=? and SPO.policy_id = P.id and SPO.profile_id=SLP.id");
                stm.setString(1, username);
            }
            rs = stm.executeQuery();
            while (rs.next()) {
                offers.add(new Offer(rs.getInt("id"), rs.getString("name"), rs.getString("date_created"), rs.getString("policy_name"), rs.getString("offer_name"), rs.getString("username"), rs.getInt("image_template_id"), rs.getInt("flavor_id"), rs.getInt("iaas_id")));
                offers.get(offers.size() - 1).setFlavor(rs.getString("flavor"));
                offers.get(offers.size() - 1).setImageTemplate(rs.getString("image_template"));
                offers.get(offers.size() - 1).setIaasProvider(rs.getString("iaas"));
            }
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseHandler.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } finally {
            DatabaseHandler.INSTANCE.closeDBStreams(con, stm, rs);
        }
        return offers;
    }

    public static Offer getOfferByServiceDescriptionFullname(String servicedesFullname) {
        Offer offer = new Offer();

        Connection con = DatabaseHandler.INSTANCE.getDatasource();
        PreparedStatement stm = null;
        ResultSet rs = null;
        try {
            stm = con.prepareStatement("SELECT * FROM SPOffer WHERE profile_id=(select id from ServiceDescription WHERE full_name=? limit 1)");
            stm.setString(1, servicedesFullname);
            rs = stm.executeQuery();
            if (rs.next()) {
                offer = (new Offer(rs.getInt("id"), rs.getString("name"), rs.getString("date_created"), "", rs.getString("name"), "", rs.getInt("image_template_id"), rs.getInt("flavor_id"), rs.getInt("iaas_id")));
            }
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseHandler.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } finally {
            DatabaseHandler.INSTANCE.closeDBStreams(con, stm, rs);
        }
        return offer;
    }

    public static boolean updateOfferDetails(String offer_id, String column, String value) {
        boolean isSuccess = false;
        Connection con = DatabaseHandler.INSTANCE.getDatasource();
        PreparedStatement stm = null;

        if (column.endsWith("it")) {
            column = "image_template_id";
        } else if (column.endsWith("flavor")) {
            column = "flavor_id";
        } else {
            return isSuccess;
        }

        try {
            stm = con.prepareStatement("UPDATE SPOffer set " + column + "=? WHERE id=?");
            stm.setInt(1, Integer.parseInt(value));
            stm.setInt(2, Integer.parseInt(offer_id));
            stm.executeUpdate();
            isSuccess = !isSuccess;
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseHandler.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            DatabaseHandler.INSTANCE.closeDBStreams(con, stm, null);
        }

        return isSuccess;
    }

    public static boolean storeOffer(Offer offer, String username) {
        boolean isSuccess = false;
        Connection con = DatabaseHandler.INSTANCE.getDatasource();
        PreparedStatement stm = null;

        try {
            stm = con.prepareStatement("INSERT INTO SPOffer (policy_id, profile_id, name, username, image_template_id, flavor_id, iaas_id) VALUES (?, ?, ?, ?, ?, ?, ?);");
            stm.setInt(1, offer.getPolicy_id());
            stm.setInt(2, offer.getService_description_id());
            stm.setString(3, offer.getName());
            stm.setString(4, username);
            stm.setInt(5, offer.getImage_template_id());
            stm.setInt(6, offer.getFlavor_id());
            stm.setInt(7, offer.getIaas_id());
            stm.executeUpdate();
            isSuccess = !isSuccess;
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseHandler.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            DatabaseHandler.INSTANCE.closeDBStreams(con, stm, null);
        }

        return isSuccess;
    }

    public static List<Offer> getAllPolicyOffers(int policy_id) {

        List<Offer> offers = new ArrayList<>();

        Connection con = DatabaseHandler.INSTANCE.getDatasource();
        PreparedStatement stm = null;
        ResultSet rs = null;
        try {

            //Get all SPOffer for specific Policy ID
            stm = con.prepareStatement("SELECT SPO.iaas_id,SPO.id,SPO.name,SPO.date_created,P.name as policy_name ,SLP.name as profile_name,SPO.username FROM `SPOffer` as SPO,`Policy` as P,`ServiceDescription` as SLP WHERE SPO.policy_id = P.id and SPO.profile_id=SLP.id");
            rs = stm.executeQuery();

            while (rs.next()) {
                offers.add(new Offer(rs.getInt("id"), rs.getString("name"), rs.getString("date_created"), rs.getString("policy_name"), rs.getString("profile_name"), rs.getString("username")));
            }
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseHandler.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } finally {
            DatabaseHandler.INSTANCE.closeDBStreams(con, stm, rs);
        }
        return offers;
    }

    public static boolean removeOffer(int offerID) {
        Connection con = DatabaseHandler.INSTANCE.getDatasource();
        PreparedStatement stm = null;

        try {
            stm = con.prepareStatement("DELETE FROM  `SPOffer` WHERE id=?");
            stm.setInt(1, offerID);
            stm.executeUpdate();

        } catch (SQLException ex) {
            Logger.getLogger(OfferService.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        } finally {
            DatabaseHandler.INSTANCE.closeDBStreams(con, stm, null);
        }
        return true;
    }

    public static Offer getOffer(String offerID) {
        Offer offer = null;
        Connection con = DatabaseHandler.INSTANCE.getDatasource();
        PreparedStatement stm = null;
        ResultSet rs = null;
        try {
            //Get SPOffer            
            stm = con.prepareStatement("SELECT  SPO.id,SPO.name,SPO.date_created,P.name as policy_name ,SLP.name as profile_name,SPO.username FROM `SPOffer` as SPO,`Policy` as P,`ServiceDescription` as SLP WHERE SPO.id=? and SPO.policy_id = P.id and SPO.profile_id=SLP.id");
            stm.setInt(1, Integer.parseInt(offerID));
            rs = stm.executeQuery();
            if (rs.next()) {
                offer = new Offer(rs.getInt("id"), rs.getString("name"), rs.getString("date_created"), rs.getString("policy_name"), rs.getString("profile_name"), rs.getString("username"));
            }
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseHandler.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } finally {
            DatabaseHandler.INSTANCE.closeDBStreams(con, stm, rs);
        }
        return offer;
    }

}
