package org.broker.orbi.servlet.policy;

import java.io.IOException;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.broker.orbi.models.Policy;
import org.broker.orbi.service.PolicyService;

/**
 *
 * @author Christos Paraskeva <ch.paraskeva at gmail dot com>
 */
@WebServlet(name = "storepolicy", urlPatterns = {"/policies/store"})
public class StorePolicy extends HttpServlet {

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
        int policy_id = (Integer.parseInt(request.getParameter("policyID")));
        String brokerPolicyName = request.getParameter("brokerPolicyName");
        String brokerPolicyContent = request.getParameter("brokerPolicyContent");
        Logger.getLogger(StorePolicy.class.getName()).info("Policy ID: " + policy_id + "\nPolicy Name: " + brokerPolicyName);

        Policy policy = new Policy(policy_id, brokerPolicyName, null, null, brokerPolicyContent);
        boolean isSuccess;

        if (policy_id > 0) {
            isSuccess = PolicyService.updateBrokerPolicy(policy);
        } else {
            isSuccess = PolicyService.storePolicy(policy);
        }

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
