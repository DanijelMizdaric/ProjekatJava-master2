package com.danijelmizdaric.example;

public class UserSession {
    private static String username;
    private static String role;
    public static void setUserDetails(String username, String role) {
        UserSession.username = username;
        UserSession.role = role;
    }
    public static String getUsername() {
        return username;
    }
    public static String getRole() {
        return role;
    }
}

