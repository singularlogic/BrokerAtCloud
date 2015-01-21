package org.broker.orbi.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.broker.orbi.util.database.DatabaseHandler;
import org.broker.orbi.ui.Consumer;
import org.broker.orbi.ui.Flavor;
import org.broker.orbi.ui.IaaSProvider;
import org.broker.orbi.ui.ImageTemplate;
import org.broker.orbi.ui.Policy;
import org.broker.orbi.ui.PolicyVariable;
import org.broker.orbi.ui.Purchase;
import org.broker.orbi.ui.SPOffer;
import org.broker.orbi.ui.ServiceLevelProfile;
import org.broker.orbi.ui.VariableType;
import org.broker.orbi.util.impl.ZabbixUtil;
import org.broker.orbi.util.openstack.OpenStackIntegration;

/**
 *
 * @author ermis
 */
public class Util {

    /*
     *Fetch Methods
     *
     */
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

    public static List<Policy> getAllPolicies() {
        return getPolicy(0);
    }

    public static List<ImageTemplate> getAllImageTemplates() {
        List<ImageTemplate> imageTemplates = new ArrayList<>();

        Connection con = DatabaseHandler.INSTANCE.getDatasource();
        PreparedStatement stm = null;
        ResultSet rs = null;
        try {

            stm = con.prepareStatement("SELECT * from ImageTemplate");
            rs = stm.executeQuery();

            while (rs.next()) {
                imageTemplates.add(new ImageTemplate(rs.getInt("id"), rs.getString("name"), rs.getString("hex_id")));
            }
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseHandler.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } finally {
            DatabaseHandler.INSTANCE.closeDBStreams(con, stm, rs);
        }
        return imageTemplates;
    }

    public static List<Flavor> getAllFlavors() {
        List<Flavor> flavors = new ArrayList<>();

        Connection con = DatabaseHandler.INSTANCE.getDatasource();
        PreparedStatement stm = null;
        ResultSet rs = null;
        try {

            stm = con.prepareStatement("SELECT * from Flavor");
            rs = stm.executeQuery();

            while (rs.next()) {
                flavors.add(new Flavor(rs.getInt("id"), rs.getString("name")));
            }
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseHandler.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            DatabaseHandler.INSTANCE.closeDBStreams(con, stm, rs);
        }
        return flavors;
    }

    public static List<ServiceLevelProfile> getAllRelatedSLP(String policy_id) {
        List<ServiceLevelProfile> slp = new ArrayList<>();

        Connection con = DatabaseHandler.INSTANCE.getDatasource();
        PreparedStatement stm = null;
        ResultSet rs = null;
        try {

            stm = con.prepareStatement("SELECT SLP.*, P.name as policy_name  FROM `Policy` P,ServiceLevelProfile SLP ,PolicyRelation PR  WHERE  P.id=SLP.policy_id and PR.policy_id=? and SLP.id=PR.slp_id");
            stm.setInt(1, Integer.parseInt(policy_id));
            rs = stm.executeQuery();

            while (rs.next()) {
                slp.add(new ServiceLevelProfile(rs.getInt("id"), rs.getInt("policy_id"), rs.getString("name"), rs.getString("date_created"), rs.getString("policy_name")));
            }
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseHandler.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } finally {
            DatabaseHandler.INSTANCE.closeDBStreams(con, stm, rs);
        }
        return slp;

    }

    public static List<Policy> getAllNonPrimitivePolicies() {
        List<Policy> policies = new ArrayList<>();

        Connection con = DatabaseHandler.INSTANCE.getDatasource();
        PreparedStatement stm = null;
        ResultSet rs = null;
        try {

            stm = con.prepareStatement("SELECT *  FROM `Policy`  WHERE isPrimitive=0");
            rs = stm.executeQuery();

            while (rs.next()) {
                policies.add(new Policy(rs.getInt("id"), rs.getString("name"), rs.getString("date_created"), rs.getString("date_edited")));
            }
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseHandler.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } finally {
            DatabaseHandler.INSTANCE.closeDBStreams(con, stm, rs);
        }
        return policies;

    }

    public static List<Policy> getPolicy(int policyID) {
        List<Policy> policies = new ArrayList<>();
        Connection con = DatabaseHandler.INSTANCE.getDatasource();
        PreparedStatement stm = null;
        ResultSet rs = null;
        try {
            if (policyID > 0) {
                stm = con.prepareStatement("SELECT P . * , count( DISTINCT variable_id ) AS variables , count( DISTINCT profile_id ) AS profiles FROM `Policy` AS P, `VariableProfile` AS VP WHERE P.id = VP.policy_id AND P.id=?  GROUP BY P.id");
                stm.setInt(1, policyID);
            } else {
                stm = con.prepareStatement("SELECT P . * , count( DISTINCT variable_id ) AS variables , count( DISTINCT profile_id ) AS profiles FROM `Policy` AS P, `VariableProfile` AS VP WHERE P.id = VP.policy_id  GROUP BY P.id");
            }
            rs = stm.executeQuery();

            while (rs.next()) {
                policies.add(new Policy(rs.getInt("id"), rs.getString("name"), rs.getString("date_created"), rs.getString("date_edited"), rs.getInt("variables"), rs.getInt("profiles")));
            }
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseHandler.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } finally {
            DatabaseHandler.INSTANCE.closeDBStreams(con, stm, rs);
        }
        return policies;

    }

