package com.oceanview.servlet;

import com.oceanview.dao.UserDAO;
import com.oceanview.model.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;

/**
 * LoginServlet - handles user authentication.
 * Pattern: Front Controller
 */
@WebServlet("/LoginServlet")
public class LoginServlet extends HttpServlet {

    private final UserDAO userDAO;

    public LoginServlet() {
        this.userDAO = new UserDAO();
    }

    // Constructor injection for testing
    public LoginServlet(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // Show login page
        req.getRequestDispatcher("/pages/login.html").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String username = req.getParameter("username");
        String password = req.getParameter("password");

        // Input validation
        if (username == null || username.trim().isEmpty() ||
            password == null || password.trim().isEmpty()) {
            req.setAttribute("error", "Username and password are required.");
            req.getRequestDispatcher("/pages/login.html").forward(req, resp);
            return;
        }

        User user = userDAO.authenticate(username.trim(), password.trim());

        if (user != null) {
            // Create session
            HttpSession session = req.getSession(true);
            session.setAttribute("loggedUser", user);
            session.setAttribute("userId",   user.getUserId());
            session.setAttribute("userRole", user.getRole());
            session.setMaxInactiveInterval(30 * 60); // 30 minutes

            resp.sendRedirect(req.getContextPath() + "/pages/dashboard.html");
        } else {
            req.setAttribute("error", "Invalid username or password. Please try again.");
            req.getRequestDispatcher("/pages/login.html").forward(req, resp);
        }
    }
}
