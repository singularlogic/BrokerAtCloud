package org.broker.orbi.servlet.offer;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.broker.orbi.models.Offer;
import org.broker.orbi.models.ServiceDescription;
import org.broker.orbi.service.OfferService;
import org.broker.orbi.service.PulsarService;
import org.broker.orbi.service.PurchaseService;
import org.broker.orbi.service.ServiceDescriptionService;
import org.broker.orbi.util.impl.ZabbixUtil;
import org.broker.orbi.util.openstack.OpenStackIntegration;
import org.broker.orbi.util.openstack.OpenStackUtil;

/**
 *
 * @author smantzouratos
 */
@WebServlet(name = "PurchaseOffer", urlPatterns = {"/purchase"})
public class PurchaseOffer extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        boolean allOK = false;
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");
        String sdFullname = request.getParameter("sid");
        String username = request.getUserPrincipal().getName();
        //Get Provider Offer based on a preferred ServiceDescription
        Offer offerToBuy = OfferService.getOfferByServiceDescriptionFullname(sdFullname);
        Logger.getLogger(PurchaseOffer.class.getName()).info("ServiceDescription with full_name: " + sdFullname + " matched Offer with ID: " + offerToBuy.getId());

        Map<String, String> osiDetail = OpenStackUtil.getOpenStackIntegrationDetails(offerToBuy.getId());

        //Step1: Check if values retrieved from DB successfuly
        if (!osiDetail.isEmpty()) {
            //Step2: Try Create VM Instance
            String serverID = null;
            OpenStackIntegration osi = new OpenStackIntegration();
            Object[] osiReturn = osi.createInstance(osiDetail.get("endpoint"), osiDetail.get("username"), osiDetail.get("password"), osiDetail.get("tenant"), osiDetail.get("instancename"), osiDetail.get("flavorid"), osiDetail.get("templateid"));

            if (null != osiReturn) {
                serverID = (String) osiReturn[0];
            }

            //Instance Successfuly Deployed 
            if (null != serverID && !serverID.isEmpty()) {
                allOK = false;

                // Get actural Public IP from OSI
                String publicIP = (String) osiReturn[1];

                //Step3: Add new VM to ZABBIX Database
                int zabbixHostID = ZabbixUtil.insertNEWZabbixHost(osiDetail.get("instancename") + publicIP, publicIP);

                //Step4: Save Purchase of User to DB
                if (zabbixHostID != 0) {
                    allOK = PurchaseService.purchaseServiceOffering(username, offerToBuy.getId(), publicIP, zabbixHostID, serverID);
                }

                //Step5: if the purchse is success then notify pulsar db
                if (allOK) {
                    ServiceDescription sd = ServiceDescriptionService.getServiceDescriptionByOfferId(offerToBuy.getId());
                    Logger.getLogger(PurchaseOffer.class.getName()).info("Add notification to Pulsar to: IN-USE for sd: "+sd.getFull_name());
                    PulsarService.editUsedServices(username, sd.getFull_name(), PulsarService.IN_USE);
                }
            }
        }

        response.sendRedirect("/orbibroker/services/list");

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
