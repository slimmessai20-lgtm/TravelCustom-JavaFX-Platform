package utils;

import entities.User;

public class SessionManager {
    private static SessionManager instance;
    private User currentUser;

    private SessionManager() {}

    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public static User getCurrentUser() {
        return getInstance().currentUser;
    }

    public static void setCurrentUser(User user) {
        getInstance().currentUser = user;
    }

    public static void clearSession() {
        getInstance().currentUser = null;
    }

    public void logout() {
        this.currentUser = null;
    }
}