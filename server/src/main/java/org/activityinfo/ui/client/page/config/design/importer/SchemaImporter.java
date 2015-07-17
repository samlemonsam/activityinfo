package org.activityinfo.ui.client.page.config.design.importer;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.activityinfo.core.shared.importing.source.SourceColumn;
import org.activityinfo.core.shared.importing.source.SourceRow;
import org.activityinfo.core.shared.importing.source.SourceTable;
import org.activityinfo.legacy.client.Dispatcher;
import org.activityinfo.legacy.shared.command.BatchCommand;
import org.activityinfo.legacy.shared.command.Command;
import org.activityinfo.legacy.shared.command.CreateEntity;
import org.activityinfo.legacy.shared.command.result.BatchResult;
import org.activityinfo.legacy.shared.command.result.CreateResult;
import org.activityinfo.legacy.shared.model.*;
import org.activityinfo.ui.client.page.config.design.importer.wrapper.*;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SchemaImporter {



    interface WarningTemplates extends SafeHtmlTemplates {

        @Template("<li>Truncated <code>{0}<strike>{1}</strike></code> (Maximum length: {2} characters)</li>")
        SafeHtml truncatedValue(String retained, String truncated, int maxLen);

        @Template("<li>There is no LocationType named <code>{0}</code>, using default <code>{1}</code></li>")
        SafeHtml invalidLocationType(String name, String defaultValue);

        @Template("<li>Using default LocationType <code>{0}</code>, for activity <code>{1}</code></li>")
        SafeHtml defaultLocationType(String defaultLocationType, String activityName);

        @Template("<li>You didn't provide a column named <code>{0}</code>, " +
                  "so we'll default to <code>{1}</code>.</li>")
        SafeHtml missingColumn(String columnName, String defaultValue);
    }

    public interface ProgressListener {
        void submittingBatch(int batchNumber, int batchCount);
    }


    private Dispatcher dispatcher;
    private UserDatabaseDTO db;

    private ProgressListener listener;

    private AsyncCallback<Void> callback;

    private Map<String, ActivityFormDTO> activityMap = Maps.newHashMap();
    private Map<String, Integer> locationTypeMap = Maps.newHashMap();

    private List<ActivityFormDTO> newActivities = Lists.newArrayList();
    private List<DtoWrapper> newIndicators = Lists.newArrayList();
    private List<DtoWrapper> newAttributeGroups = Lists.newArrayList();
    private List<DtoWrapper> newAttributes = Lists.newArrayList();

    private Set<SafeHtml> warnings = Sets.newHashSet();


    public class Column {
        private int index;
        private String name;
        private int maxLength;
        private String defaultValue;

        public Column(int index, String name, String defaultValue, int maxLength) {
            super();
            this.index = index;
            this.name = name;
            this.defaultValue = defaultValue;
            this.maxLength = maxLength;
        }

        public String get(SourceRow row) {
            if (index < 0) {
                return Strings.emptyToNull(defaultValue);
            }
            String value = row.getColumnValue(index);
            if (value.length() <= maxLength) {
                return Strings.emptyToNull(value.trim());

            } else {
                String retainedValue = value.substring(0, maxLength);
                String truncatedValue = value.substring(maxLength);
                warnings.add(templates.truncatedValue(retainedValue, truncatedValue, maxLength));
                return retainedValue;
            }
        }

        public boolean isMissing() {
            return index < 0;
        }

        public String getName() {
            return name;
        }

        public int getMaxLength() {
            return maxLength;
        }
    }

    // columns
    private Column activityCategory;
    private Column activityName;

    private SourceTable source;
    private Column formFieldType;
    private Column fieldName;
    private Column fieldCategory;
    private Column fieldDescription;
    private Column fieldUnits;
    private Column fieldMandatory;
    private Column multipleAllowed;
    private Column attributeValue;
    private Column locationType;
    private Column reportingFrequency;

    private int batchNumber;
    private int batchCount;

    private List<String> missingColumns = Lists.newArrayList();

    private LocationTypeDTO defaultLocationType;
    private boolean fatalError;

    private final WarningTemplates templates;

    SchemaImporter(Dispatcher dispatcher, UserDatabaseDTO db, WarningTemplates templates) {
        this.dispatcher = dispatcher;
        this.db = db;
        this.templates = templates;

//        for (ActivityFormDTO activity : db.getActivities()) {
//            activityMap.put(activity.getName() + activity.getCategory(), activity);
//        }
        for (LocationTypeDTO locationType : db.getCountry().getLocationTypes()) {
            locationTypeMap.put(locationType.getName().toLowerCase(), locationType.getId());
        }
        defaultLocationType = db.getCountry().getLocationTypes().iterator().next();
    }

    public SchemaImporter(Dispatcher service, UserDatabaseDTO db) {
        this(service, db, GWT.<WarningTemplates>create(WarningTemplates.class));
    }

    public void setProgressListener(ProgressListener listener) {
        this.listener = listener;
    }


    public boolean parseColumns(SourceTable source) {
        this.source = source;
        this.source.parseAllRows();
        findColumns();
        return missingColumns.isEmpty();
    }

    public List<String> getMissingColumns() {
        return missingColumns;
    }

    public boolean processRows() {
        processRows(source);
        return !fatalError;
    }

    private void processRows(SourceTable source) {
        for (SourceRow row : source.getRows()) {
            ActivityFormDTO activity = getActivity(row);
            String fieldType = formFieldType.get(row);
            if ("Indicator".equals(fieldType)) {
                DtoWrapper indicatorWrapper = new DtoWrapper(new IndicatorKey(activity.getName(), fieldName.get(row), fieldCategory.get(row)));
                if (!newIndicators.contains(indicatorWrapper)) {
                    IndicatorDTO indicator = new IndicatorDTO();
                    indicator.setName(fieldName.get(row));
                    indicator.setCategory(fieldCategory.get(row));
                    indicator.setDescription(fieldDescription.get(row));
                    indicator.setUnits(fieldUnits.get(row));
                    indicator.set("activityId", activity);
                    if (isTruthy(fieldMandatory.get(row))) {
                        indicator.setMandatory(true);
                    }
                    indicatorWrapper.setDto(indicator);
                    newIndicators.add(indicatorWrapper);
                }
            } else if ("AttributeGroup".equals(fieldType)) {
                String name = fieldName.get(row);
                AttributeGroupDTO group = activity.getAttributeGroupByName(name);
                DtoWrapper attributeGroupWrapper = new DtoWrapper(new AttributeGroupKey(activity.getName(), name));
                if (group == null && !newAttributeGroups.contains(attributeGroupWrapper)) {
                    group = new AttributeGroupDTO();
                    group.setId(-1);
                    group.setName(name);
                    group.set("activityId", activity);

                    if (isTruthy(multipleAllowed.get(row))) {
                        group.setMultipleAllowed(true);
                    }
                    if (isTruthy(fieldMandatory.get(row))) {
                        group.setMandatory(true);
                    }
                    activity.getAttributeGroups().add(group);
                    attributeGroupWrapper.setDto(group);
                    newAttributeGroups.add(attributeGroupWrapper);
                }

                String attribName = attributeValue.get(row);
                AttributeDTO attrib = findAttrib(group, attribName);
                DtoWrapper attributeWrapper = new DtoWrapper(new AttributeKey((AttributeGroupKey) attributeGroupWrapper.getKey(), attribName));
                if (attrib == null && !newAttributes.contains(attributeWrapper)) {
                    attrib = new AttributeDTO();
                    attrib.setId(-1);
                    attrib.setName(attribName);
                    attrib.set("attributeGroupId", group);
                    attributeWrapper.setDto(attrib);
                    newAttributes.add(attributeWrapper);
                }
            }
        }
    }

    private AttributeDTO findAttrib(AttributeGroupDTO group, String attribName) {
        for (AttributeDTO attrib : group.getAttributes()) {
            if (attrib.getName().equals(attribName)) {
                return attrib;
            }
        }
        return null;
    }


    public Set<SafeHtml> getWarnings() {
        return warnings;
    }

    private boolean isTruthy(String columnValue) {
        if (columnValue == null) {
            return false;
        }
        String loweredValue = columnValue.toLowerCase().trim();
        return loweredValue.equals("1") ||
               loweredValue.startsWith("t") || // true
               loweredValue.startsWith("y");   // yes

    }

    private ActivityFormDTO getActivity(SourceRow row) {
        String name = activityName.get(row);
        String category = activityCategory.get(row);

        ActivityFormDTO activity = activityMap.get(name + category);
        if (activity == null) {
            activity = new ActivityFormDTO();
            activity.set("databaseId", db.getId());
            activity.setName(name);
            activity.setCategory(category);
            activity.setLocationTypeId(findLocationType(activity, row));

            String frequency = Strings.nullToEmpty(reportingFrequency.get(row));
            if (frequency.toLowerCase().contains("month")) {
                activity.setReportingFrequency(ActivityFormDTO.REPORT_MONTHLY);
            }

            activityMap.put(name + category, activity);
            newActivities.add(activity);
        }

        return activity;
    }

    private int findLocationType(ActivityFormDTO activity, SourceRow row) {
        String name = locationType.get(row);
        if (Strings.isNullOrEmpty(name)) {
            warnings.add(templates.defaultLocationType(defaultLocationType.getName(), activity.getName()));
            return defaultLocationType.getId();
        } else {
            Integer typeId = locationTypeMap.get(name.toLowerCase());
            if (typeId == null) {
                warnings.add(templates.invalidLocationType(name, defaultLocationType.getName()));
                return defaultLocationType.getId();
            }
            return typeId;
        }
    }

    private int findColumnIndex(String name) {
        for (SourceColumn col : source.getColumns()) {
            if (col.getHeader().equalsIgnoreCase(name)) {
                return col.getIndex();
            }
        }
        return -1;
    }

    private Column findColumn(String name) {
        return findColumn(name, null, Integer.MAX_VALUE);
    }

    private Column findColumn(String name, String defaultValue) {
        return findColumn(name, defaultValue, Integer.MAX_VALUE);
    }

    private Column findColumn(String name, int maxLength) {
        return findColumn(name, null, maxLength);
    }

    private Column findColumn(String name, String defaultValue, int maxLength) {
        int col = findColumnIndex(name);
        if (col == -1) {
            if (defaultValue == null) {
                missingColumns.add(name);
            } else if(!Strings.isNullOrEmpty(defaultValue)) {
                warnings.add(templates.missingColumn(name, defaultValue));
            }
        }
        return new Column(col, col == -1 ? name : source.getColumnHeader(col), defaultValue, maxLength);
    }

    private void findColumns() {
        missingColumns.clear();
        activityCategory = findColumn("ActivityCategory", "", 255);
        activityName = findColumn("ActivityName", 45);
        locationType = findColumn("LocationType", defaultLocationType.getName());
        formFieldType = findColumn("FormFieldType", "quantity");
        fieldName = findColumn("Name");
        fieldCategory = findColumn("Category", "", 50);
        fieldDescription = findColumn("Description", "");
        fieldUnits = findColumn("Units", 15);
        fieldMandatory = findColumn("Mandatory", "false");
        multipleAllowed = findColumn("multipleAllowed", "false");
        attributeValue = findColumn("AttributeValue", 50);
        reportingFrequency = findColumn("ReportingFrequency", "once");
    }

    public void persist(AsyncCallback<Void> callback) {
        this.callback = callback;

        List<List<? extends EntityDTO>> batches = Lists.newArrayList();
        batches.add(newActivities);
        batches.add(getNewIndicators());
        batches.add(getNewAttributeGroups());
        batches.add(getNewAttributes());

        batchCount = batches.size();
        batchNumber = 1;

        persistBatch(batches.iterator());
    }

    private void persistBatch(final Iterator<List<? extends EntityDTO>> batchIterator) {
        BatchCommand batchCommand = new BatchCommand();
        final List<? extends EntityDTO> batch = batchIterator.next();
        for (EntityDTO entity : batch) {
            batchCommand.add(create(entity));
        }
        listener.submittingBatch(batchNumber++, batchCount);

        dispatcher.execute(batchCommand, new AsyncCallback<BatchResult>() {

            @Override
            public void onFailure(Throwable caught) {
                callback.onFailure(caught);
            }

            @Override
            public void onSuccess(BatchResult result) {
                for (int i = 0; i != result.getResults().size(); ++i) {
                    CreateResult createResult = result.getResult(i);
                    batch.get(i).set("id", createResult.getNewId());
                }
                if (batchIterator.hasNext()) {
                    persistBatch(batchIterator);
                } else {
                    callback.onSuccess(null);
                }
            }
        });
    }

    private Command<CreateResult> create(EntityDTO dto) {
        Map<String, Object> map = Maps.newHashMap();
        for (String propertyName : dto.getPropertyNames()) {
            Object value = dto.get(propertyName);
            if (value instanceof EntityDTO) {
                map.put(propertyName, ((EntityDTO) value).getId());
            } else {
                map.put(propertyName, value);
            }
        }
        return new CreateEntity(dto.getEntityName(), map);
    }

    public void clearWarnings() {
        warnings.clear();
        fatalError = false;
    }

    public List<ActivityFormDTO> getNewActivities() {
        return newActivities;
    }

    public List<EntityDTO> getNewIndicators() {
        return Wrappers.asDto(newIndicators);
    }

    public List<EntityDTO> getNewAttributeGroups() {
        return Wrappers.asDto(newAttributeGroups);
    }

    public List<EntityDTO> getNewAttributes() {
        return Wrappers.asDto(newAttributes);
    }
}
