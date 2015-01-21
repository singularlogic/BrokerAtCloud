package org.broker.orbi.util.database;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

/**
 * Singleton pattern example using Java Enumeration
 */
public enum DatabaseHandler {

    INSTANCE;

    private DataSource zabbixDatasource = null;
    private DataSource brokerDatasource = null;
    private Connection connection = null;

    private final String DEVELOPMENT_PROFILE = "development";
    private final String DEPLOYMENT_PROFILE = "deployment";
    private final Properties prop = new Properties();

    //All fields are readen from datasource.properties file
    private final String ZabbixDSName;
    private final String BrokerDSName;
    private final String ZabbixDBName;
    private final String BrokerDBName;
    private final String ZabbixDBUser;
    private final String BrokerDBUser;
    private final String ZabbixDBPass;
    private final String BrokerDBPass;
    private final String DBPort;
    private final String ZabbixHost;
    private final String BrokerHost;
    private final String ZabbixProfile;
    private final String BrokerProfile;
    private final String ZabbixDBURL;
    private final String BrokerDBURL;

    DatabaseHandler() {
        try {
            prop.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("datasource.properties"));
        } catch (IOException ex) {
            Logger.getLogger(DatabaseHandler.class.getName()).severe("Couldn't read properties file...");
        }

        this.ZabbixDSName = prop.getProperty("ZABBIXDSName").trim();
        this.BrokerDSName = prop.getProperty("BrokerDSName").trim();
        this.ZabbixDBName = prop.getProperty("ZABBIXDBName").trim();
        this.BrokerDBName = prop.getProperty("BrokerDBName").trim();
        this.ZabbixDBUser = prop.getProperty("ZABBIXDBUser").trim();
        this.BrokerDBUser = prop.getProperty("BrokerDBUser").trim();
        this.ZabbixDBPass = prop.getProperty("ZABBIXDBPass").trim();
        this.BrokerDBPass = prop.getProperty("BrokerDBPass").trim();
        this.DBPort = prop.getProperty("DBPort").trim();
        this.ZabbixProfile = prop.getProperty("ZABBIXProfile").trim();
        this.BrokerProfile = prop.getProperty("BrokerProfile").trim();

        //Set dynamically the url of mysql server based on the current profile
        if (ZabbixProfile.equalsIgnoreCase(DEVELOPMENT_PROFILE)) {
            this.ZabbixHost = prop.getProperty("zabbix_host_development_ip").trim();
        } else if (ZabbixProfile.equalsIgnoreCase(DEPLOYMENT_PROFILE)) {
            this.ZabbixHost = prop.getProperty("zabbix_host_deployment_ip").trim();
        } else {
            throw new UnsupportedOperationException("Unknown profile: " + this.ZabbixProfile);
        }

        //Set dynamically the url of mysql server based on the current profile
        if (BrokerProfile.equalsIgnoreCase(DEVELOPMENT_PROFILE)) {
            this.BrokerHost = prop.getProperty("broker_host_development_ip").trim();
        } else if (BrokerProfile.equalsIgnoreCase(DEPLOYMENT_PROFILE)) {
            this.BrokerHost = prop.getProperty("broker_host_deployment_ip").trim();
        } else {
            throw new UnsupportedOperationException("Unknown profile: " + this.BrokerProfile);
        }

        this.ZabbixDBURL = "jdbc:mysql://" + this.ZabbixHost + ":" + this.DBPort + "/" + this.ZabbixDBName + "?user=" + this.ZabbixDBUser + "&password=" + this.ZabbixDBPass + "&useEncoding=true&characterEncoding=UTF-8&";
        this.BrokerDBURL = "jdbc:mysql://" + this.BrokerHost + ":" + this.DBPort + "/" + this.BrokerDBName + "?user=" + this.BrokerDBUser + "&password=" + this.BrokerDBPass + "&useEncoding=true&characterEncoding=UTF-8&";

//        System.out.println(DBURL);
    }

    public Connection getDatasource() {
        return getDatasource("broker");
    }

    public Connection getDatasource(String database) {
        if (database.equalsIgnoreCase("zabbix")) {
            if (zabbixDatasource == null) {
//            Logger.getLogger(DatabaseHandler.class.getName()).log(Level.INFO, "Datasource: " + DSName + " is empty!");
                try {
                    Context envContext = new InitialContext();

                    this.zabbixDatasource = (DataSource) envContext.lookup("java:jboss/datasources/" + ZabbixDSName);

//                Logger.getLogger(DatabaseHandler.class.getName()).log(Level.INFO, "Created new  datasource!");
                } catch (NamingException ex) {
                    Logger.getLogger(DatabaseHandler.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            try {
                return this.zabbixDatasource.getConnection();
            } catch (SQLException ex) {
                ex.printStackTrace();
                Logger.getLogger(DatabaseHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            if (brokerDatasource == null) {
//            Logger.getLogger(DatabaseHandler.class.getName()).log(Level.INFO, "Datasource: " + DSName + " is empty!");
                try {
                    Context envContext = new InitialContext();

                    this.brokerDatasource = (DataSource) envContext.lookup("java:jboss/datasources/" + BrokerDSName);

//                Logger.getLogger(DatabaseHandler.class.getName()).log(Level.INFO, "Created new  datasource!");
                } catch (NamingException ex) {
                    Logger.getLogger(DatabaseHandler.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            try {
                return this.brokerDatasource.getConnection();
            } catch (SQLException ex) {
                ex.printStackTrace();
                Logger.getLogger(DatabaseHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return null;
    }

    public Connection getConnection(String database) {
        try {

            try {
                Class.forName("com.mysql.jdbc.Driver").newInstance();

            } catch (InstantiationException ex) {
                Logger.getLogger(DatabaseHandler.class
                        .getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalAccessException ex) {
                Logger.getLogger(DatabaseHandler.class
                        .getName()).log(Level.SEVERE, null, ex);
            }
            try {

                String _DBURL = "";
                if (database.equalsIgnoreCase("zabbix")) {
                    _DBURL = this.ZabbixDBURL;
                } else {
                    _DBURL = this.BrokerDBURL;
                }
                this.connection = DriverManager.getConnection(_DBURL);

                return this.connection;

            } catch (SQLException ex) {

                Logger.getLogger(DatabaseHandler.class
                        .getName()).log(Level.SEVERE, null, ex);
            }

        } catch (ClassNotFoundException ex) {
            Logger.getLogger(DatabaseHandler.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
        return this.connection;
    }

    public boolean closeDBStreams(Connection connection, PreparedStatement ps, ResultSet rs) {
        boolean status = true;
        if (null != rs) {
            try {
                rs.close();
            } catch (SQLException ex) {
                Logger.getLogger(DatabaseHandler.class.getName()).log(Level.SEVERE, null, ex);
                status = false;
            }
        }
        if (null != ps) {
            try {
                ps.close();
            } catch (SQLException ex) {
                Logger.getLogger(DatabaseHandler.class.getName()).log(Level.SEVERE, null, ex);
                status = false;
            }
        }
        if (null != connection) {
            try {
                connection.close();
            } catch (SQLException ex) {
                Logger.getLogger(DatabaseHandler.class.getName()).log(Level.SEVERE, null, ex);
                status = false;
            }
        }
        if (status) {
//            Logger.getLogger(DatabaseHandler.class.getName()).info("Successfuly closed all Database streams...");
        } else {
            Logger.getLogger(DatabaseHandler.class.getName()).severe("Could not close all Database streams... This may lead to memory leak..");
        }
        return status;
    }

}