    public static List<SPOffer> getAllSPOffers() {
        return getAllSPOffers("");
    }

    public static List<SPOffer> getAllSPOffers(String username) {

        List<SPOffer> offers = new ArrayList<>();

        Connection con = DatabaseHandler.INSTANCE.getDatasource();
        PreparedStatement stm = null;
        ResultSet rs = null;
        try {

            //Get all SPOffer for all Service Providers
            if (username.isEmpty()) {
                stm = con.prepareStatement("SELECT  SPO.image_template_id,SPO.flavor_id,SPO.id,SPO.name,SPO.date_created,P.name as policy_name ,SLP.name as offer_name,SPO.username FROM `SPOffer` as SPO,`Policy` as P,`ServiceLevelProfile` as SLP WHERE SPO.policy_id = P.id and SPO.profile_id=SLP.id order by SPO.username");
            } else {
                stm = con.prepareStatement("SELECT SPO.image_template_id,SPO.flavor_id, SPO.id,SPO.name,SPO.date_created,P.name as policy_name ,SLP.name as offer_name,SPO.username FROM `SPOffer` as SPO,`Policy` as P,`ServiceLevelProfile` as SLP WHERE SPO.username=? and SPO.policy_id = P.id and SPO.profile_id=SLP.id");
                stm.setString(1, username);
            }

            rs = stm.executeQuery();

            while (rs.next()) {
                offers.add(new SPOffer(rs.getInt("id"), rs.getString("name"), rs.getString("date_created"), rs.getString("policy_name"), rs.getString("offer_name"), rs.getString("username"), rs.getInt("image_template_id"), rs.getInt("flavor_id")));
            }
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseHandler.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } finally {
            DatabaseHandler.INSTANCE.closeDBStreams(con, stm, rs);
        }
        return offers;
    }

    public static List<SPOffer> getAllPolicySPOffers(int policy_id) {

        List<SPOffer> offers = new ArrayList<>();

        Connection con = DatabaseHandler.INSTANCE.getDatasource();
        PreparedStatement stm = null;
        ResultSet rs = null;
        try {

            //Get all SPOffer for specific Policy ID
            stm = con.prepareStatement("SELECT SPO.id,SPO.name,SPO.date_created,P.name as policy_name ,SLP.name as profile_name,SPO.username FROM `SPOffer` as SPO,`Policy` as P,`ServiceLevelProfile` as SLP WHERE SPO.policy_id=? and SPO.policy_id = P.id and SPO.profile_id=SLP.id");
            stm.setInt(1, policy_id);

            rs = stm.executeQuery();

            while (rs.next()) {
                offers.add(new SPOffer(rs.getInt("id"), rs.getString("name"), rs.getString("date_created"), rs.getString("policy_name"), rs.getString("profile_name"), rs.getString("username")));
            }
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseHandler.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } finally {
            DatabaseHandler.INSTANCE.closeDBStreams(con, stm, rs);
        }
        return offers;
    }

    public static List<PolicyVariable> getAllPolicyProfileVariablesDependOnPolicy(String policy_id, String slp_id) {
        List<PolicyVariable> policyVariables = new ArrayList<>();
        Connection con = DatabaseHandler.INSTANCE.getDatasource();
        PreparedStatement stm = null;
        ResultSet rs = null;
        try {
            if (slp_id.isEmpty()) {
                stm = con.prepareStatement("SELECT PV.id,PV.name,PV.type_id,PV.date_created,VT.name as type_name,PV.metric_unit  FROM  `PolicyRelation` as PR,  `VariableProfile` as VP, `PolicyVariable` as PV, `VariableType` as VT WHERE PV.type_id=VT.id and ( VP.policy_id=?  OR VP.profile_id= PR.slp_id) and  VP.variable_id = PV.id  and PR.policy_id=? group by PV.name");
            } else {
                stm = con.prepareStatement("SELECT PV.id,PV.name,PV.type_id,PV.date_created,VT.name as type_name,PV.metric_unit  FROM  `PolicyRelation` as PR,  `VariableProfile` as VP, `PolicyVariable` as PV, `VariableType` as VT WHERE PV.type_id=VT.id and ( VP.policy_id=?  OR ( VP.profile_id= PR.slp_id and PR.policy_id=?) OR VP.profile_id=?) and  VP.variable_id = PV.id  group by PV.name");
                stm.setInt(3, Integer.parseInt(slp_id));
            }
            stm.setInt(1, Integer.parseInt(policy_id));
            stm.setInt(2, Integer.parseInt(policy_id));
            rs = stm.executeQuery();
            while (rs.next()) {
                policyVariables.add(new PolicyVariable(rs.getInt("id"), rs.getString("name"), rs.getString("type_id"), rs.getString("type_name"), rs.getString("date_created"), rs.getString("metric_unit")));
            }
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseHandler.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            DatabaseHandler.INSTANCE.closeDBStreams(con, stm, rs);
        }

        return policyVariables;

    }

