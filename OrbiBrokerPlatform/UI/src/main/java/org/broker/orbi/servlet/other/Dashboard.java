package org.broker.orbi.servlet.other;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.broker.orbi.service.DashboardService;
import org.broker.orbi.service.PurchaseService;
import org.broker.orbi.service.UserService;

/**
 *
 * @author Christos Paraskeva <ch.paraskeva at gmail dot com>
 */
@WebServlet(name = "Dashboard", urlPatterns = {"/dashboard"})
public class Dashboard extends HttpServlet {
    
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        String userRole = UserService.getUserRole(request.getUserPrincipal().getName());
        String username = request.getUserPrincipal().getName();
        request.setAttribute("username", username);

        //Service Provider Role
        if (userRole.equalsIgnoreCase("serviceprovider")) {
            request.setAttribute("info", DashboardService.getProviderDashboardInfo(username));
            request.getRequestDispatcher("provider.jsp").forward(request, response);
        } //Admin Role
        else if (userRole.equalsIgnoreCase("admin")) {
            request.setAttribute("info", DashboardService.getAdminDashboardInfo());
            request.getRequestDispatcher("administrator.jsp").forward(request, response);
        } //End-User Role
        else {
            
            

            request.setAttribute("numOfServices", PurchaseService.getAllPurchasedSPOffers(UserService.getUserID(username)).size());
            
            request.getRequestDispatcher("client.jsp").forward(request, response);
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
