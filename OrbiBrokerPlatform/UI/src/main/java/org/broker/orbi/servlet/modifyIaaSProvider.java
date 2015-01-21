/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.broker.orbi.servlet;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.broker.orbi.ui.IaaSProvider;
import org.broker.orbi.util.Util;

/**
 *
 * @author ermis
 */
public class modifyIaaSProvider extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        String username = request.getUserPrincipal().getName();

        //Modify the record
        if (null != request.getParameter("iaas_id")) {
            boolean isSuccess = Util.modifyIaaSProviderDetails(Integer.parseInt(request.getParameter("iaas_id")), request.getParameter("end_point"), request.getParameter("tenant_name"), request.getParameter("username"), request.getParameter("password"),request.getParameter("failure_end_point"));
            if (isSuccess) {
               request.setAttribute("updateIAAS", "SUCCESS"); 
            } else {
               request.setAttribute("updateIAAS", "ERROR");  
            }
        }

        
        
        
        IaaSProvider iaasProvider = Util.getIaaSProvider(username);
        request.setAttribute("iaasProvider", iaasProvider);
        request.setAttribute("username", username);
        
        request.getRequestDispatcher("OpenStackAccount.jsp?").forward(request, response);
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
