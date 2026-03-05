package com.oceanview.servlet;

import com.oceanview.dao.UserDAO;
import com.oceanview.model.User;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * LoginServlet Tests.
 * Tests authentication flow, redirects, and session creation.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("LoginServlet Unit Tests")
class LoginServletTest {

    @Mock private UserDAO            mockUserDAO;
    @Mock private HttpServletRequest  mockReq;
    @Mock private HttpServletResponse mockResp;
    @Mock private HttpSession         mockSession;
    @Mock private RequestDispatcher   mockDispatcher;

    private LoginServlet loginServlet;

    @BeforeEach
    void setUp() {
        loginServlet = new LoginServlet(mockUserDAO);
    }

    @Test
    @DisplayName("doPost: valid credentials create session and redirect to dashboard")
    void testValidLoginCreatesSession() throws Exception {
        when(mockReq.getParameter("username")).thenReturn("admin");
        when(mockReq.getParameter("password")).thenReturn("Admin@123");
        when(mockUserDAO.authenticate("admin", "Admin@123"))
            .thenReturn(new User(1, "admin", "System Administrator", "ADMIN"));
        when(mockReq.getSession(true)).thenReturn(mockSession);
        when(mockReq.getContextPath()).thenReturn("/OceanViewResort");

        loginServlet.doPost(mockReq, mockResp);

        verify(mockSession).setAttribute(eq("loggedUser"), any(User.class));
        verify(mockSession).setAttribute("userId", 1);
        verify(mockSession).setAttribute("userRole", "ADMIN");
        verify(mockResp).sendRedirect("/OceanViewResort/pages/dashboard.html");
    }

    @Test
    @DisplayName("doPost: invalid credentials forward to login with error")
    void testInvalidLoginForwardsWithError() throws Exception {
        when(mockReq.getParameter("username")).thenReturn("admin");
        when(mockReq.getParameter("password")).thenReturn("wrongpass");
        when(mockUserDAO.authenticate("admin", "wrongpass")).thenReturn(null);
        when(mockReq.getRequestDispatcher("/pages/login.html")).thenReturn(mockDispatcher);

        loginServlet.doPost(mockReq, mockResp);

        verify(mockReq).setAttribute(eq("error"), anyString());
        verify(mockDispatcher).forward(mockReq, mockResp);
        verify(mockResp, never()).sendRedirect(anyString());
    }

    @Test
    @DisplayName("doPost: empty username forwards with error (no DB call)")
    void testEmptyUsernameForwardsWithError() throws Exception {
        when(mockReq.getParameter("username")).thenReturn("");
        when(mockReq.getParameter("password")).thenReturn("Admin@123");
        when(mockReq.getRequestDispatcher("/pages/login.html")).thenReturn(mockDispatcher);

        loginServlet.doPost(mockReq, mockResp);

        verify(mockUserDAO, never()).authenticate(anyString(), anyString());
        verify(mockReq).setAttribute(eq("error"), anyString());
        verify(mockDispatcher).forward(mockReq, mockResp);
    }

    @Test
    @DisplayName("doPost: empty password forwards with error (no DB call)")
    void testEmptyPasswordForwardsWithError() throws Exception {
        when(mockReq.getParameter("username")).thenReturn("admin");
        when(mockReq.getParameter("password")).thenReturn("   ");
        when(mockReq.getRequestDispatcher("/pages/login.html")).thenReturn(mockDispatcher);

        loginServlet.doPost(mockReq, mockResp);

        verify(mockUserDAO, never()).authenticate(anyString(), anyString());
        verify(mockDispatcher).forward(mockReq, mockResp);
    }

    @Test
    @DisplayName("doGet: forwards to login page")
    void testDoGetForwardsToLogin() throws Exception {
        when(mockReq.getRequestDispatcher("/pages/login.html")).thenReturn(mockDispatcher);

        loginServlet.doGet(mockReq, mockResp);

        verify(mockDispatcher).forward(mockReq, mockResp);
    }
}
