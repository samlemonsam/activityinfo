package org.activityinfo.store.migrate;

import com.google.appengine.api.datastore.*;
import com.google.appengine.tools.pipeline.Job0;
import com.google.appengine.tools.pipeline.Value;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.store.hrd.entity.FormEntity;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.logging.Logger;

public class HrdPrimary extends Job0<Void> {

    private static final Logger LOGGER = Logger.getLogger(HrdPrimary.class.getName());

    private int activityId;

    public HrdPrimary(int activityId) {
        this.activityId = activityId;
    }

    @Override
    public Value<Void> run() throws Exception {

        MySqlQueryExecutor executor = connect();

        // Do a final check that MySQL and HRD match

        IntSet mysqlRecords = new IntOpenHashSet();
        try(ResultSet sites = executor.query("select siteid from site where activityid=? and deleted=0", Collections.singletonList(activityId))) {
            while(sites.next()) {
                int siteId = sites.getInt(1);
                mysqlRecords.add(siteId);
            }
        }

        Query query = new Query("FormRecord")
                .setAncestor(FormEntity.key(CuidAdapter.activityFormClass(activityId)).getRaw())
                .setKeysOnly();

        IntSet hrdRecords = new IntOpenHashSet();
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        Transaction tx = datastore.beginTransaction();
        PreparedQuery preparedQuery = datastore.prepare(tx, query);
        for (Entity entity : preparedQuery.asIterable(FetchOptions.Builder.withChunkSize(500))) {
            String recordId = entity.getKey().getName();
            int siteId;
            try {
                siteId = Integer.parseInt(recordId.substring(1));
                hrdRecords.add(siteId);
            } catch (Exception e) {
                LOGGER.severe("Invalid record id " + entity.getKey());
            }
        }
        tx.rollback();


        boolean consistent = true;

        // Now check...
        IntIterator mysqlIt = mysqlRecords.iterator();
        while(mysqlIt.hasNext()) {
            int siteId = mysqlIt.nextInt();
            if(!hrdRecords.contains(siteId)) {
                LOGGER.severe("Activity " + activityId + " is missing " + siteId + " in HRD");
                consistent = false;
            }
        }
        if(mysqlRecords.size() != hrdRecords.size()) {
            IntIterator hrdIt = hrdRecords.iterator();
            while(hrdIt.hasNext()) {
                int siteId = hrdIt.nextInt();
                if(!mysqlRecords.contains(siteId)) {
                    LOGGER.severe("Activity " + activityId + " is missing " + siteId + " in MySQL");
                    consistent = false;
                }
            }
        }


        // Set the hrd flag to 1 in the activity table
        if(consistent) {

            LOGGER.info("Consistent! Mysql count = " + mysqlRecords.size() + ", Hrd count = " + hrdRecords.size());

            executor.update("update activity set hrd = 1 where activityid=?", Collections.singletonList(activityId));

            return futureCall(new BlockJob(CuidAdapter.activityFormClass(activityId).asString()));

        } else {
            return null;
        }
    }

    private MySqlQueryExecutor connect() throws IOException {
        try {
            return new MySqlQueryExecutor();
        } catch (SQLException e) {
            throw new IOException("Failed to open connection to MySQL", e);
        }
    }
}
