package org.broker.orbi.servlet.other;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import javax.xml.bind.DatatypeConverter;

import org.apache.commons.io.IOUtils;
import org.broker.orbi.models.Policy;
import org.broker.orbi.rest.client.BrokerRestClient;
import org.broker.orbi.service.PolicyService;

/**
 *
 * @author Christos Paraskeva <ch.paraskeva at gmail dot com>
 */
@WebServlet(name = "FileUploader", urlPatterns = {"/upload"})
@MultipartConfig
public class FileUploader extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        String username = request.getUserPrincipal().getName();

        try (PrintWriter out = response.getWriter()) {
            request.setAttribute("username", username);
            InputStream is = null;
            Part part = request.getPart("policyUpload");
            is = part.getInputStream();
            boolean isSuccess = false;

            String brokerPolicyContent = DatatypeConverter.printBase64Binary(IOUtils.toString(is, StandardCharsets.UTF_8.name()).getBytes());
            String fileName = getFileName(part);
            String validationStatus = BrokerRestClient.validateBrokerPolicy(brokerPolicyContent);
            //Check if Broker Policy is valid
            if ("OK".equalsIgnoreCase(validationStatus)) {
                Policy policy = new Policy(0, fileName, null, null, brokerPolicyContent);
                isSuccess = PolicyService.storePolicy(policy);
                if (isSuccess) {
                    out.println("{\"status\":\"OK\", \"message\":\"Broker Policy with name : " + fileName + " was stored!\"}");
                    Logger.getLogger(FileUploader.class.getName()).info("Broker Policy with name : " + fileName + " was stored!");
                    
                    
                    
                } else {
                    out.println("{\"status\":\"FAIL\", \"message\":\"Broker Policy could not be stored to database...\"}");
                }

            } else {
                out.println("{\"status\":\"FAIL\", \"message\":\"Broker Policy could not be validated. Reason: " + validationStatus + "\"}");
                Logger.getLogger(FileUploader.class.getName()).severe("Broker Policy with name : " + fileName + " is not valid.. Reason: " + validationStatus);
            }

        }

//        response.sendRedirect("policies/list");
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

    private String getFileName(final Part part) {
        final String partHeader = part.getHeader("content-disposition");
        for (String content : partHeader.split(";")) {
            if (content.trim().startsWith("filename")) {
                return content.substring(
                        content.indexOf('=') + 1).trim().replace("\"", "");
            }
        }
        return null;
    }

}
