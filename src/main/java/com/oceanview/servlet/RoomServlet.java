package com.oceanview.servlet;

import com.google.gson.Gson;
import com.oceanview.dao.RoomDAO;
import com.oceanview.model.Room;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

/**
 * RoomServlet - returns available rooms as JSON (for front-end dropdowns).
 */
@WebServlet("/api/rooms")
public class RoomServlet extends HttpServlet {

    private final RoomDAO roomDAO;
    private final Gson    gson;

    public RoomServlet() {
        this.roomDAO = new RoomDAO();
        this.gson    = new Gson();
    }

    public RoomServlet(RoomDAO roomDAO) {
        this.roomDAO = roomDAO;
        this.gson    = new Gson();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json;charset=UTF-8");
        PrintWriter out = resp.getWriter();

        String filter = req.getParameter("filter");
        List<Room> rooms;

        if ("available".equalsIgnoreCase(filter)) {
            rooms = roomDAO.getAvailableRooms();
        } else {
            rooms = roomDAO.getAllRooms();
        }

        out.print(gson.toJson(rooms));
    }
}
