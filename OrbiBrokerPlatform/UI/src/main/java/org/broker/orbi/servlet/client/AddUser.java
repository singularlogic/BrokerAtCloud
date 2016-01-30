/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.broker.orbi.servlet.client;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.broker.orbi.rest.client.PuLSaRRestClient;
import org.broker.orbi.rest.client.PuLSaRUser;
import org.broker.orbi.service.UserService;

/**
 *
 * @author Christos Paraskeva <ch.paraskeva at gmail dot com>
 */
public class AddUser {

    public static void main(String[] args) {

        //sp eu
        for (int i = 21; i <= 30; i++) {

            String username = "p" + i;
            String name = "Broker";
            String surname = "User" + i;
            String password = username;
            String userRole = "sp";
            String pulsarRole = "";

            String role = "";

            switch (userRole) {
                case "sp": {
                    role = "serviceprovider";
                    pulsarRole = "sp";
                    break;
                }
                case "eu": {
                    role = "enduser";
                    pulsarRole = "sc";
                }
                default: {
                    break;
                }
            }

            String encryptedPWD = "";

            try {
                //Producing the SHA hash for the input
                MessageDigest m;
                m = MessageDigest.getInstance("SHA");
                m.update(password.getBytes(), 0, password.length());
                encryptedPWD = new BigInteger(1, m.digest()).toString(16);
            } catch (NoSuchAlgorithmException ex) {
                Logger.getLogger(RegisterClient.class.getName()).log(Level.SEVERE, null, ex);
            }

            boolean allOK = UserService.registerNewUser(username, name, surname, username + "@broker.eu", encryptedPWD, role, password);
            if (allOK) {
                PuLSaRRestClient.addPuLSarUser(new PuLSaRUser(username, password, pulsarRole));
                Logger.getLogger(AddUser.class.getName()).info("Added user: " + username + ", " + name + ", " + surname + ", " + username + "@broker.eu" + ", " + encryptedPWD + ", " + role + ", " + password);
            }

        }

    }

}
