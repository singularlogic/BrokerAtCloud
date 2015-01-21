/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.broker.orbi.servlet;

import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.broker.orbi.util.Util;

/**
 *
 * @author smantzouratos
 */
public class registerNewUser extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");

        String username = request.getParameter("username");
        String name = request.getParameter("name");
        String surname = request.getParameter("surname");
        String password = request.getParameter("password");
        String userRole = request.getParameter("userRole");
        String email = request.getParameter("email");
        int MA = 0;

        if (null == username || username.isEmpty()) {
            MA++;
        }
        if (null == name || name.isEmpty()) {
            MA++;
        }
        if (null == surname || surname.isEmpty()) {
            MA++;
        }
        if (null == password || password.isEmpty()) {
            MA++;
        }
        if (null == email || email.isEmpty()) {
            MA++;
        }
        if (userRole.equalsIgnoreCase("0")) {
            MA++;
        }

        if (MA > 0) {
            request.getRequestDispatcher("register?msg=MA").forward(request, response);
        } else {

            String role = "";

            switch (Integer.parseInt(userRole)) {
                case 1: {
                    role = "serviceprovider";
                    break;
                }
                case 2: {
                    role = "enduser";
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
                Logger.getLogger(registerNewUser.class.getName()).log(Level.SEVERE, null, ex);
            }

            boolean allOK = Util.registerNewUser(username, name, surname, email, encryptedPWD, role);

            if (allOK) {
                request.getRequestDispatcher("register?msg=OK").forward(request, response);
            } else {
                request.getRequestDispatcher("register?msg=ER").forward(request, response);
            }
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
