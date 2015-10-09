package org.activityinfo.store.mysql.collections;

import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.service.store.ColumnQueryBuilder;
import org.activityinfo.service.store.Cursor;
import org.activityinfo.service.store.CursorObserver;
import org.activityinfo.store.mysql.cursor.MySqlCursorBuilder;
import org.activityinfo.store.mysql.cursor.QueryExecutor;
import org.activityinfo.store.mysql.mapping.TableMapping;

import java.io.IOException;
import java.sql.SQLException;


public class LocationQueryBuilder implements ColumnQueryBuilder {

    private QueryExecutor executor;
    private CountryStructure country;
    private int locationTypeId;
    private MySqlCursorBuilder baseTableBuilder;
    private LocationLinkScanner locationLinkScanner;
    
    public LocationQueryBuilder(QueryExecutor executor, TableMapping tableMapping, CountryStructure country) {
        this.executor = executor;
        this.country = country;
        this.locationTypeId = CuidAdapter.getLegacyIdFromCuid(tableMapping.getFormClass().getId());
        baseTableBuilder = new MySqlCursorBuilder(tableMapping, executor);
        locationLinkScanner = new LocationLinkScanner(locationTypeId, country);
    }

    @Override
    public void only(ResourceId resourceId) {
        baseTableBuilder.only(resourceId);
    }

    @Override
    public void addResourceId(CursorObserver<ResourceId> observer) {
        baseTableBuilder.addResourceId(observer);
    }

    @Override
    public void addField(ResourceId fieldId, CursorObserver<FieldValue> observer) {
        if(fieldId.equals(LocationCollection.ADMIN_FIELD_ID)) {
            locationLinkScanner.addObserver(observer);
        } else {
            baseTableBuilder.addField(fieldId, observer);
        }
    }

    @Override
    public void execute() throws IOException {

        // Emit all of the base columns 
        Cursor cursor = baseTableBuilder.open();
        while(cursor.next()) {
        }
        
        // If we need the adminlevel, then we need to scan the locationadminlink table
        if(locationLinkScanner.hasObservers()) {
            try {
                locationLinkScanner.execute(executor);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
