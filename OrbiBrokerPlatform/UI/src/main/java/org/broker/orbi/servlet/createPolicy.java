/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.broker.orbi.servlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.broker.orbi.ui.Policy;
import org.broker.orbi.ui.PolicyVariable;
import org.broker.orbi.ui.ServiceLevelProfile;
import org.broker.orbi.ui.VariableType;
import org.broker.orbi.util.Util;

/**
 *
 * @author ermis
 */
@WebServlet(name = "createPolicy", urlPatterns = {"/en/createPolicy"})
public class createPolicy extends HttpServlet {

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
        List<VariableType> variableTypes = Util.getVariableTypes();
        request.setAttribute("variableTypes", variableTypes);

        if (null != request.getParameter("id")) {
            try {
                int policy_id = Integer.parseInt(request.getParameter("id"));
                //Get all variable os the specific policy
                List<PolicyVariable> policyVariables = Util.getPolicyAllProfilesVariables(policy_id);
                Policy policy = Util.getPolicy(policy_id).get(0);
                List<ServiceLevelProfile> policyProfiles = Util.getServiceLevelProfiles(policy_id,0);
                
                
                
                Map<String, ArrayList<String>> slpVariables = Util.getPolicyAllSLPVariable(policy_id);
                List<ServiceLevelProfile> relatedSLP = Util.getAllRelatedSLP(request.getParameter("id"));
                request.setAttribute("policyVariables", policyVariables);
                request.setAttribute("policy", policy);
                request.setAttribute("policyProfiles", policyProfiles);
                request.setAttribute("slpVariables", slpVariables);
                request.setAttribute("relatedSLP", relatedSLP);

            } catch (NumberFormatException nfe) {

            }
        }
        
        
                        
        
        List<ServiceLevelProfile> primitiveSLP = Util.getServiceLevelProfiles(0,1);
        request.setAttribute("primitiveSLP", primitiveSLP);

        response.setContentType("text/html;charset=UTF-8");
        request.setAttribute("username", username);
        request.getRequestDispatcher("CreatePolicy.jsp").forward(request, response);

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
