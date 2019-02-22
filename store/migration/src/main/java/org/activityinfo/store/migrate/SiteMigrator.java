package org.activityinfo.store.migrate;

import com.google.appengine.tools.mapreduce.MapOnlyMapper;
import com.google.common.base.Optional;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.LoadResult;
import com.googlecode.objectify.VoidWork;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.form.TypedFormRecord;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.store.hrd.FieldConverter;
import org.activityinfo.store.hrd.FieldConverters;
import org.activityinfo.store.hrd.Hrd;
import org.activityinfo.store.hrd.entity.FormRecordEntity;
import org.activityinfo.store.mysql.cursor.QueryExecutor;
import org.activityinfo.store.mysql.cursor.SiteFetcher;
import org.activityinfo.store.mysql.metadata.Activity;
import org.activityinfo.store.mysql.metadata.ActivityLoader;

import java.sql.SQLException;
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
        com.google.common.base.Optional<TypedFormRecord> siteRecord = fetcher.fetch(siteId);

        if (!siteRecord.isPresent()) {
            throw new RuntimeException("No such site " + siteId);
        }

        Activity activity;
        try {
            activity = activityLoader.load(CuidAdapter.getLegacyIdFromCuid(siteRecord.get().getFormId()));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        FormClass formClass = activity.getSerializedFormClass();

        FormRecordEntity recordEntity = new FormRecordEntity(activity.getSiteFormClassId(), siteRecord.get().getId());
        recordEntity.setVersion(1);
        recordEntity.setSchemaVersion(0);
        recordEntity.setFieldValues(formClass, siteRecord.get().getFieldValueMap());

        maybeUpdate(formClass, recordEntity);
    }

    private void maybeUpdate(FormClass formClass, FormRecordEntity recordEntity) {
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
                    if(!fieldsIdentical(formClass, recordEntity, existing.now())) {
                        getContext().getCounter("inconsistent").increment(1);
                    } else {
                        getContext().getCounter("valid").increment(1);
                    }
                }

            }
        });
    }

    private boolean fieldsIdentical(FormClass formClass, FormRecordEntity mysql, FormRecordEntity hrd) {
        Map<String, Object> mysqlProps = mysql.getFieldValues().getProperties();
        Map<String, Object> hrdProps = hrd.getFieldValues().getProperties();

        StringBuilder diff = new StringBuilder();
        boolean identical = true;

        for (String fieldId : mysqlProps.keySet()) {
            Optional<FormField> field = formClass.getFieldIfPresent(ResourceId.valueOf(fieldId));
            if(!field.isPresent()) {
                continue;
            }
            FieldConverter<?> fieldConverter = FieldConverters.forType(field.get().getType());

            Object mysqlValue = convertNullable(fieldConverter, mysqlProps.get(fieldId));
            Object hrdValue = convertNullable(fieldConverter, hrdProps.get(fieldId));

            if(!Objects.equals(mysqlValue, hrdValue)) {
                diff.append("\nField " + fieldId + " has unequal values: MySQL = " + mysqlValue + ", HRD = " + hrdValue);
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

    private Object convertNullable(FieldConverter<?> fieldConverter, Object value) {
        if(value == null) {
            return null;
        }
        try {
            return fieldConverter.toFieldValue(value);
        } catch (Exception e) {
            LOGGER.warning("Failed to convert value " + value);
            return value;
        }
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