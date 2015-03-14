package org.activityinfo.store.mysql

import java.sql.Connection

/**
 * Describes a MySql database within a {@code MySqlServer}
 */
class MySqlDatabase {
    
    MySqlServer server
    String name

    String getUrl() {
        "jdbc:mysql://${server.host}:${server.port}/${name}?useUnicode=true&characterEncoding=UTF-8"
    }

    Connection connect() {
        server.connect(name)
    }
    
    Map<String, String> getProperties() {
        Map<String, String> properties = new HashMap<>()
        properties.put("databaseName", name)
        properties.put("databaseHost", server.host)
        properties.put("databaseUsername", server.username)
        properties.put("databasePassword", server.password)
        return properties
    }
}
