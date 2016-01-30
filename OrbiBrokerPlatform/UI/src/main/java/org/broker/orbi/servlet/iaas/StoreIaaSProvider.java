package org.broker.orbi.servlet.iaas;

import java.io.IOException;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.broker.orbi.service.IaaSConfigurationService;

/**
 *
 * @author Christos Paraskeva <ch.paraskeva at gmail dot com>
 */
@WebServlet(name = "StoreIaaSProvider", urlPatterns = {"/iaasconfig/store"})
public class StoreIaaSProvider extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        String username = request.getUserPrincipal().getName();
        //Parameters
        int iaas_id = (Integer.parseInt(request.getParameter("iaas_id")));
        //Add/Edit IaaSConfiguration
        boolean isSuccess = IaaSConfigurationService.modifyIaaSProviderDetails(iaas_id, request.getParameter("iaas_url"), request.getParameter("iaas_tenant"), request.getParameter("iaas_username"), request.getParameter("iaas_password"),request.getParameter("iaas_name"));
        Logger.getLogger(StoreIaaSProvider.class.getName()).info("IaaSConfiguration with ID: " + request.getParameter("iaas_id") + " was modified with success status: " + isSuccess);
        response.setContentType("text/html;charset=UTF-8");
        request.setAttribute("username", username);
         response.sendRedirect("list");
//        request.getRequestDispatcher("/iaasconfig/list.jsp").forward(request, response);
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
