package org.broker.orbi.servlet.other;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.broker.orbi.util.entities.ZabbixHistory;
import org.broker.orbi.util.entities.ZabbixItem;
import org.broker.orbi.util.impl.ZabbixUtil;

/**
 *
 * @author smantzouratos
 */
@WebServlet(name = "retrieveHistory", urlPatterns = {"/services/retrieveHistory"})
public class retrieveHistory extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");

        String purchaseID = request.getParameter("id");
        String varType = request.getParameter("varType");
        String callback = request.getParameter("callback");

        int hostID = ZabbixUtil.getHostID(Integer.valueOf(purchaseID));

        String jsonSTR = "";

        if (hostID != 0) {
            Map<String, ZabbixItem> mapOfZabbixItem = ZabbixUtil.retrieveHistory(hostID, 120);
            if (mapOfZabbixItem.containsKey(varType)) {

                ZabbixItem tempItem = (ZabbixItem) mapOfZabbixItem.get(varType);
                Map<String, ZabbixHistory> mapOfZabbixHistory = tempItem.getMapOfZabbixHistory();
                SortedSet<String> keys = new TreeSet<String>(mapOfZabbixHistory.keySet());
                for (String key : keys) {
                    String value = "";
                    ZabbixHistory tempHist = (ZabbixHistory) mapOfZabbixHistory.get(key);
                    Date currentDate = new Date(Long.parseLong(tempHist.getTimestamp()) * 1000);

                    if (null != tempItem.getZabbixKey()) {

                        switch (tempItem.getZabbixKey()) {
                            case "apache[ReqPerSec]":
                                float reqPerSec = Float.valueOf(tempHist.getValue());
                                value = String.valueOf((int) reqPerSec);
                                break;
                            case "mysql.threads":
                                float threads = Float.valueOf(tempHist.getValue());
                                value = String.valueOf((int) threads);
                                break;
                            case "mysql.questions":
                                float questions = Float.valueOf(tempHist.getValue());
                                value = String.valueOf((int) questions);
                                break;
                            case "mysql.slowqueries":
                                float slowQueries = Float.valueOf(tempHist.getValue());
                                value = String.valueOf((int) slowQueries);
                                break;
                            case "system.cpu.load[,avg1]":
                                float cpuLoad = Float.valueOf(tempHist.getValue()) * 125;
                                if (cpuLoad > 100) {
                                    cpuLoad = 99F;
                                }
//                                System.out.println("Float: " + cpuLoad);
                                value = String.valueOf((int) cpuLoad);
//                                System.out.println("Int:" + (int) cpuLoad);
                                break;
                            case "apache[Uptime]":
                                float apacheUptime = Float.valueOf(tempHist.getValue());
                                value = String.valueOf((int) apacheUptime);
                                break;
                            default:
                                value = tempHist.getValue();
                                break;
                        }
                    } else {
                        value = tempHist.getValue();
                    }

                    jsonSTR += "{\"timestamp\":\"" + currentDate + "\",\"metricValue\":\"" + value + "\"},";

                }

            }
            if (jsonSTR.contains(",")) {
                jsonSTR = jsonSTR.substring(0, jsonSTR.length() - 1);
            } else {
                jsonSTR = "{\"message\":\"ERROR\"}";
            }
        } else {
            jsonSTR = "{\"message\":\"ERROR\"}";
        }

//        System.out.println("JSON: " + jsonSTR);
        try (PrintWriter out = response.getWriter()) {

            out.println(callback + "({\"history\":[" + jsonSTR + "]})");

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
