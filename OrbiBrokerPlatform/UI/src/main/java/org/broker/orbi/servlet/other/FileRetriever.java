package org.broker.orbi.servlet.other;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.DatatypeConverter;
import org.broker.orbi.models.Policy;
import org.broker.orbi.models.ServiceDescription;
import org.broker.orbi.service.PolicyService;
import org.broker.orbi.service.ServiceDescriptionService;

/**
 *
 * @author Christos Paraskeva <ch.paraskeva at gmail dot com>
 */
@WebServlet(name = "FileRetriever", urlPatterns = {"/download"})
public class FileRetriever extends HttpServlet {

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
        response.setContentType("text/html;charset=UTF-8");
        response.setContentType("application/octet-stream");
        String id = request.getParameter("id");
//        String username = request.getUserPrincipal().getName();

        String fileName = "";
        String content = "";
        String type = (null == (String) request.getParameter("type") ? "" : (String) request.getParameter("type"));

        switch (type) {

            case "p":
                Policy policy = PolicyService.getPolicy(id);
                fileName = policy.getName();
                content = policy.getContent();
                break;

            case "sd":
                ServiceDescription serviceDescription = ServiceDescriptionService.getServiceDescription(id);
                fileName = serviceDescription.getName();
                content = serviceDescription.getContent();
                break;

            default:
                Logger.getLogger(FileRetriever.class.getName()).severe("Invalid type: " + type);
                break;
        }

        if (!content.isEmpty() && !fileName.isEmpty()) {
            response.setHeader("Content-disposition", "attachment; filename=" + fileName.concat(".ttl"));
            InputStream in = new ByteArrayInputStream(DatatypeConverter.parseBase64Binary(content));
            //InputStream in = new ByteArrayInputStream(policy.getContent().getBytes());
            OutputStream out = response.getOutputStream();
            byte[] buffer = new byte[4096];
            int n;
            while ((n = in.read(buffer)) > 0) {
                out.write(buffer, 0, n);
            }
            in.close();
            out.close();

        } else {
            Logger.getLogger(FileRetriever.class.getName()).severe("Could not retrieve file with  ID: " + id + " for type: " + type);
            response.sendRedirect("pageNotFound");
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
