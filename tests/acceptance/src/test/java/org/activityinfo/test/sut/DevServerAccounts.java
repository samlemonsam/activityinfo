package org.activityinfo.test.sut;

import com.google.common.base.Preconditions;
import cucumber.runtime.java.guice.ScenarioScoped;
import org.activityinfo.test.config.ConfigProperty;
import org.mindrot.bcrypt.BCrypt;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;


/**
 * Manages creation of users accounts on the local dev server
 */
public class DevServerAccounts implements Accounts {

    private static final ConfigProperty DATABASE_NAME = new ConfigProperty("databaseName", "MySQL database name");
    private static final ConfigProperty DATABASE_HOST = new ConfigProperty("databaseHost", "MySQL database name");

    private static final ConfigProperty EMAIL = new ConfigProperty("devAccountEmail", "Dev account email");
    private static final ConfigProperty USERNAME_PROPERTY =
            new ConfigProperty("databaseUsername", "MySQL database username");
    private static final ConfigProperty PASSWORD_PROPERTY =
            new ConfigProperty("databasePassword", "MySQL database password");


    private static final String DEV_PASSWORD = "notasecret";

    /**
     * Map from test-friendly handles to unique email addresses
     */
    private Map<String, String> aliasMap = new HashMap<>();

    private Random random = new Random();
    
    public DevServerAccounts() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("MySQL driver is not on the classpath", e);
        }
    }

    @Override
    public UserAccount ensureAccountExists(String userName) {

        if(aliasMap.containsKey(userName)) {
            String email = aliasMap.get(userName);
            return new UserAccount(email, DEV_PASSWORD);
        }
        return createUniqueAccount(userName);
    }

    /**
     * Creates a unique account mapped to the username for the duration of the test.
     * This ensures that each test is isolated.
     * 
     * @param userName a friendly test name actually used in tests
     * @return a UserAccount with a unique username and password for this user
     */
    private UserAccount createUniqueAccount(String userName) {
        
        String email = generateAlias(userName);
        
        try(Connection connection = DriverManager.getConnection(
                connectionUrl(),
                USERNAME_PROPERTY.getOr("root"),
                PASSWORD_PROPERTY.getIfPresent("root"))) {

            // Create the user for testing purposes
            try(PreparedStatement stmt = connection.prepareStatement(
                    "INSERT INTO userlogin (email, name, password, locale) VALUES(?, ?, ?, ?)")) {

                stmt.setString(1, email);
                stmt.setString(2, nameForEmail(email));
                stmt.setString(3, BCrypt.hashpw(DEV_PASSWORD, BCrypt.gensalt()));
                stmt.setString(4, "en");
                stmt.execute();
            }


        } catch (Exception e) {
            throw new RuntimeException("Exception creating user " + userName, e);
        }
        
        aliasMap.put(userName, email);

        return new UserAccount(email, DEV_PASSWORD);
    }
    
    private String generateAlias(String testHandle) {
        Preconditions.checkNotNull(testHandle, "testHandle");

        StringBuilder alias = new StringBuilder();

        // Add any alphabetic prefix from the test handle,
        // so that bob@bedatadiven.com becomes bob_xyzzsdff@example.com
        // and is at least recognizable
        for(int i=0;i!=testHandle.length();++i) {
            char c = testHandle.toLowerCase().charAt(i);
            if(c >= 'a' && c <= 'z') {
                alias.append(c);
            } else {
                break;
            }
        }
        if(alias.length() == 0) {
            alias.append("user");
        }
        alias.append("_");
        alias.append(Long.toHexString(random.nextLong()));
        alias.append("@example.com");

        return alias.toString();
    }

    private String connectionUrl() {
        return String.format("jdbc:mysql://%s/%s?useUnicode=true&characterEncoding=UTF-8",
                DATABASE_HOST.getOr("localhost"),
                DATABASE_NAME.getOr("activityinfo_at"));

    }

    private String nameForEmail(String email) {
        int at = email.indexOf('@');
        return email.substring(0, at);
    }

    @Override
    public UserAccount any() {
        return ensureAccountExists("user");
    }
}
