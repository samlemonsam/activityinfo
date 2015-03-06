package org.activityinfo.store.mysql

import com.mysql.jdbc.Driver
import liquibase.Liquibase
import liquibase.database.jvm.JdbcConnection
import liquibase.resource.FileSystemResourceAccessor
import org.gradle.api.DefaultTask
import org.gradle.api.file.FileCollection
import org.gradle.api.file.FileTree
import org.gradle.api.logging.LogLevel
import org.gradle.api.tasks.TaskAction

/**
 * Sets up a MySQL Database using Liquibase and dump scripts
 */
class MySqlSetupTask extends DefaultTask {

    boolean dropDatabase = false
    String databaseName
    String user
    String password
    String changeLog
    FileCollection scripts

    @TaskAction
    def setup() {
        createDatabase()
        def connection = openConnection(databaseName)
        try {
            migrateSchema(connection)
            populateData(connection)
        } finally {
            connection.close()
        }
    }

    def openConnection(String databaseName) {
        def properties = new Properties()
        properties.setProperty("user", user)
        properties.setProperty("password", password)

        def url = "jdbc:mysql://localhost/${databaseName}?useUnicode=true&characterEncoding=UTF-8"

        def driver = new Driver()
        return driver.connect(url, properties)
    }


    def createDatabase() {
        // First create the database itself, dropping first if requested
        def connection = openConnection("")
        try {
            def stmt = connection.createStatement()
            if(dropDatabase) {
                stmt.execute("DROP DATABASE IF EXISTS `${databaseName}`")
            }
            stmt.execute("CREATE DATABASE IF NOT EXISTS `${databaseName}`")
        } finally {
            connection.close()
        }
    }

    def migrateSchema(connection) {
        def liquibase = new Liquibase(changeLog,
                new FileSystemResourceAccessor(project.file('src/main/resources').absolutePath),
                new JdbcConnection(connection));

        liquibase.log.logLevel = liquibaseLoggingLevel()
        liquibase.update(null)
    }


    def populateData(connection) {
        ScriptRunner runner = new ScriptRunner(connection, false, true)
        if(!project.logger.isEnabled(LogLevel.DEBUG)) {
            runner.setLogWriter(null)
        }
        scripts.each {
            it.withReader { reader ->
                runner.execute(reader)
            }
        }
    }

    def liquibaseLoggingLevel() {

        switch (logging.level) {
            case LogLevel.DEBUG:
                return liquibase.logging.LogLevel.DEBUG
            case LogLevel.INFO:
                return liquibase.logging.LogLevel.INFO
            default:
                return liquibase.logging.LogLevel.SEVERE
        }
    }
}
