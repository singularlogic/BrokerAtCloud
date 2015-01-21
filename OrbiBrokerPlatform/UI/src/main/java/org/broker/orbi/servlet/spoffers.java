/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.broker.orbi.servlet;

import java.io.IOException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.broker.orbi.ui.Flavor;
import org.broker.orbi.ui.ImageTemplate;
import org.broker.orbi.ui.SPOffer;
import org.broker.orbi.util.Util;

/**
 *
 * @author ermis
 */
public class spoffers extends HttpServlet {

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

        //Check delete action
        if (null != request.getParameter("remove")) {
            Util.removeSPOffer(Integer.parseInt(request.getParameter("remove")));
            System.out.println("Remove Offer with ID: " + request.getParameter("remove"));
        }

        String username = request.getUserPrincipal().getName();
        List<SPOffer> spoffers = Util.getAllSPOffers(username);
        List<ImageTemplate> imageTemplates = Util.getAllImageTemplates();
        List<Flavor> flavors = Util.getAllFlavors();
        request.setAttribute("spoffers", spoffers);
        request.setAttribute("imageTemplates", imageTemplates);
        request.setAttribute("flavors", flavors);
        response.setContentType("text/html;charset=UTF-8");
        request.setAttribute("username", username);
        request.getRequestDispatcher("ServiceProviderOffers.jsp").forward(request, response);
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