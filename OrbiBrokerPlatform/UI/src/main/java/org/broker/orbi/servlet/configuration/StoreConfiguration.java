package org.broker.orbi.servlet.configuration;

import java.io.IOException;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.broker.orbi.models.Configuration;
import org.broker.orbi.models.Policy;
import org.broker.orbi.service.ConfigurationService;
import org.broker.orbi.service.PolicyService;

/**
 *
 * @author Christos Paraskeva <ch.paraskeva at gmail dot com>
 */
@WebServlet(name = "StoreConfiguration", urlPatterns = {"/configuration/store"})
public class StoreConfiguration extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        String username = request.getUserPrincipal().getName();

        //Parameters
        int configurationID = (Integer.parseInt(request.getParameter("configurationID")));
        String configName = request.getParameter("configName");
        String configFName = request.getParameter("configFName");
        String configValue = request.getParameter("configValue");
        Logger.getLogger(StoreConfiguration.class.getName()).info("Policy ID: " + configurationID + "\nPolicy Name: " + configName);

        Configuration configuration = new Configuration(configurationID, configFName, configValue, configFName, null);

        boolean isSuccess;

        if (configurationID > 0) {
            isSuccess = ConfigurationService.updateConfiguration(configuration);
        } else {
            isSuccess = ConfigurationService.storeConfiguration(configuration);
        }

        response.setContentType("text/html;charset=UTF-8");
        request.setAttribute("username", username);
        response.sendRedirect("list");
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
