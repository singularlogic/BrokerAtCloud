package org.broker.orbi.servlet.servicedescription;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.broker.orbi.models.Consumer;
import org.broker.orbi.models.ServiceDescription;
import org.broker.orbi.service.ServiceDescriptionService;
import org.broker.orbi.service.UserService;
import org.broker.orbi.util.other.MultipartUtility;
import sun.misc.BASE64Decoder;

/**
 *
 * @author Christos Paraskeva <ch.paraskeva at gmail dot com>
 */
@WebServlet(name = "StoreServiceDescription", urlPatterns = {"/servicedesc/store"})
public class StoreServiceDescription extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        String username = request.getUserPrincipal().getName();
        Consumer consumer = UserService.getUser(username);
        //Parameters
        int policy_id = (Integer.parseInt(request.getParameter("selectedPolicyID")));
        int serviceDescriptionID = (Integer.parseInt(request.getParameter("serviceDescriptionID")));
        //String serviceDescriptionName = request.getParameter("serviceDescriptionName");
        String serviceDescriptionName = request.getParameter("serviceDescriptionAcronym");
        String serviceDescriptionContent = request.getParameter("serviceDescriptionContent");
        Logger.getLogger(StoreServiceDescription.class.getName()).info("Policy ID: " + policy_id + "\nService Description Name: " + serviceDescriptionName);

        BASE64Decoder decoder = new BASE64Decoder();
        String full_name = new String();
        try {
            String decoded = new String(decoder.decodeBuffer(serviceDescriptionContent));

            Pattern p = Pattern.compile("@prefix sd:.*<(.*)>");
            Matcher matcher = p.matcher(decoded);

            if (matcher.find()) {
                full_name = decoded.substring(matcher.start(1), matcher.end(1)) + serviceDescriptionName;
            }

        } catch (IOException ex) {
            Logger.getLogger(MultipartUtility.class.getName()).log(Level.SEVERE, null, ex);
        }

        if (full_name.isEmpty()) {
            Logger.getLogger(StoreServiceDescription.class.getName()).log(Level.SEVERE, "Could not extract ServiceDescription full name");
        } else {
            ServiceDescription serviceDescription = new ServiceDescription(serviceDescriptionID, policy_id, consumer.getId(), serviceDescriptionName, full_name, null, null, null, serviceDescriptionContent);

            boolean isSuccess;

            //Upload Service Description to Pulsar
            isSuccess = true;//MultipartUtility.uploadServiceDescription(serviceDescriptionName.concat(".ttl"), serviceDescriptionContent);

            if (isSuccess == false) {
                Logger.getLogger(StoreServiceDescription.class.getName()).log(Level.SEVERE, "Could not upload ServiceDescription with name: {0} to Pulsar.", serviceDescriptionName);
            } else if (serviceDescriptionID > 0) {
                isSuccess = ServiceDescriptionService.updateServiceDescription(serviceDescription);
            } else {
                isSuccess = ServiceDescriptionService.storeServiceDescription(serviceDescription);
            }

            response.setContentType("text/html;charset=UTF-8");
            request.setAttribute("username", username);
            response.sendRedirect("list");
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
