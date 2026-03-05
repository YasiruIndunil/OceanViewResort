package com.oceanview.servlet;

import com.google.gson.Gson;
import com.oceanview.dao.ReservationDAO;
import com.oceanview.dao.RoomDAO;
import com.oceanview.model.Reservation;
import com.oceanview.model.Room;
import com.oceanview.util.InputValidator;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ReservationServlet - REST-style endpoint for reservation operations.
 * Handles: add, view, list, cancel
 * Design Pattern: Front Controller + DAO
 */
@WebServlet("/api/reservations/*")
public class ReservationServlet extends HttpServlet {

    private final ReservationDAO reservationDAO;
    private final RoomDAO        roomDAO;
    private final Gson           gson;

    public ReservationServlet() {
        this.reservationDAO = new ReservationDAO();
        this.roomDAO        = new RoomDAO();
        this.gson           = new Gson();
    }

    public ReservationServlet(ReservationDAO reservationDAO, RoomDAO roomDAO) {
        this.reservationDAO = reservationDAO;
        this.roomDAO        = roomDAO;
        this.gson           = new Gson();
    }

    // ── GET: list all OR get by reservation number ────────────

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        if (!isAuthenticated(req, resp)) return;

        resp.setContentType("application/json;charset=UTF-8");
        PrintWriter out = resp.getWriter();

        String pathInfo = req.getPathInfo(); // e.g. /OVR-20250101-123456

        if (pathInfo == null || pathInfo.equals("/")) {
            // List all reservations
            List<Reservation> list = reservationDAO.getAllReservations();
            out.print(gson.toJson(list));
        } else {
            // Get single reservation
            String resNumber = pathInfo.substring(1);
            Reservation res = reservationDAO.getReservationByNumber(resNumber);
            if (res != null) {
                out.print(gson.toJson(res));
            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print(gson.toJson(Map.of("error", "Reservation not found: " + resNumber)));
            }
        }
    }

    // ── POST: add new reservation ────────────────────────────

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        if (!isAuthenticated(req, resp)) return;

        resp.setContentType("application/json;charset=UTF-8");
        PrintWriter out = resp.getWriter();

        try {
            String guestName   = req.getParameter("guestName");
            String address     = req.getParameter("address");
            String contact     = req.getParameter("contactNumber");
            String email       = req.getParameter("email");
            int    roomId      = Integer.parseInt(req.getParameter("roomId"));
            String checkInStr  = req.getParameter("checkInDate");
            String checkOutStr = req.getParameter("checkOutDate");
            int    numGuests   = Integer.parseInt(req.getParameter("numGuests"));
            String specialReq  = req.getParameter("specialRequests");

            LocalDate checkIn  = LocalDate.parse(checkInStr);
            LocalDate checkOut = LocalDate.parse(checkOutStr);

            // Validate room capacity
            Room room = roomDAO.getRoomById(roomId);
            int  maxCap = (room != null) ? room.getMaxGuests() : 10;

            String validationError = InputValidator.validateReservationInput(
                    guestName, contact, email, roomId, checkIn, checkOut, numGuests);

            if (validationError != null) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print(gson.toJson(Map.of("error", validationError)));
                return;
            }

            if (!InputValidator.isValidNumGuests(numGuests, maxCap)) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print(gson.toJson(Map.of("error", "Number of guests exceeds room capacity of " + maxCap)));
                return;
            }

            Reservation res = new Reservation(guestName, address, contact, email,
                                              roomId, checkIn, checkOut, numGuests, specialReq);

            HttpSession session = req.getSession(false);
            int createdBy = (session != null) ? (int) session.getAttribute("userId") : 1;

            String result = reservationDAO.addReservation(res, createdBy);

            Map<String, String> response = new HashMap<>();
            if (result.startsWith("SUCCESS")) {
                resp.setStatus(HttpServletResponse.SC_CREATED);
                response.put("message", result);
                response.put("reservationNumber", res.getReservationNumber());
            } else {
                resp.setStatus(HttpServletResponse.SC_CONFLICT);
                response.put("error", result);
            }
            out.print(gson.toJson(response));

        } catch (NumberFormatException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print(gson.toJson(Map.of("error", "Invalid numeric input: " + e.getMessage())));
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print(gson.toJson(Map.of("error", "Server error: " + e.getMessage())));
        }
    }

    // ── DELETE: cancel reservation ───────────────────────────

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        if (!isAuthenticated(req, resp)) return;

        resp.setContentType("application/json;charset=UTF-8");
        PrintWriter out = resp.getWriter();

        String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print(gson.toJson(Map.of("error", "Reservation number is required.")));
            return;
        }

        String resNumber = pathInfo.substring(1);
        String result    = reservationDAO.cancelReservation(resNumber);

        if (result.startsWith("SUCCESS")) {
            out.print(gson.toJson(Map.of("message", result)));
        } else {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            out.print(gson.toJson(Map.of("error", result)));
        }
    }

    // ── Helpers ───────────────────────────────────────────────

    private boolean isAuthenticated(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("loggedUser") == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.setContentType("application/json;charset=UTF-8");
            resp.getWriter().print(new Gson().toJson(
                    Map.of("error", "Unauthorized. Please login first.")));
            return false;
        }
        return true;
    }
}
