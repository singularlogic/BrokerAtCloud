/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.broker.orbi.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.broker.orbi.ui.PolicyVariable;
import org.broker.orbi.util.Util;

/**
 *
 * @author smantzouratos
 */
public class retrieveVariables extends HttpServlet {

    
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");

        String policyID = request.getParameter("policy");
        String slpID = request.getParameter("slp");
        String callback = request.getParameter("callback");
        


        List<PolicyVariable> variables =  Util.getAllPolicyProfileVariablesDependOnPolicy(policyID, slpID);///  Util.getPolicyProfileVariables(Integer.parseInt(policyID), Integer.parseInt(slpID));

        
        
        String variablesSTR = "";
        
        for (PolicyVariable tempVar : variables) {
            variablesSTR += "{\"id\":\"" + tempVar.getId() + "\",\"name\":\"" + tempVar.getName() + "\",\"type\":\"" + tempVar.getType_name() + "\",\"metric\":\"" + tempVar.getMetric_unit() + "\"},";
        }
        
        if (variablesSTR.contains(",")) {
            variablesSTR = variablesSTR.substring(0, variablesSTR.length()-1);
        }
        
        try (PrintWriter out = response.getWriter()) {
            out.println(callback +"({\"variables\":[" + variablesSTR + "]})");
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
