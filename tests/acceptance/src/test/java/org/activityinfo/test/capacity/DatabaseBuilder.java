package org.activityinfo.test.capacity;

import com.google.common.collect.Lists;
import org.activityinfo.test.capacity.agent.Agent;
import org.activityinfo.test.driver.ApiApplicationDriver;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import static org.activityinfo.test.driver.Property.name;
import static org.activityinfo.test.driver.Property.property;


public class DatabaseBuilder {
    
    private static final Logger LOGGER = Logger.getLogger(DatabaseBuilder.class.getName());
    
    private final ApiApplicationDriver driver;
    private final int formCount = 30;
    private final int fieldCount = 30;

    public DatabaseBuilder(Agent agent) {
        this.driver = agent.getDriver();
    }
    
    public DatabaseBuilder(ApiApplicationDriver driver) {
        this.driver = driver;
    }


    public List<String> createDatabases(int databaseCount) throws Exception {
        
        LOGGER.info(String.format("Creating %d databases...", databaseCount));
        
        List<String> databases = Lists.newArrayList();
        
        driver.startBatch();

        for(int dbIndex=0;dbIndex<databaseCount;++dbIndex) {
            String databaseName = "Database " + dbIndex;

            LOGGER.info("Creating database " + databaseName);
            
            driver.createDatabase(name(databaseName));
            
            databases.add(databaseName);

            for (int formIndex = 0; formIndex < formCount; ++formIndex) {
                String formName = String.format("Form %d-%d", dbIndex, formIndex);

                driver.createForm(
                        property("database", databaseName),
                        property("name", formName));
            }

            for (int formIndex = 0; formIndex < formCount; ++formIndex) {
                String formName = String.format("Form %d-%d", dbIndex, formIndex);
                for (int fieldIndex = 0; fieldIndex < fieldCount; ++fieldIndex) {
                    String fieldName = String.format("Field %d-%d-%d", dbIndex, formIndex, fieldIndex);
                    if((fieldIndex % 2) == 0) {
                        createQuantityField(formName, fieldName);
                    } else {
                        createEnumField(formName, fieldName);
                    }
                }
            }
        }

        driver.submitBatch();
        
        return databases;
    }

    private void createEnumField(String formName, String fieldName) throws Exception {
        
        driver.createField(name(fieldName),
                property("form", formName),
                property("type", "enumerated"),
                property("items", Arrays.asList("blue", "green", "red", "orange")));
    }


    private void createQuantityField(String formName, String fieldName) throws Exception {
        driver.createField(
                property("form", formName),
                property("name", fieldName),
                property("type", "quantity"));

    }

}