    public static List<PolicyVariable> getPolicyProfileVariables(int policy_id, int profile_id) {
        List<PolicyVariable> policyVariables = new ArrayList<>();

        Connection con = DatabaseHandler.INSTANCE.getDatasource();
        PreparedStatement stm = null;
        ResultSet rs = null;
        try {

            if (policy_id > 0 && profile_id > 0) {
                stm = con.prepareStatement("SELECT PV.*,VT.name as type_name FROM `PolicyVariable` as PV,`VariableProfile` as VP,`VariableType` as VT  WHERE VP.policy_id=? and VP.profile_id=? and VP.variable_id =PV.id and PV.type_id=VT.id");
                stm.setInt(1, policy_id);
                stm.setInt(2, profile_id);
            } else if (policy_id > 0 && profile_id < 1) {
                stm = con.prepareStatement("SELECT PV.id,PV.name,PV.type_id,PV.date_created,VT.name as type_name,PV.metric_unit  FROM `VariableProfile` as VP, `PolicyVariable` as PV, `VariableType` as VT WHERE PV.type_id=VT.id and VP.policy_id=? and  VP.variable_id = PV.id group by PV.name");
                stm.setInt(1, policy_id);
            } else {
                stm = con.prepareStatement("SELECT p.id,p.name,p.type_id,p.date_created,p.metric_unit,pv.name as type_name FROM `PolicyVariable` as p, `VariableType` as pv WHERE p.type_id=pv.id");
            }

            rs = stm.executeQuery();

            while (rs.next()) {
                policyVariables.add(new PolicyVariable(rs.getInt("id"), rs.getString("name"), rs.getString("type_id"), rs.getString("type_name"), rs.getString("date_created"), rs.getString("metric_unit")));
            }
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseHandler.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            DatabaseHandler.INSTANCE.closeDBStreams(con, stm, rs);
        }

        return policyVariables;
    }

    public static List<PolicyVariable> getPolicyProfileVariables() {
        return getPolicyProfileVariables(0, 0);
    }

    public static List<PolicyVariable> getPolicyAllProfilesVariables(int policy_id) {
        return getPolicyProfileVariables(policy_id, 0);
    }

    public static List<ServiceLevelProfile> getServiceLevelProfiles() {
        return getServiceLevelProfiles(0, 0);
    }

    public static List<ServiceLevelProfile> getServiceLevelProfiles(int policy_id, int isPrimitive) {
        List<ServiceLevelProfile> serviceLevelProfiles = new ArrayList<>();

        Connection con = DatabaseHandler.INSTANCE.getDatasource();
        PreparedStatement stm = null;
        ResultSet rs = null;
        try {
            if (policy_id > 0) {
                stm = con.prepareStatement("SELECT SP.*,P.name as policy_name FROM `ServiceLevelProfile` SP, Policy P WHERE SP.policy_id=? and P.id=SP.policy_id ");
                stm.setInt(1, policy_id);
            } else {
                stm = con.prepareStatement("SELECT SP.*,P.name as policy_name FROM `ServiceLevelProfile` SP ,Policy P WHERE P.id = SP.policy_id AND P.isPrimitive=?");
                stm.setInt(1, isPrimitive);
            }
            rs = stm.executeQuery();
            while (rs.next()) {
                serviceLevelProfiles.add(new ServiceLevelProfile(rs.getInt("id"), rs.getInt("policy_id"), rs.getString("name"), rs.getString("date_created"), rs.getString("policy_name")));
            }
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseHandler.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } finally {
            DatabaseHandler.INSTANCE.closeDBStreams(con, stm, rs);
        }

        return serviceLevelProfiles;
    }

