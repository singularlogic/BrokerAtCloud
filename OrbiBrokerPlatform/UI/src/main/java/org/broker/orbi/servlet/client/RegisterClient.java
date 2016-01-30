package org.broker.orbi.servlet.client;

import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.broker.orbi.rest.client.PuLSaRRestClient;
import org.broker.orbi.rest.client.PuLSaRUser;
import org.broker.orbi.service.UserService;

/**
 *
 * @author Christos Paraskeva <ch.paraskeva at gmail dot com>
 */
@WebServlet(name = "ClientsRegister", urlPatterns = {"/clients/register"})
public class RegisterClient extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");

        String username = request.getParameter("username");
        String name = request.getParameter("firstname");
        String surname = request.getParameter("lastname");
        String password = request.getParameter("password");
        String userRole = request.getParameter("userRole");
        String email = request.getParameter("email");
        String pulsarRole = "";

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
        if (null == userRole || userRole.equalsIgnoreCase("0")) {
            MA++;
        }

        if (MA > 0) {
            response.sendRedirect("../register?msg=MA");
        } else {

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

            boolean allOK = UserService.registerNewUser(username, name, surname, email, encryptedPWD, role,password);

            if (allOK) {
                PuLSaRRestClient.addPuLSarUser(new PuLSaRUser(username, password, pulsarRole));

                response.sendRedirect("../register?msg=OK");
            } else {
                response.sendRedirect("../register?msg=ER");
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
