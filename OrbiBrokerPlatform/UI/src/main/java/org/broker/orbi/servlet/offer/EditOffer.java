package org.broker.orbi.servlet.offer;

import java.io.IOException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.broker.orbi.models.Flavor;
import org.broker.orbi.models.IaaSConfiguration;
import org.broker.orbi.models.Application;
import org.broker.orbi.models.Offer;
import org.broker.orbi.models.Policy;
import org.broker.orbi.models.ServiceDescription;
import org.broker.orbi.service.FlavorService;
import org.broker.orbi.service.IaaSConfigurationService;
import org.broker.orbi.service.AppstoreService;
import org.broker.orbi.service.OfferService;
import org.broker.orbi.service.PolicyService;
import org.broker.orbi.service.ServiceDescriptionService;

/**
 *
 * @author Christos Paraskeva <ch.paraskeva at gmail dot com>
 */
@WebServlet(name = "EditOffer", urlPatterns = {"/offerings/add"})
public class EditOffer extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        String username = request.getUserPrincipal().getName();

        if (null != request.getParameter("id")) {
            Offer offer = OfferService.getOffer(request.getParameter("id"));
            request.setAttribute("offer", offer);
        }

        //
        //TODO: Fix proper solution policy is related to sd
        //
        List<Policy> policies = PolicyService.getAllPolicies();
        List<ServiceDescription> serviceDescs = ServiceDescriptionService.getServiceDescriptions(username);
        List<Application> images = AppstoreService.getAllImages(username);
        List<Flavor> flavors = FlavorService.getAllFlavors(username);
        List<IaaSConfiguration> iaasProviders = IaaSConfigurationService.getIaaSProviders(username);

        request.setAttribute("policies", policies);
        request.setAttribute("serviceDescs", serviceDescs);
        request.setAttribute("images", images);
        request.setAttribute("flavors", flavors);
        request.setAttribute("iaasProviders", iaasProviders);

        response.setContentType("text/html;charset=UTF-8");
        request.setAttribute("username", username);
        request.getRequestDispatcher("/offerings/add.jsp").forward(request, response);

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