    public static Map<String, ArrayList<String>> getPolicyAllSLPVariable(int policyID) {
        Map<String, ArrayList<String>> slpVariables = new HashMap<>();
        Connection con = DatabaseHandler.INSTANCE.getDatasource();
        PreparedStatement stm = null;
        ResultSet rs = null;
        try {
            stm = con.prepareStatement("SELECT SLP.name as profile_name,PV.name as vname FROM `ServiceLevelProfile` as SLP , `VariableProfile` as VP , `PolicyVariable` as PV,`VariableType` as VT WHERE SLP.policy_id=? AND SLP.policy_id =VP.policy_id  AND SLP.id = VP.profile_id  AND VP.variable_id=PV.id AND PV.type_id=VT.id order by SLP.id");
            stm.setInt(1, policyID);
            rs = stm.executeQuery();

            while (rs.next()) {
                String variable_name = rs.getString("vname");
                if (!slpVariables.containsKey(rs.getString("profile_name"))) {
                    ArrayList<String> tmpList = new ArrayList<>();
                    tmpList.add(variable_name);
                    slpVariables.put(rs.getString("profile_name"), tmpList);
                } else {
                    slpVariables.get(rs.getString("profile_name")).add(variable_name);
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseHandler.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } finally {
            DatabaseHandler.INSTANCE.closeDBStreams(con, stm, rs);
        }

        return slpVariables;
    }

    public static List<VariableType> getVariableTypes() {
        List<VariableType> variableTypes = new ArrayList<>();

        Connection con = DatabaseHandler.INSTANCE.getDatasource();
        PreparedStatement stm = null;
        ResultSet rs = null;
        try {
            stm = con.prepareStatement("SELECT * FROM `VariableType`");
            rs = stm.executeQuery();

            while (rs.next()) {
                variableTypes.add(new VariableType(rs.getString("id"), rs.getString("name")));
            }
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseHandler.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } finally {
            DatabaseHandler.INSTANCE.closeDBStreams(con, stm, rs);
        }

        return variableTypes;
    }

    public static SPOffer getSPOfferByID(int spofferID) {

        SPOffer offer = null;

        Connection con = DatabaseHandler.INSTANCE.getDatasource();
        PreparedStatement stm = null;
        ResultSet rs = null;
        try {

            //Get SPOffer            
            stm = con.prepareStatement("SELECT SPO.id,SPO.name,SPO.date_created,P.name as policy_name ,SLP.name as profile_name,SPO.username FROM `SPOffer` as SPO,`Policy` as P,`ServiceLevelProfile` as SLP WHERE SPO.id=? and SPO.policy_id = P.id and SPO.profile_id=SLP.id");
            stm.setInt(1, spofferID);

            rs = stm.executeQuery();

            if (rs.next()) {
                offer = new SPOffer(rs.getInt("id"), rs.getString("name"), rs.getString("date_created"), rs.getString("policy_name"), rs.getString("profile_name"), rs.getString("username"));
            }
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseHandler.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } finally {
            DatabaseHandler.INSTANCE.closeDBStreams(con, stm, rs);
        }
        return offer;
    }

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

    public static List<Consumer> getAllProviders() {
        List<Consumer> consumers = new ArrayList<>();

        Connection con = DatabaseHandler.INSTANCE.getDatasource();
        PreparedStatement stm = null;
        ResultSet rs = null;
        try {
            stm = con.prepareStatement("SELECT U.uid,U.username,U.surname,U.name,U.email,U.date_created FROM `User` as U,`UserRole` as UR where U.uid=UR.uid and UR.rolename='serviceprovider'");
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

    public static List<Purchase> getAllPurchasedSPOffers(int userID) {
        List<Purchase> purchases = new ArrayList<>();

        Connection con = DatabaseHandler.INSTANCE.getDatasource();
        PreparedStatement stm = null;
        ResultSet rs = null;
        try {
            stm = con.prepareStatement("SELECT a.vm_public_ip publicIP,a.id, a.date_purchased, a.spoffer_id, b.policy_id, d.name as policy_name, b.profile_id, c.name as profile_name, b.name as spoffer_name, b.date_created, b.username as provider_username FROM Purchase a, SPOffer b, ServiceLevelProfile c, Policy d WHERE a.spoffer_id = b.id and b.profile_id = c.id and b.policy_id = d.id and a.user_id = ?");
            stm.setInt(1, userID);
            rs = stm.executeQuery();

            while (rs.next()) {
                Map<String, String> variables = ZabbixUtil.getAllPolicyProfileVariablesDependOnOffer(rs.getString("spoffer_id"));
                List<VariableType> listOfVariables = new ArrayList<>();
                for (Map.Entry<String, String> entry : variables.entrySet()) {
                    String tempKey = entry.getKey();
                    String tempValue = entry.getValue();
                    listOfVariables.add(new VariableType(tempKey, tempValue));
                }
                purchases.add(new Purchase(rs.getInt("id"), rs.getInt("spoffer_id"), rs.getInt("policy_id"), rs.getString("policy_name"), rs.getInt("profile_id"), rs.getString("profile_name"), rs.getString("spoffer_name"), rs.getString("date_created"), rs.getString("date_purchased"), rs.getString("provider_username"), listOfVariables,rs.getString("publicIP")));
            }
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseHandler.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } finally {
            DatabaseHandler.INSTANCE.closeDBStreams(con, stm, rs);
        }
        return purchases;
    }

    public static IaaSProvider getIaaSProvider(String username) {
        IaaSProvider iaasProvider = new IaaSProvider();
        Connection con = DatabaseHandler.INSTANCE.getDatasource();
        PreparedStatement stm = null;
        ResultSet rs = null;
        try {
            stm = con.prepareStatement("SELECT * FROM `IaaSProvider` WHERE pid in (select uid from User WHERE username=?);");
            stm.setString(1, username);
            rs = stm.executeQuery();
            if (rs.next()) {
                iaasProvider = new IaaSProvider(rs.getInt("id"), rs.getInt("pid"), rs.getString("endpoint"), rs.getString("tenant_name"), rs.getString("username"), rs.getString("password"),rs.getString("prevention_mechanism_endpoint"));
            }
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseHandler.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } finally {
            DatabaseHandler.INSTANCE.closeDBStreams(con, stm, rs);
        }
        return iaasProvider;
    }


    /*
     *Store Methods
     *
     */
    public static boolean storeBrokerPolicy(int policy_id, String policyName, String policyVariables, String policyProfiles, String variablesPerProfile, String relatedSLP) {

        if (policyVariables.isEmpty() || policyProfiles.isEmpty() || variablesPerProfile.isEmpty()) {
            Logger.getLogger(Util.class.getName()).warning("Aborting storing Broker Policy to database empty values....");
            return false;
        }
        Policy policy = null;
        //Remove policy if already exists!
        if (policy_id > 0) {
            policy = getPolicy(policy_id).get(0);
            System.out.println("Trying to remove Policy with ID: " + policy_id);
            removePolicy(policy_id);
        }

        Map<String, Integer> foreignKeysForPolicyVariables = new HashMap();
        Map<String, Integer> foreignKeysForPolicyProfiles = new HashMap();

        int primaryKeyForPolicy = policy_id;

        Connection con = DatabaseHandler.INSTANCE.getDatasource();
        PreparedStatement stm = null;
        try {
            con.setAutoCommit(false);

            //Store Policy
            try {

                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date date = new Date();
                String current_date = dateFormat.format(date);
                if (policy_id > 0) {

                    stm = con.prepareStatement("INSERT INTO  `Policy` (id,name,date_created,date_edited) VALUES (?,?,?,?)");
                    //Add Foreign key
                    stm.setInt(1, policy_id);
                    stm.setString(2, policyName);
                    stm.setString(3, policy.getDate_created());
                    stm.setString(4, current_date);
                    stm.executeUpdate();

                } else {
                    int isPrimitive = (relatedSLP.isEmpty() ? 1 : 0);
                    stm = con.prepareStatement("INSERT INTO  `Policy` (name,date_created,date_edited,isPrimitive) VALUES (?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
                    //Add Foreign key
                    stm.setString(1, policyName);
                    stm.setString(2, current_date);
                    stm.setString(3, current_date);
                    stm.setInt(4, isPrimitive);
                    stm.executeUpdate();
                    ResultSet rs = stm.getGeneratedKeys();
                    if (rs.next()) {
                        primaryKeyForPolicy = rs.getInt(1);
                    }

                    //Store the related to Policies
                    if (isPrimitive == 0) {
                        String relatedToPoliciesID[] = relatedSLP.split(",");
                        for (String relatedToPolicyID : relatedToPoliciesID) {
                            stm = con.prepareStatement("INSERT INTO  `PolicyRelation` (policy_id,slp_id) VALUES (?,?)");
                            stm.setInt(1, primaryKeyForPolicy);
                            stm.setInt(2, Integer.parseInt(relatedToPolicyID.trim()));
                            stm.executeUpdate();
                        }
                    }
                }
                System.out.println("Primary Key for Policy: " + policyName + ": " + primaryKeyForPolicy);

            } catch (SQLException ex) {
                Logger.getLogger(Util.class.getName()).log(Level.SEVERE, null, ex);
                return false;
            }

            //Store Policy Variables
            String[] policyVaraiablesRows = policyVariables.split("\\|");
            for (String policyVariableRow : policyVaraiablesRows) {
                String[] policyVariableColumns = policyVariableRow.split(",");
                try {
                    stm = con.prepareStatement("INSERT INTO  `PolicyVariable` (name,type_id,metric_unit) VALUES (?,?,?)", Statement.RETURN_GENERATED_KEYS);
                    stm.setString(1, policyVariableColumns[0]);
                    stm.setString(2, policyVariableColumns[1]);
                    stm.setString(3, policyVariableColumns[2]);

                    //Add Foreign key
                    stm.executeUpdate();
                    ResultSet rs = stm.getGeneratedKeys();
                    if (rs.next()) {
                        foreignKeysForPolicyVariables.put(policyVariableColumns[0], rs.getInt(1));
                        System.out.println("Variable: " + policyVariableColumns[0] + " ID: " + rs.getInt(1));
                    }

                } catch (SQLException ex) {
                    Logger.getLogger(Util.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
            System.out.println("Total Foreign keys ForPolicyVariables: " + foreignKeysForPolicyVariables.size());

            //Store Policy Profiles
            String[] policyProfilesRows = policyProfiles.split("\\|");

            for (String policyProfileRow : policyProfilesRows) {
                String[] policyProfileColumns = policyProfileRow.split(",");
                System.out.println("Row: " + policyProfileRow);
                try {
                    stm = con.prepareStatement("INSERT INTO  `ServiceLevelProfile` (policy_id,name,date_created) VALUES (?,?,?)", Statement.RETURN_GENERATED_KEYS);
                    stm.setInt(1, primaryKeyForPolicy);
                    stm.setString(2, policyProfileColumns[0]);
                    stm.setString(3, policyProfileColumns[1]);

                    //Add Foreign key
                    stm.executeUpdate();
                    ResultSet rs = stm.getGeneratedKeys();
                    if (rs.next()) {
                        foreignKeysForPolicyProfiles.put(policyProfileColumns[0], rs.getInt(1));
                    }

                } catch (SQLException ex) {
                    Logger.getLogger(Util.class.getName()).log(Level.SEVERE, null, ex);
                    return false;
                }

            }
            System.out.println("Total Foreign keys ForPolicyProfiles: " + foreignKeysForPolicyProfiles.size());

            //Store VariableProfile Relations
            try {

                String[] VariablesPerProfiles = variablesPerProfile.split("\\|");

                for (String profile : VariablesPerProfiles) {
                    String profileName = profile.split(":")[0];
                    int profileID = foreignKeysForPolicyProfiles.get(profileName);

                    if (profile.split(":").length > 1) {
                        String variables[] = profile.split(":")[1].split(",");
                        for (String variable : variables) {
                            int variableID = foreignKeysForPolicyVariables.get(variable);
                            System.out.println("PolicyID: " + primaryKeyForPolicy + " ProfileID: " + profileID + " VariableID: " + variableID);
                            stm = con.prepareStatement("INSERT INTO  `VariableProfile` (policy_id,profile_id,variable_id) VALUES (?,?,?)");
                            stm.setInt(1, primaryKeyForPolicy);
                            stm.setInt(2, profileID);
                            stm.setInt(3, variableID);
                            stm.executeUpdate();
                        }
                    }
                }
            } catch (SQLException ex) {
                Logger.getLogger(Util.class.getName()).log(Level.SEVERE, null, ex);
                return false;
            }

            //Commit all transactions
            con.commit();
        } catch (SQLException ex) {
            Logger.getLogger(Util.class.getName()).log(Level.SEVERE, null, ex);
            return false;
            //Close all Streams  
        } finally {
            DatabaseHandler.INSTANCE.closeDBStreams(con, stm, null);
        }

        return true;
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
                Logger.getLogger(Util.class.getName()).log(Level.SEVERE, null, ex);
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
                Logger.getLogger(Util.class.getName()).log(Level.SEVERE, null, ex);
                return false;
            }

            //Commit all transactions
            con.commit();
        } catch (SQLException ex) {
            Logger.getLogger(Util.class.getName()).log(Level.SEVERE, null, ex);
            return false;

            //Close all Streams  
        } finally {
            DatabaseHandler.INSTANCE.closeDBStreams(con, stm, null);
        }
        return true;
    }

    public static boolean purchaseServiceOffering(String username, int offerID, String publicIP, int zabbixHostID, String serverID) {

        // Steps 
        // Call Openstack Library to create the new VM
        // Register the new host to the ZABBIX Database
        // Insert user purchase in Broker DB    
        int userID = getUserID(username);

        Connection con = DatabaseHandler.INSTANCE.getDatasource();
        PreparedStatement stm = null;
        try {

            stm = con.prepareStatement("INSERT INTO  `Purchase` (user_id, spoffer_id, vm_public_ip, zabbix_host_id,serverID) VALUES (?,?,?,?,?)");
            //Add Foreign key
            stm.setInt(1, userID);
            stm.setInt(2, offerID);
            stm.setString(3, publicIP);
            stm.setInt(4, zabbixHostID);
            stm.setString(5, serverID);
            stm.executeUpdate();

        } catch (SQLException ex) {
            Logger.getLogger(Util.class.getName()).log(Level.SEVERE, null, ex);
            return false;
            //Close all Streams  
        } finally {
            DatabaseHandler.INSTANCE.closeDBStreams(con, stm, null);
        }
        return true;
    }

    public static Map<String, String> getOpenStackIntegrationDetails(String offerID) {
        Map<String, String> openstackIntegrationDetails = new HashMap<>();

        Connection con = DatabaseHandler.INSTANCE.getDatasource();
        PreparedStatement stm = null;
        ResultSet rs = null;
        try {

            stm = con.prepareStatement("SELECT IM.hex_id ,IP.pid as provider_id, IP.endpoint,IP.tenant_name as tenant,IP.username,IP.password,O.image_template_id as templateid, O.flavor_id as flavorid , O.name as offer_name FROM `User` U, `SPOffer` O ,`IaaSProvider` IP, `ImageTemplate` IM  WHERE O.username=U.username and O.id=? and IP.pid=U.uid AND IM.id=O.image_template_id limit 1");
            stm.setInt(1, Integer.parseInt(offerID));
            rs = stm.executeQuery();
            if (rs.next()) {
                openstackIntegrationDetails.put("username", rs.getString("username"));
                openstackIntegrationDetails.put("password", rs.getString("password"));
                openstackIntegrationDetails.put("tenant", rs.getString("tenant"));
                openstackIntegrationDetails.put("endpoint", rs.getString("endpoint"));
                openstackIntegrationDetails.put("flavorid", rs.getString("flavorid"));
                openstackIntegrationDetails.put("templateid", rs.getString("hex_id"));
                openstackIntegrationDetails.put("instancename", "User" + rs.getString("offer_name") + "Provider" + rs.getString("provider_id"));
                //TODO: Handle internal IP
                openstackIntegrationDetails.put("internalIP", "192.168.10.110");
                System.out.println("OpenStack Integration Details\n\nusername: " + openstackIntegrationDetails.get("username") + "\n"
                        + "password: " + openstackIntegrationDetails.get("password") + "\n"
                        + "tenant: " + openstackIntegrationDetails.get("tenant") + "\n"
                        + "endpoint: " + openstackIntegrationDetails.get("endpoint") + "\n"
                        + "flavorid: " + openstackIntegrationDetails.get("flavorid") + "\n"
                        + "templateid: " + openstackIntegrationDetails.get("templateid") + "\n"
                        + "instancename: " + openstackIntegrationDetails.get("instancename") + "\n"
                        + "internalIP: " + openstackIntegrationDetails.get("internalIP") + "\n"
                );
            }

        } catch (SQLException ex) {
            Logger.getLogger(Util.class.getName()).log(Level.SEVERE, null, ex);
            //Close all Streams  
        } finally {
            DatabaseHandler.INSTANCE.closeDBStreams(con, stm, null);
        }

        //
        return openstackIntegrationDetails;
    }

    public static boolean registerNewUser(String username, String name, String surname, String email, String password, String role) {

        Connection con = DatabaseHandler.INSTANCE.getDatasource();
        PreparedStatement stm = null;
        int primaryKeyUID = 0;
        try {
            con.setAutoCommit(false);
            //Store Policy
            try {
                stm = con.prepareStatement("INSERT INTO User (username, name, surname, password, email) VALUES (?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
                //Add Foreign key
                stm.setString(1, username);
                stm.setString(2, name);
                stm.setString(3, surname);
                stm.setString(4, password);
                stm.setString(5, email);
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
                Logger.getLogger(Util.class.getName()).log(Level.SEVERE, null, ex);
                return false;
            }

        } catch (SQLException ex) {
            Logger.getLogger(Util.class.getName()).log(Level.SEVERE, null, ex);
            return false;

            //Close all Streams  
        } finally {
            DatabaseHandler.INSTANCE.closeDBStreams(con, stm, null);
        }

        return true;
    }

    /**
     * Remove Methods
     *
     */
    public static boolean removePolicy(int policyID) {
        Connection con = DatabaseHandler.INSTANCE.getDatasource();
        PreparedStatement stm = null;

        try {
            stm = con.prepareStatement("DELETE FROM `PolicyVariable` WHERE id IN (SELECT  distinct variable_id FROM `VariableProfile` where policy_id=?)");
            stm.setInt(1, policyID);
            stm.executeUpdate();

            stm = con.prepareStatement("DELETE FROM `Policy` WHERE id=?");
            stm.setInt(1, policyID);
            stm.executeUpdate();

        } catch (SQLException ex) {
            Logger.getLogger(Util.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        } finally {
            DatabaseHandler.INSTANCE.closeDBStreams(con, stm, null);
        }
        return true;
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
            Logger.getLogger(Util.class.getName()).log(Level.SEVERE, null, ex);
            return false;
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
            Logger.getLogger(Util.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        } finally {
            DatabaseHandler.INSTANCE.closeDBStreams(con, stm, null);
        }
        return true;
    }

    public static boolean removeSPOffer(int offerID) {
        Connection con = DatabaseHandler.INSTANCE.getDatasource();
        PreparedStatement stm = null;

        try {
            stm = con.prepareStatement("DELETE FROM  `SPOffer` WHERE id=?");
            stm.setInt(1, offerID);
            stm.executeUpdate();

        } catch (SQLException ex) {
            Logger.getLogger(Util.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        } finally {
            DatabaseHandler.INSTANCE.closeDBStreams(con, stm, null);
        }
        return true;
    }

    public static boolean removePurchase(int purchaseID) {
        Connection con = DatabaseHandler.INSTANCE.getDatasource();
        PreparedStatement stm = null;

        try {
            String serverID = "";
            String offerID = "";
            int hostID = 0;
            String username = "";
            String password = "";
            String tenant_name = "";
            String endpoint = "";

            stm = con.prepareStatement("select  IP.*, P.serverID,P.spoffer_id,P.zabbix_host_id FROM  `Purchase` P,`IaaSProvider` IP, `SPOffer` O WHERE P.id=? AND O.id=P.spoffer_id AND IP.pid in (select U.uid from User U where username = O.username) limit 1");
            stm.setInt(1, purchaseID);
            ResultSet rs = stm.executeQuery();
            if (rs.next()) {
                serverID = rs.getString("serverID");
                offerID = rs.getString("spoffer_id");
                hostID = rs.getInt("zabbix_host_id");
                username = rs.getString("username");
                password = rs.getString("password");
                tenant_name = rs.getString("tenant_name");
                endpoint = rs.getString("endpoint");
            }

            OpenStackIntegration osi = new OpenStackIntegration();
            osi.destroyInstance(endpoint, username, password, tenant_name, serverID);

            // Remove From ZABBIX Server
            boolean isSuccess = ZabbixUtil.deleteZabbixHost(hostID);

            stm = con.prepareStatement("DELETE FROM `Purchase` WHERE id=?");
            stm.setInt(1, purchaseID);
            stm.executeUpdate();

        } catch (SQLException ex) {
            Logger.getLogger(Util.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        } finally {
            DatabaseHandler.INSTANCE.closeDBStreams(con, stm, null);
        }
        return true;
    }

    public static boolean validateVariables(String validationObject) {

        boolean allOK = false;
        String[] validationArray = validationObject.split("\\^");
        for (String tempRule : validationArray) {

            String[] ruleArray = tempRule.split("\\|");
            String type = ruleArray[2].substring(4);
            String value = ruleArray[1];

            // ^[-+]?\d+$ Integer
            // ^[a-zA-Z0-9_]*$ String
            //  ^[-+]?[0-9]*\.?[0-9]+$ Float
            if (type.contains("Integer")) {
                allOK = value.matches("[-+]?\\d+");
                if (!allOK) {
                    break;
                }
            } else if (type.contains("String")) {
                allOK = value.matches("[a-zA-Z0-9_]*");
                if (!allOK) {
                    break;
                }
            } else if (type.contains("Float")) {
                allOK = value.matches("[-+]?[0-9]*\\.?[0-9]+");
                if (!allOK) {
                    break;
                }
            } else {
                // Enumeration
            }

        }

        return allOK;
    }

    /*
     *Update Methods
     *
     */
    public static boolean updateSPOfferDetails(String offer_id, String column, String value) {
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

    public static boolean modifyIaaSProviderDetails(int iaas_id, String endpoint, String tenant_name, String username, String password, String failure_end_point) {
        boolean isSuccess = false;
        Connection con = DatabaseHandler.INSTANCE.getDatasource();
        PreparedStatement stm = null;

        //Basic Validation for Input
        if (endpoint.isEmpty() || tenant_name.isEmpty() || username.isEmpty() || password.isEmpty()) {
            return isSuccess;
        }
        try {
            //Update existing record
            if (iaas_id > 0) {
                stm = con.prepareStatement("UPDATE `IaaSProvider` SET endpoint=?,tenant_name=?,username=?,password=?,prevention_mechanism_endpoint=? WHERE id=?");
                stm.setString(1, endpoint);
                stm.setString(2, tenant_name);
                stm.setString(3, username);
                stm.setString(4, password);
                stm.setString(5, failure_end_point);
                stm.setInt(6, iaas_id);
                stm.executeUpdate();
                isSuccess = !isSuccess;
            } //New Record
            else {
                int providerID = getUserID(username);
                stm = con.prepareStatement("INSERT INTO `IaaSProvider` (`pid`, `endpoint`, `tenant_name`, `username`, `password`) VALUES ( ?,?,?,?,?,?)");
                stm.setInt(1, providerID);
                stm.setString(2, endpoint);
                stm.setString(3, tenant_name);
                stm.setString(4, username);
                stm.setString(5, password);
                stm.setString(6, failure_end_point);
                stm.executeUpdate();
                isSuccess = !isSuccess;
            }
        } catch (SQLException ex) {
            Logger.getLogger(DatabaseHandler.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            DatabaseHandler.INSTANCE.closeDBStreams(con, stm, null);
        }
        return isSuccess;
    }

}
