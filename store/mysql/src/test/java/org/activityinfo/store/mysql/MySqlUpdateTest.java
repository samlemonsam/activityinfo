package org.activityinfo.store.mysql;

import com.google.gson.JsonObject;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.legacy.KeyGenerator;
import org.activityinfo.store.query.impl.Updater;
import org.junit.Before;
import org.junit.Test;

import static org.activityinfo.model.legacy.CuidAdapter.*;
import static org.activityinfo.store.mysql.ColumnSetMatchers.hasValues;
import static org.junit.Assert.assertThat;


public class MySqlUpdateTest extends AbstractMySqlTest {


    @Before
    public void setupDatabase() throws Throwable {
        resetDatabase();
    }
    
    @Test
    public void createSite() {
        JsonObject changeObject = new JsonObject();
        changeObject.addProperty("@id", "s0000000013");
        changeObject.addProperty("@class", activityFormClass(1).asString());
        changeObject.addProperty("partner", partnerInstanceId(1).asString());
        changeObject.addProperty("date1", "2015-01-01");
        changeObject.addProperty("date2", "2015-01-01");
        changeObject.addProperty("BENE", 45000);
        changeObject.addProperty("location", locationInstanceId(3).asString());

        Updater updater = new Updater(catalogProvider);
        updater.executeChange(changeObject);

        query(activityFormClass(1), "_id", "partner.label", "BENE");
        
        assertThat(column("_id"), hasValues("s0000000001", "s0000000002", "s0000000003", "s0000000013"));
        assertThat(column("partner.label"), hasValues("NRC", "NRC", "Solidarites", "NRC"));
        assertThat(column("BENE"), hasValues(1500, 3600, 10000, 45000));
    }
    
    @Test
    public void updateSite() {
        JsonObject changeObject = new JsonObject();
        changeObject.addProperty("@id", "s0000000001");
        changeObject.addProperty("partner", partnerInstanceId(2).asString());

        Updater updater = new Updater(catalogProvider);
        updater.executeChange(changeObject);

        query(activityFormClass(1), "_id", "partner.label", "BENE");

        assertThat(column("_id"), hasValues("s0000000001", "s0000000002", "s0000000003"));
        assertThat(column("partner.label"), hasValues("Solidarites", "NRC", "Solidarites"));
        assertThat(column("BENE"), hasValues(1500, 3600, 10000));
    }


    @Test
    public void deleteSite() {
        JsonObject changeObject = new JsonObject();
        changeObject.addProperty("@id", "s0000000001");
        changeObject.addProperty("@deleted", true);

        Updater updater = new Updater(catalogProvider);
        updater.executeChange(changeObject);
        
        query(activityFormClass(1), "_id");

        assertThat(column("_id"), hasValues("s0000000002", "s0000000003"));
    }

    @Test
    public void updateSiteWithMultipleProperties() {
        JsonObject changeObject = new JsonObject();
        changeObject.addProperty("@id", "s0000000001");
        changeObject.addProperty("partner", partnerInstanceId(2).asString());
        changeObject.addProperty("BENE", 2100);
        changeObject.addProperty(attributeGroupField(1).asString(), "Deplacement");


        Updater updater = new Updater(catalogProvider);
        updater.executeChange(changeObject);

        query(activityFormClass(1), "_id", "partner.label", "BENE", "cause");

        assertThat(column("_id"), hasValues("s0000000001", "s0000000002", "s0000000003"));
        assertThat(column("partner.label"), hasValues("Solidarites", "NRC", "Solidarites"));
        assertThat(column("BENE"), hasValues(2100, 3600, 10000));
        assertThat(column("cause"), hasValues("Deplacement", "Deplacement", "Catastrophe Naturelle"));
    }

    @Test
    public void updateSiteWithMultiAttributes() {
        JsonObject changeObject = new JsonObject();
        changeObject.addProperty("@id", "s0000000001");
        changeObject.addProperty(attributeGroupField(1).asString(), "Deplacement");
        changeObject.addProperty(attributeGroupField(2).asString(), "Casserole");

        Updater updater = new Updater(catalogProvider);
        updater.executeChange(changeObject);

        query(activityFormClass(1), "_id",  "cause", "[contenu du kit]");

        assertThat(column("_id"), hasValues("s0000000001", "s0000000002", "s0000000003"));
        assertThat(column("cause"), hasValues("Deplacement", "Deplacement", "Catastrophe Naturelle"));
    }
    
    @Test
    public void creatingActivitiesWithNullaryLocations() {

        int newId = new KeyGenerator().generateInt();

        JsonObject change = new JsonObject();
        change.addProperty("@id", CuidAdapter.cuid(SITE_DOMAIN, newId).asString());
        change.addProperty("@class", activityFormClass(ADVOCACY).asString());
        change.addProperty("partner", partnerInstanceId(1).asString());
        change.addProperty("date1", "2015-01-01");
        change.addProperty("date2", "2015-01-31");

        Updater updater = new Updater(catalogProvider);
        updater.executeChange(change);

        query(activityFormClass(ADVOCACY), "_id", "partner");
    }
}
