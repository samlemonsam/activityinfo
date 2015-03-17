package org.activityinfo.store.mysql

import com.mysql.jdbc.Driver
import java.sql.Connection

/**
 * Describes parameters needed to connect to a MySQL Server instance
 */
class MySqlServer {
    String host
    int port = 3306
    String username
    String password
    
    MySqlDatabase database(String name) {
        new MySqlDatabase(server: this, name: name)
    }
    
    MySqlDatabase getAt(String name) {
        database(name)
    }
    
    Connection connect(String databaseName) {
        def properties = new Properties()
        properties.setProperty("user", username)
        properties.setProperty("password", password)

        def url = "jdbc:mysql://${host}:${port}/${databaseName}?useUnicode=true&amp;characterEncoding=UTF-8"

        def driver = new Driver()
        return driver.connect(url, properties)
    }
    
    Connection connect() {
        return connect("")
    }
}
