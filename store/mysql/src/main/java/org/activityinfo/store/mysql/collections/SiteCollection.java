package org.activityinfo.store.mysql.collections;

import com.google.common.base.Optional;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.Resource;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.resource.ResourceUpdate;
import org.activityinfo.model.resource.Resources;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.service.store.ColumnQueryBuilder;
import org.activityinfo.service.store.ResourceCollection;
import org.activityinfo.store.mysql.cursor.QueryExecutor;
import org.activityinfo.store.mysql.mapping.TableMapping;
import org.activityinfo.store.mysql.update.AttributeValueTableUpdater;
import org.activityinfo.store.mysql.update.BaseTableInserter;
import org.activityinfo.store.mysql.update.BaseTableUpdater;
import org.activityinfo.store.mysql.update.IndicatorValueTableUpdater;

import java.sql.SQLException;
import java.util.Date;
import java.util.Map;

/**
 * Collection of Sites
 */
public class SiteCollection implements ResourceCollection {
    
    private final Activity activity;
    private final TableMapping baseMapping;
    private final QueryExecutor queryExecutor;

    public SiteCollection(Activity activity, TableMapping baseMapping, QueryExecutor queryExecutor) {
        this.activity = activity;
        this.baseMapping = baseMapping;
        this.queryExecutor = queryExecutor;
    }

    @Override
    public Optional<Resource> get(ResourceId resourceId) {
        Resource resource = Resources.createResource();
        resource.setId(resourceId);
        resource.setOwnerId(getFormClass().getId());

        try {
            if(!baseMapping.queryFields(queryExecutor, resource)) {
                return Optional.absent();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        
        return Optional.of(resource);
    }

    @Override
    public FormClass getFormClass() {
        return baseMapping.getFormClass();
    }



    @Override
    public void add(ResourceUpdate update) {
        ResourceId formClassId = getFormClass().getId();
        BaseTableInserter baseTable = new BaseTableInserter(baseMapping, update.getResourceId());
        baseTable.addValue("ActivityId", activity.getId());
        baseTable.addValue("DateCreated", new Date());
        baseTable.addValue("DateEdited", new Date());

        IndicatorValueTableUpdater indicatorValues = new IndicatorValueTableUpdater(update.getResourceId());
        AttributeValueTableUpdater attributeValues = new AttributeValueTableUpdater(activity, update.getResourceId());

        for (Map.Entry<ResourceId, FieldValue> change : update.getChangedFieldValues().entrySet()) {
            if(change.getKey().getDomain() == CuidAdapter.INDICATOR_DOMAIN) {
                indicatorValues.update(change.getKey(), change.getValue());
            } else if(change.getKey().getDomain() == CuidAdapter.ATTRIBUTE_GROUP_FIELD_DOMAIN) {
                attributeValues.update(change.getKey(), change.getValue());
            } else {
                baseTable.set(change.getKey(), change.getValue());
            }
            if(change.getKey().equals(CuidAdapter.field(formClassId, CuidAdapter.START_DATE_FIELD))) {
                indicatorValues.setDate1(change.getValue());
            } else if(change.getKey().equals(CuidAdapter.field(formClassId, CuidAdapter.END_DATE_FIELD))) {
                indicatorValues.setDate2(change.getValue());
            }
        }
        baseTable.executeInsert(queryExecutor);
        attributeValues.executeUpdates(queryExecutor);
        indicatorValues.insert(queryExecutor);
    }

    @Override
    public void update(ResourceUpdate update) {
    
        BaseTableUpdater baseTable = new BaseTableUpdater(baseMapping, update.getResourceId());
        IndicatorValueTableUpdater indicatorValues = new IndicatorValueTableUpdater(update.getResourceId());
        AttributeValueTableUpdater attributeValues = new AttributeValueTableUpdater(activity, update.getResourceId());

        for (Map.Entry<ResourceId, FieldValue> change : update.getChangedFieldValues().entrySet()) {
            if(change.getKey().getDomain() == CuidAdapter.INDICATOR_DOMAIN) {
                indicatorValues.update(change.getKey(), change.getValue());
            } else if(change.getKey().getDomain() == CuidAdapter.ATTRIBUTE_GROUP_FIELD_DOMAIN) {
                attributeValues.update(change.getKey(), change.getValue());
            } else {
                baseTable.update(change.getKey(), change.getValue());
            }
        }
        try {
            baseTable.executeUpdates(queryExecutor);
            indicatorValues.execute(queryExecutor);
            attributeValues.executeUpdates(queryExecutor);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public ColumnQueryBuilder newColumnQuery() {
        return new SiteColumnQueryBuilder(activity, baseMapping, queryExecutor);
    }
}
