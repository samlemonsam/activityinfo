package org.activityinfo.ui.client.page.config.design.importer;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.activityinfo.legacy.shared.command.BatchCommand;
import org.activityinfo.legacy.shared.command.Command;
import org.activityinfo.legacy.shared.command.CreateEntity;
import org.activityinfo.legacy.shared.command.result.BatchResult;
import org.activityinfo.legacy.shared.command.result.CreateResult;
import org.activityinfo.legacy.shared.model.*;
import org.activityinfo.ui.client.component.importDialog.model.source.SourceRow;
import org.activityinfo.ui.client.component.importDialog.model.source.SourceTable;
import org.activityinfo.ui.client.dispatch.Dispatcher;
import org.activityinfo.ui.client.page.config.design.importer.wrapper.*;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class SchemaImporterV2 extends SchemaImporter {


    private static final int MAX_BATCH_SIZE = 40;

    private Dispatcher dispatcher;
    private UserDatabaseDTO db;

    private AsyncCallback<Void> callback;

    private Map<String, ActivityFormDTO> activityMap = Maps.newHashMap();
    private Map<String, Integer> locationTypeMap = Maps.newHashMap();

    private List<ActivityFormDTO> newActivities = Lists.newArrayList();
    private List<DtoWrapper> newIndicators = Lists.newArrayList();
    private List<DtoWrapper> newAttributeGroups = Lists.newArrayList();
    private List<DtoWrapper> newAttributes = Lists.newArrayList();


    // columns
    private Column activityCategory;
    private Column formVersion;
    private Column formName;
    private Column formFieldType;
    private Column fieldName;
    private Column fieldCategory;
    private Column fieldDescription;
    private Column fieldUnits;
    private Column fieldRequired;
    private Column multipleAllowed;
    private Column choiceLabel;
    private Column locationType;
    private Column reportingFrequency;
    private Column fieldCode;
    private Column fieldExpression;

    private int batchNumber;
    private int batchCount;

    private LocationTypeDTO defaultLocationType;

    public SchemaImporterV2(Dispatcher dispatcher, UserDatabaseDTO db, WarningTemplates templates) {
        super(templates);
        this.dispatcher = dispatcher;
        this.db = db;

        for (LocationTypeDTO locationType : db.getCountry().getLocationTypes()) {
            locationTypeMap.put(locationType.getName().toLowerCase(), locationType.getId());
        }
        defaultLocationType = db.getCountry().getLocationTypes().iterator().next();
    }

    public SchemaImporterV2(Dispatcher service, UserDatabaseDTO db) {
        this(service, db, GWT.<WarningTemplates>create(WarningTemplates.class));
    }




    public boolean isHasSkippedAttributes() {
        return hasSkippedAttributes;
    }

    @Override
    public boolean processRows() {
        processRows(source);
        return !fatalError;
    }

    private void processRows(SourceTable source) {
        int rowCount = 1;
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
                    indicator.setNameInExpression(fieldCode.get(row));
                    indicator.setExpression(fieldExpression.get(row));
                    if (isTruthy(fieldRequired.get(row))) {
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
                    if (isTruthy(fieldRequired.get(row))) {
                        group.setMandatory(true);
                    }
                    activity.getAttributeGroups().add(group);
                    attributeGroupWrapper.setDto(group);
                    newAttributeGroups.add(attributeGroupWrapper);
                }

                String attribName = choiceLabel.get(row);
                AttributeDTO attrib = findAttrib(group, attribName);
                DtoWrapper attributeWrapper = new DtoWrapper(new AttributeKey((AttributeGroupKey) attributeGroupWrapper.getKey(), attribName));
                if (attrib == null && !newAttributes.contains(attributeWrapper)) {
                    if (!Strings.isNullOrEmpty(attribName)) {
                        attrib = new AttributeDTO();
                        attrib.setId(-1);
                        attrib.setName(attribName);
                        attrib.set("attributeGroupId", group);
                        attributeWrapper.setDto(attrib);
                        newAttributes.add(attributeWrapper);
                    } else {
                        hasSkippedAttributes = true;
                        warnings.add(templates.missingAttribueValue(rowCount));
                    }
                }
            }
            rowCount++;
        }
    }


    private AttributeDTO findAttrib(AttributeGroupDTO group, String attribName) {
        if (group != null) {
            for (AttributeDTO attrib : group.getAttributes()) {
                if (attrib.getName().equals(attribName)) {
                    return attrib;
                }
            }
        }
        return null;
    }


    private ActivityFormDTO getActivity(SourceRow row) {
        String name = formName.get(row);
        String category = activityCategory.get(row);

        ActivityFormDTO activity = activityMap.get(name + category);
        if (activity == null) {
            activity = new ActivityFormDTO();
            activity.setClassicView(!formVersion.get(row).startsWith("3"));
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

    protected void findColumns() {
        hasSkippedAttributes = false;
        missingColumns.clear();
        activityCategory = findColumn("ActivityCategory", "", 255);
        formVersion = findColumn("FormVersion", "2.0");
        formName = findColumn("ActivityName", 45);
        locationType = findColumn("LocationType", defaultLocationType.getName());
        formFieldType = findColumn("FormFieldType", "quantity");
        fieldName = findColumn("Name");
        fieldCategory = findColumn("Category", "", 50);
        fieldDescription = findColumn("Description", "");
        fieldUnits = findColumn("Units", 15);
        fieldRequired = findColumn("Mandatory", "false");
        multipleAllowed = findColumn("multipleAllowed", "false");
        choiceLabel = findColumn("AttributeValue", 50);
        reportingFrequency = findColumn("ReportingFrequency", "once");
        fieldCode = findColumn("Code", "");
        fieldExpression = findColumn("Expression", "");
    }

    @Override
    public void persist(AsyncCallback<Void> callback) {
        this.callback = callback;

        List<List<? extends EntityDTO>> batches = Lists.newArrayList();

        addFixedBatchSize(batches, newActivities);
        addFixedBatchSize(batches, getNewIndicators());
        addFixedBatchSize(batches, getNewAttributeGroups());
        addFixedBatchSize(batches, getNewAttributes());

        batchCount = batches.size();
        batchNumber = 1;

        persistBatch(batches.iterator());
    }

    private static void addFixedBatchSize(List<List<? extends EntityDTO>> batches, List<? extends EntityDTO> toAdd) {
        toAdd = Lists.newArrayList(toAdd);

        int index = 0;
        int size = toAdd.size();

        if (size <= MAX_BATCH_SIZE) {
            batches.add(toAdd);
            return;
        }

        while (index < size) {
            int endIndex = (index + MAX_BATCH_SIZE) < size ? (index + MAX_BATCH_SIZE) : size;
            List<? extends EntityDTO> subList = Lists.newArrayList(toAdd.subList(index, endIndex));
            index = endIndex;
            batches.add(subList);
        }
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
