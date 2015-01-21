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
import org.broker.orbi.util.Util;

/**
 *
 * @author ermis
 */
public class storePolicy extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
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
        int policy_id = (request.getParameter("policyID").isEmpty() ? 0 : Integer.parseInt(request.getParameter("policyID")));
        String policyName = request.getParameter("policyName");
        String policyVariables = request.getParameter("policyVariables");
        String policyProfiles = request.getParameter("policyProfiles");
        String variablesPerProfile = request.getParameter("valuesPerProfile");
        String relatedSLP = request.getParameter("relatedPolicies");
        System.out.println("Request Parameters:\n-------------------------------");
        System.out.println("Policy Name: " + policyName);
        System.out.println("Policy Variables: " + policyVariables);
        System.out.println("Policy Service Profiles: " + policyProfiles);
        System.out.println("VariablesPerProfile: " + variablesPerProfile);
        System.out.println("RelatedToSLP:" +relatedSLP+ "\n");
        
        Util.storeBrokerPolicy(policy_id,policyName, policyVariables, policyProfiles, variablesPerProfile,relatedSLP);

        response.setContentType("text/html;charset=UTF-8");
        request.setAttribute("username", username);
        request.getRequestDispatcher("policies").forward(request, response);
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
