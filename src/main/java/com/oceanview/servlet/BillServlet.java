package com.oceanview.servlet;

import com.google.gson.Gson;
import com.oceanview.dao.ReservationDAO;
import com.oceanview.model.Bill;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

/**
 * BillServlet - generates and retrieves bills.
 */
@WebServlet("/api/bills/*")
public class BillServlet extends HttpServlet {

    private final ReservationDAO reservationDAO;
    private final Gson gson;

    public BillServlet() {
        this.reservationDAO = new ReservationDAO();
        this.gson = new Gson();
    }

    public BillServlet(ReservationDAO reservationDAO) {
        this.reservationDAO = reservationDAO;
        this.gson = new Gson();
    }

    /** GET /api/bills/{reservationNumber} - retrieve bill */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        if (!isAuthenticated(req, resp)) return;
        resp.setContentType("application/json;charset=UTF-8");
        PrintWriter out = resp.getWriter();

        String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print(gson.toJson(Map.of("error", "Reservation number required.")));
            return;
        }

        String resNumber = pathInfo.substring(1);
        Bill bill = reservationDAO.getBillDetails(resNumber);

        if (bill != null) {
            out.print(gson.toJson(bill));
        } else {
            // Auto-generate bill first, then return
            String genResult = reservationDAO.generateBill(resNumber);
            if (genResult.startsWith("SUCCESS")) {
                bill = reservationDAO.getBillDetails(resNumber);
                out.print(gson.toJson(bill));
            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print(gson.toJson(Map.of("error", genResult)));
            }
        }
    }

    /** POST /api/bills/{reservationNumber} - generate bill */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        if (!isAuthenticated(req, resp)) return;
        resp.setContentType("application/json;charset=UTF-8");
        PrintWriter out = resp.getWriter();

        String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print(gson.toJson(Map.of("error", "Reservation number required.")));
            return;
        }

        String resNumber = pathInfo.substring(1);
        String result    = reservationDAO.generateBill(resNumber);

        if (result.startsWith("SUCCESS")) {
            Bill bill = reservationDAO.getBillDetails(resNumber);
            out.print(gson.toJson(bill));
        } else {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            out.print(gson.toJson(Map.of("error", result)));
        }
    }

    private boolean isAuthenticated(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("loggedUser") == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.setContentType("application/json;charset=UTF-8");
            resp.getWriter().print(gson.toJson(Map.of("error", "Unauthorized.")));
            return false;
        }
        return true;
    }
}
