package org.activityinfo.test.sut;

import org.activityinfo.test.config.ConfigProperty;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;


/**
 * Manages creation of users accounts on the local dev server
 */
public class DevServerAccounts implements Accounts {

    private static final ConfigProperty DATABASE_NAME = new ConfigProperty("databaseName", "MySQL database name");
    private static final ConfigProperty USERNAME_PROPERTY = 
            new ConfigProperty("databaseUsername", "MySQL database username");
    private static final ConfigProperty PASSWORD_PROPERTY = 
            new ConfigProperty("databasePassword", "MySQL database password");


    /**
     * Passwords are not checked when running in development mode
     */
    private static final String DEV_PASSWORD = "notasecret";

    public DevServerAccounts() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("MySQL driver is not on the classpath", e);
        }
    }

    @Override
    public UserAccount ensureAccountExists(String email) {

        try(Connection connection = DriverManager.getConnection(
                connectionUrl(),
                USERNAME_PROPERTY.getOr("root"),
                PASSWORD_PROPERTY.getOr("root", true))) {

            try(PreparedStatement stmt = connection.prepareStatement("SELECT * FROM userlogin WHERE email = ?")) {
                stmt.setString(1, email);

                try(ResultSet resultSet = stmt.executeQuery()) {
                    if (resultSet.next()) {
                        return new UserAccount(email, DEV_PASSWORD);
                    }
                }
            }
            
            // Create the user for testing purposes
            try(PreparedStatement stmt = connection.prepareStatement(
                    "INSERT INTO userlogin (email, name, locale) VALUES(?, ?, ?)")) {
                
                stmt.setString(1, email);
                stmt.setString(2, nameForEmail(email));
                stmt.setString(3, "en");
                stmt.execute();
            }
            
            return new UserAccount(email, DEV_PASSWORD);
            
        } catch (Exception e) {
            throw new RuntimeException("Exception creating user " + email, e);           
        }
    }

    private String connectionUrl() {
        return String.format("jdbc:mysql://localhost/%s?useUnicode=true&characterEncoding=UTF-8",
                DATABASE_NAME.getOr("activityinfo_at"));
        
    }

    private String nameForEmail(String email) {
        int at = email.indexOf('@');
        return email.substring(0, at);
    }

    @Override
    public UserAccount any() {
        return ensureAccountExists("dev@bedatadriven.com");
    }
}
