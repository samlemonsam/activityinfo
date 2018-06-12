package org.activityinfo.store.migrate;

import com.google.appengine.api.datastore.Text;
import com.google.appengine.tools.mapreduce.MapOnlyMapper;
import com.google.common.base.Strings;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.LoadResult;
import com.googlecode.objectify.VoidWork;
import org.activityinfo.model.form.FormInstance;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.store.hrd.Hrd;
import org.activityinfo.store.hrd.entity.FormRecordEntity;
import org.activityinfo.store.mysql.cursor.QueryExecutor;
import org.activityinfo.store.mysql.cursor.SiteFetcher;
import org.activityinfo.store.mysql.metadata.Activity;
import org.activityinfo.store.mysql.metadata.ActivityLoader;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Logger;

public class SiteMigrator extends MapOnlyMapper<Integer, Void> {

    private static final Logger LOGGER = Logger.getLogger(SiteMigrator.class.getName());

    private boolean fix;

    private transient QueryExecutor queryExecutor;
    private transient ActivityLoader activityLoader;

    public SiteMigrator(boolean fix) {
        this.fix = fix;
    }

    public SiteMigrator() {
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
        activityLoader = new ActivityLoader(queryExecutor);

    }

    @Override
    public void map(Integer siteId) {

        SiteFetcher fetcher = new SiteFetcher(activityLoader, queryExecutor);
        com.google.common.base.Optional<FormInstance> siteRecord = fetcher.fetch(siteId);

        if (!siteRecord.isPresent()) {
            throw new RuntimeException("No such site " + siteId);
        }

        Activity activity;
        try {
            activity = activityLoader.load(CuidAdapter.getLegacyIdFromCuid(siteRecord.get().getFormId()));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        FormRecordEntity recordEntity = new FormRecordEntity(activity.getSiteFormClassId(), siteRecord.get().getId());
        recordEntity.setVersion(1);
        recordEntity.setSchemaVersion(0);
        recordEntity.setFieldValues(activity.getSerializedFormClass(), siteRecord.get().getFieldValueMap());

        maybeUpdate(recordEntity);
    }

    private void maybeUpdate(FormRecordEntity recordEntity) {
        Hrd.run(new VoidWork() {
            @Override
            public void vrun() {

                Key<FormRecordEntity> recordKey = recordEntity.getKey();
                LoadResult<FormRecordEntity> existing = Hrd.ofy().load().key(recordKey);
                if(existing.now() == null) {
                    LOGGER.info("Found missing FormRecord entity: " + recordKey + stringify(recordEntity)  +
                            " fix = " + fix);

                    getContext().getCounter("missing").increment(1);

                    if(fix) {
                        Hrd.ofy().save().entity(recordEntity).now();
                        LOGGER.info("Wrote missing FormRecord entity: " + Key.create(recordEntity));
                    }

                } else {
                    if(!fieldsIdentical(recordEntity, existing.now())) {
                        getContext().getCounter("inconsistent").increment(1);
                    } else {
                        getContext().getCounter("valid").increment(1);
                    }
                }

            }
        });
    }

    private boolean fieldsIdentical(FormRecordEntity mysql, FormRecordEntity hrd) {
        Map<String, Object> mysqlProps = mysql.getFieldValues().getProperties();
        Map<String, Object> hrdProps = hrd.getFieldValues().getProperties();

        StringBuilder diff = new StringBuilder();
        boolean identical = true;

        for (String field : mysqlProps.keySet()) {

            Object mysqlValue = mysqlProps.get(field);
            Object hrdValue = hrdProps.get(field);

            if(!fieldsIdentical(mysqlValue, hrdValue)) {
                diff.append("\nField " + field + " has unequal values: MySQL = " + mysqlValue + ", HRD = " + hrdValue);
                identical = false;
            }
        }

        for (String field : hrdProps.keySet()) {
            Object hrdValue = hrdProps.get(field);

            if(!mysqlProps.containsKey(field)) {
                diff.append("\nField " + field + " has unequal values: MySQL = null, HRD = " + hrdValue);
                identical = false;
            }
        }

        if(!identical) {
            LOGGER.warning("Site " + mysql.getRecordId() + " in " + mysql.getFormId() + diff.toString());
        }

        return identical;
    }

    private boolean fieldsIdentical(Object mysqlValue, Object hrdValue) {
        if(mysqlValue instanceof Text) {
            mysqlValue = Strings.emptyToNull(((Text) mysqlValue).getValue().trim());
        }
        if(hrdValue instanceof Text) {
            hrdValue = Strings.emptyToNull(((Text) hrdValue).getValue().trim());
        }
        if(mysqlValue instanceof List && hrdValue instanceof List) {
            return setsEquivalent(((List) mysqlValue), ((List) hrdValue));
        }
        return Objects.equals(mysqlValue, hrdValue);
    }


    private boolean setsEquivalent(List<Object> mysqlList, List<Object> hrdList) {
        return mysqlList.containsAll(hrdList) && hrdList.containsAll(mysqlList);
    }


    private static String stringify(FormRecordEntity recordEntity) {
        StringBuilder s = new StringBuilder();
        s.append("formId = ").append(recordEntity.getFormId().asString());
        s.append("\nrecordId = ").append(recordEntity.getFormId().asString());
        s.append("\nversion = " ).append(recordEntity.getVersion());
        Map<String, Object> fieldValues = recordEntity.getFieldValues().getProperties();
        for (String field : fieldValues.keySet()) {
            s.append("\n").append(field).append(": ").append(fieldValues.get(field));
        }
        return s.toString();
    }


}