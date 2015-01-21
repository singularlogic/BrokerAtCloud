package org.broker.orbi.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.broker.orbi.util.Util;
import org.broker.orbi.util.impl.ZabbixUtil;
import org.broker.orbi.util.openstack.OpenStackIntegration;

/**
 *
 * @author smantzouratos
 */
public class purchaseSO extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        boolean allOK = false;
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");
        String username = request.getUserPrincipal().getName();
        String offerID = request.getParameter("offer");
        String callback = request.getParameter("callback");
        Map<String, String> osiDetail = Util.getOpenStackIntegrationDetails(offerID);

        //Step1: Check if values retrieved from DB successfuly
        if (!osiDetail.isEmpty()) {
            //Step2: Try Create VM Instance
            String serverID=null;
            OpenStackIntegration osi = new OpenStackIntegration();
            Object[] osiReturn =osi.createInstance(osiDetail.get("endpoint"), osiDetail.get("username"), osiDetail.get("password"), osiDetail.get("tenant"), osiDetail.get("instancename"), osiDetail.get("flavorid"), osiDetail.get("templateid"), osiDetail.get("internalIP"));

            if (null != osiReturn){
                serverID= (String)osiReturn[0];
            }
            
            
            //Instance Successfuly Deployed 
            if (null != serverID && !serverID.isEmpty()) {
                allOK = false;
                //
                //TODO: Get actural Public IP from OSI
                //
                //String publicIP = "213.249.38.69";
                String publicIP = (String)osiReturn[1];
                
                //Step3: Add new VM to ZABBIX Database
                int zabbixHostID = ZabbixUtil.insertNEWZabbixHost(osiDetail.get("instancename") + publicIP, publicIP);

                //Step4: Save Purchase of User to DB
                if (zabbixHostID != 0) {
                    allOK = Util.purchaseServiceOffering(username, Integer.parseInt(offerID), publicIP, zabbixHostID, serverID);
                }
            }
        }

        try (PrintWriter out = response.getWriter()) {
            if (allOK) {
                out.println(callback + "({\"message\":\"SUCCESS\"})");
            } else {
                out.println(callback + "({\"message\":\"ERROR\"})");
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
