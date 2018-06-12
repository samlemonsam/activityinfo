package org.activityinfo.store.migrate;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.tools.mapreduce.DatastoreMutationPool;
import com.google.appengine.tools.mapreduce.MapOnlyMapper;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.store.mysql.cursor.QueryExecutor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.logging.Logger;

/**
 * Synchronizes deleted sites to HRD. During the period where mysql store updates
 * were not wrapped in a transaction, a very small percentage of deletes were
 * written to HRD, but not to MySQL.
 */
public class DeletedSiteFixer extends MapOnlyMapper<Entity, Void> {

    private static final Logger LOGGER = Logger.getLogger(SiteMigrator.class.getName());

    private boolean fix;

    private transient QueryExecutor queryExecutor;
    private transient DatastoreMutationPool mutationPool;

    public DeletedSiteFixer(boolean fix) {
        this.fix = fix;
    }

    public DeletedSiteFixer() {
        this(false);
    }

    @Override
    public void beginSlice() {
        super.beginSlice();
        try {
            queryExecutor = new MySqlQueryExecutor();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        mutationPool = DatastoreMutationPool.create();
    }

    @Override
    public void endSlice() {
        super.endSlice();
        mutationPool.flush();
    }

    @Override
    public void map(Entity recordEntity) {

        String formId = recordEntity.getKey().getParent().getName();
        String recordId = recordEntity.getKey().getName();

        if(!isSiteRecord(formId, recordId)) {
            getContext().getCounter("non-site").increment(1);
            return;
        }

        getContext().getCounter("site").increment(1);

        int siteId = CuidAdapter.getLegacyIdFromCuid(recordId);

        if(!CuidAdapter.cuid(CuidAdapter.SITE_DOMAIN, siteId).asString().equals(recordId)) {
            LOGGER.severe("Site id " + recordEntity.getKey() + " malformed.");
            getContext().getCounter("malformed").increment(1);
            return;
        }

        try (ResultSet site = queryExecutor.query("select dateDeleted from site s " +
                "where siteid = ?", siteId)) {

            if(!site.next()) {
                LOGGER.warning("Site missing for " + recordEntity.getKey());
                getContext().getCounter("missing").increment(1);

                if(fix) {
                    delete(recordEntity);
                }

                return;
            }

            Date dateDeleted = site.getDate(1);
            if(site.wasNull()) {
                dateDeleted = null;
            }

            if(dateDeleted != null) {
                LOGGER.warning("Site deleted " + recordEntity.getKey());
                getContext().getCounter("deleted").increment(1);

                if(fix) {
                    delete(recordEntity);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void delete(Entity recordEntity) {
        LOGGER.warning("Deleting record " + recordEntity.getKey());
        mutationPool.delete(recordEntity.getKey());
    }

    private boolean isSiteRecord(String formId, String recordId) {
        return formId.startsWith("a") && recordId.startsWith("s");
    }
}
