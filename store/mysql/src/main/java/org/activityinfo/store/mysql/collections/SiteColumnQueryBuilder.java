package org.activityinfo.store.mysql.collections;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Maps;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.service.store.ColumnQueryBuilder;
import org.activityinfo.service.store.Cursor;
import org.activityinfo.service.store.CursorObserver;
import org.activityinfo.store.mysql.cursor.MySqlCursorBuilder;
import org.activityinfo.store.mysql.cursor.QueryExecutor;
import org.activityinfo.store.mysql.mapping.TableMapping;
import org.activityinfo.store.mysql.side.SideColumnBuilder;

import java.util.Map;
import java.util.logging.Logger;

public class SiteColumnQueryBuilder implements ColumnQueryBuilder {
    
    private static final Logger LOGGER = Logger.getLogger(SiteColumnQueryBuilder.class.getName());
    
    private final Activity activity;
    private final TableMapping tableMapping;
    private QueryExecutor executor;
    private final MySqlCursorBuilder baseCursor;
    private final SideColumnBuilder indicators;
    private final SideColumnBuilder attributes;

    private Map<ResourceId, ActivityField> fieldMap = Maps.newHashMap();
    
    public SiteColumnQueryBuilder(Activity activity, TableMapping tableMapping, QueryExecutor executor) {
        this.activity = activity;
        this.tableMapping = tableMapping;
        this.executor = executor;
        this.baseCursor = new MySqlCursorBuilder(tableMapping, executor);
        this.indicators = new SideColumnBuilder(tableMapping.getFormClass());
        this.attributes = new SideColumnBuilder(tableMapping.getFormClass());
        
        for(ActivityField field : activity.getFields()) {
            fieldMap.put(field.getResourceId(), field);
        }
    }
    
    @Override
    public void only(ResourceId resourceId) {
        baseCursor.only(resourceId);
        indicators.only(resourceId);
        attributes.only(resourceId);
    }

    @Override
    public void addResourceId(CursorObserver<ResourceId> observer) {
        baseCursor.addResourceId(observer);
    }

    @Override
    public void addField(ResourceId fieldId, CursorObserver<FieldValue> observer) {
        if(tableMapping.getMapping(fieldId) != null) {
            baseCursor.addField(fieldId, observer);
        } else {
            ActivityField field = fieldMap.get(fieldId);
            if(field == null) {
                throw new IllegalArgumentException("fieldId: " + fieldId);
            }
            if(field.isIndicator()) {
                indicators.add(field, observer);
            } else {
                attributes.add(field, observer);
            }
        }
    }

    @Override
    public void execute() {
        
        try {

            Stopwatch stopwatch = Stopwatch.createStarted();
            
            // Run base table
            Cursor cursor = baseCursor.open();
            while (cursor.next()) {
            }
            
            stopwatch.stop();
            
            LOGGER.fine("Scanned site table in " + stopwatch);

            stopwatch.reset().start();
            
            // Run indicator loop
            if (!indicators.isEmpty()) {
                indicators.sitesIndicators(activity.getId(), executor);
                
                LOGGER.fine("Scanned indicatorValue table in " + stopwatch);
            }
            
            
            stopwatch.reset().start();
            
            if (!attributes.isEmpty()) {
                attributes.attributes(activity.getId(), executor);

                LOGGER.fine("Scanned attributeValue in " + stopwatch);
            }
            
            
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
