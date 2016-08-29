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
import org.activityinfo.store.mysql.metadata.CountryStructure;
import org.activityinfo.store.mysql.side.AdminColumnBuilder;

import java.sql.SQLException;


public class LocationQueryBuilder implements ColumnQueryBuilder {

    private QueryExecutor executor;
    private int locationTypeId;
    private MySqlCursorBuilder baseTableBuilder;
    private AdminColumnBuilder adminColumnBuilder;
    private ResourceId formClassId;
    private TableMapping tableMapping;
    private boolean hasWhereSet = false;

    public LocationQueryBuilder(QueryExecutor executor, TableMapping tableMapping, CountryStructure country) {
        this.executor = executor;
        this.locationTypeId = CuidAdapter.getLegacyIdFromCuid(tableMapping.getFormClass().getId());
        this.tableMapping = tableMapping;
        formClassId = CuidAdapter.locationFormClass(locationTypeId);
        baseTableBuilder = new MySqlCursorBuilder(tableMapping, executor);
        adminColumnBuilder = new AdminColumnBuilder(locationTypeId, country);
    }

    @Override
    public void only(ResourceId resourceId) {
        hasWhereSet = true;
        baseTableBuilder.where("base." + tableMapping.getPrimaryKey().getColumnName() + "=" + CuidAdapter.getLegacyIdFromCuid(resourceId)
                + " AND base.workflowStatusId != 'rejected'");
    }

    @Override
    public void addResourceId(CursorObserver<ResourceId> observer) {
        baseTableBuilder.addResourceId(observer);
    }

    @Override
    public void addField(ResourceId fieldId, CursorObserver<FieldValue> observer) {
        if(fieldId.equals(CuidAdapter.field(formClassId, CuidAdapter.ADMIN_FIELD))) {
            adminColumnBuilder.addObserver(observer);
        } else {
            baseTableBuilder.addField(fieldId, observer);
        }
    }

    @Override
    public void execute() {
        if (!hasWhereSet) {
            baseTableBuilder.where("base.workflowStatusId != 'rejected'");
        }

        // Emit all of the base columns 
        Cursor cursor = baseTableBuilder.open();
        while(cursor.next()) {
        }
        
        // If we need the adminlevel, then we need to scan the locationadminlink table
        if(adminColumnBuilder.hasObservers()) {
            try {
                adminColumnBuilder.execute(executor);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
