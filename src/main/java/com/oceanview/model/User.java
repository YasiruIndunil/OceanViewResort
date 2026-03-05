package com.oceanview.model;

import java.math.BigDecimal;

/**
 * User model for authentication.
 */
public class User {
    private int userId;
    private String username;
    private String fullName;
    private String role;

    public User() {}

    public User(int userId, String username, String fullName, String role) {
        this.userId   = userId;
        this.username = username;
        this.fullName = fullName;
        this.role     = role;
    }

    public int getUserId()             { return userId; }
    public void setUserId(int id)      { this.userId = id; }

    public String getUsername()        { return username; }
    public void setUsername(String u)  { this.username = u; }

    public String getFullName()        { return fullName; }
    public void setFullName(String n)  { this.fullName = n; }

    public String getRole()            { return role; }
    public void setRole(String r)      { this.role = r; }
}
