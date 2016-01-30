package org.broker.orbi.servlet.services;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.broker.orbi.models.Purchase;
import org.broker.orbi.models.ServiceDescription;
import org.broker.orbi.service.PulsarService;
import org.broker.orbi.service.PurchaseService;
import org.broker.orbi.service.ServiceDescriptionService;
import org.broker.orbi.service.UserService;

/**
 *
 * @author Christos Paraskeva <ch.paraskeva at gmail dot com>
 */
@WebServlet(name = "ServicesList", urlPatterns = {"/services/list"})
public class FetchServices extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");
        String username = request.getUserPrincipal().getName();

        //Check delete action
        if (null != request.getParameter("remove")) {
             ServiceDescription sd = ServiceDescriptionService.getServiceDescriptionByPurchaseId(Integer.parseInt(request.getParameter("remove")));
            PurchaseService.removePurchase(Integer.parseInt(request.getParameter("remove")));
            Logger.getLogger(FetchServices.class.getName()).info("Edit notification to Pulsar to: NOT-USED for sd: " + sd.getFull_name());
            PulsarService.editUsedServices(username, sd.getFull_name(), PulsarService.NOT_USED);
            Logger.getLogger(FetchServices.class.getName()).info("Remove Purchase with ID: " + request.getParameter("remove"));
        }

        
        List<Purchase> purchases = PurchaseService.getAllPurchasedSPOffers(UserService.getUserID(username));

        if (null != purchases) {
            request.setAttribute("numOfpurchases", purchases.size());
            request.setAttribute("purchases", purchases);
        } else {
            request.setAttribute("numOfpurchases", 0);
        }

        request.setAttribute("username", username);
        request.getRequestDispatcher("/services/list.jsp").forward(request, response);
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
