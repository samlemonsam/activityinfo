package org.activityinfo.store.mysql;


import com.google.appengine.api.datastore.QueryResultIterable;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import org.activityinfo.model.form.CatalogEntry;
import org.activityinfo.model.form.CatalogEntryType;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.store.hrd.Hrd;
import org.activityinfo.store.hrd.entity.AnalysisEntity;

import java.util.Collections;

public class ReportFolder {

    public static Iterable<CatalogEntry> queryReports(String parentId) {

        if(parentId.charAt(0) != CuidAdapter.DATABASE_DOMAIN) {
            return Collections.emptyList();
        }

        QueryResultIterable<AnalysisEntity> result = Hrd.ofy().load()
            .type(AnalysisEntity.class)
            .filter("parentId", parentId)
            .iterable();

        return Iterables.transform(result, new Function<AnalysisEntity, CatalogEntry>() {
            @Override
            public CatalogEntry apply(AnalysisEntity analysisEntity) {
                return new CatalogEntry(analysisEntity.getId(), analysisEntity.getLabel(), CatalogEntryType.ANALYSIS);
            }
        });
    }
}
