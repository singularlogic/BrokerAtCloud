package org.broker.orbi.servlet.offer;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.broker.orbi.models.Offer;
import org.broker.orbi.service.OfferService;

/**
 *
 * @author Christos Paraskeva <ch.paraskeva at gmail dot com>
 */
@WebServlet(name = "storeoffer", urlPatterns = {"/offerings/store"})
public class StoreOffer extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        String username = request.getUserPrincipal().getName();

        //Parameters
        String offeringName = request.getParameter("offeringName");
        int policies = (Integer.parseInt(request.getParameter("policies")));
        int serviceDescs = (Integer.parseInt(request.getParameter("serviceDescs")));
        int iaasProviders = (Integer.parseInt(request.getParameter("iaasProviders")));
        int images = (Integer.parseInt(request.getParameter("images")));
        int flavors = (Integer.parseInt(request.getParameter("flavors")));
        
//        Logger.getLogger(StoreOffer.class.getName()).info("Policy ID: " + policy_id + "\nPolicy Name: " + brokerPolicyName);

        Offer offer = new Offer();
        offer.setName(offeringName);
        offer.setPolicy_id(policies);
        offer.setService_description_id(serviceDescs);
        offer.setIaas_id(iaasProviders);
        offer.setImage_template_id(images);
        offer.setFlavor_id(flavors);
        boolean isSuccess;

//        if (policy_id > 0) {
//            isSuccess = PolicyService.updateBrokerPolicy(policy);
//        } else {
            isSuccess = OfferService.storeOffer(offer, username);
//        }

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
