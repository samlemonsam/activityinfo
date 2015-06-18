package org.activityinfo.store.mysql.collections;

import com.google.common.base.Optional;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.service.store.ResourceCollection;
import org.activityinfo.store.mysql.cursor.QueryExecutor;
import org.activityinfo.store.mysql.mapping.ActivityTableMappingBuilder;

import java.sql.SQLException;


public class ReportingPeriodCollectionProvider implements CollectionProvider {
    @Override
    public boolean accept(ResourceId formClassId) {
        return formClassId.getDomain() == CuidAdapter.MONTHLY_REPORT_FORM_CLASS;
    }

    @Override
    public ResourceCollection getAccessor(QueryExecutor executor, ResourceId formClassId) throws SQLException {

        Activity activity = Activity.query(executor, CuidAdapter.getLegacyIdFromCuid(formClassId));
        ActivityTableMappingBuilder mapping = ActivityTableMappingBuilder.reportingPeriod(activity);

        return new SiteCollection(activity, mapping.build(), executor);
    }

    @Override
    public Optional<ResourceId> lookupCollection(QueryExecutor executor, ResourceId resourceId) throws SQLException {
        return Optional.absent();
    }
}
