package org.activityinfo.server.endpoint.rest;


import org.activityinfo.legacy.shared.command.GetActivityForm;
import org.activityinfo.legacy.shared.command.GetSchema;
import org.activityinfo.legacy.shared.model.*;
import org.activityinfo.server.command.DispatcherSync;

public class SchemaCsvWriter {

    private final StringBuilder csv = new StringBuilder();
    private final DispatcherSync dispatcher;

    public SchemaCsvWriter(DispatcherSync dispatcher) {
        this.dispatcher = dispatcher;
    }

    public void write(int databaseId) {

        UserDatabaseDTO db = dispatcher.execute(new GetSchema()).getDatabaseById(databaseId);

        writeHeaders();

        for (ActivityDTO activity : db.getActivities()) {
            writeActivity(dispatcher.execute(new GetActivityForm(activity.getId())));
        }

    }

    private void writeActivity(ActivityFormDTO activity) {

        ActivityFormDTO form = dispatcher.execute(new GetActivityForm(activity.getId()));

        for (IndicatorDTO indicator : form.getIndicators()) {
            writeElementLine(activity, indicator);

        }

        for (AttributeGroupDTO group : form.getAttributeGroups()) {
            for (AttributeDTO attrib : group.getAttributes()) {
                writeElementLine(activity, group, attrib);
            }
        }
    }

    private String aggregationToString(IndicatorDTO indicator) {
        switch (indicator.getAggregation()) {
            case IndicatorDTO.AGGREGATE_SITE_COUNT:
                return "Count of Sites";
            case IndicatorDTO.AGGREGATE_AVG:
                return "Average";
            case IndicatorDTO.AGGREGATE_SUM:
                return "Sum";
        }
        return "-";
    }

    private void writeHeaders() {
        writeLine("DatabaseId",
                "DatabaseName",
                "ActivityId",
                "ActivityCategory",
                "ActivityName",
                "FormFieldType",
                "AttributeGroup/IndicatorId",
                "Category",
                "Name",
                "Description",
                "Units",
                "AttributeId",
                "AttributeValue");
    }

    private void writeElementLine(ActivityFormDTO activity, IndicatorDTO indicator) {
        writeLine(activity.getDatabaseId(),
                activity.getDatabaseName(),
                activity.getId(),
                activity.getCategory(),
                activity.getName(),
                "Indicator",
                indicator.getId(),
                indicator.getCategory(),
                indicator.getName(),
                indicator.getDescription(),
                indicator.getUnits(),
                null,
                null);
    }

    private void writeElementLine(ActivityFormDTO activity, AttributeGroupDTO attribGroup, AttributeDTO attrib) {
        writeLine(activity.getDatabaseId(),
                activity.getDatabaseName(),
                activity.getId(),
                activity.getCategory(),
                activity.getName(),
                "AttributeGroup",
                attribGroup.getId(),
                null,
                attribGroup.getName(),
                null,
                null,
                attrib.getId(),
                attrib.getName());
    }

    private void writeLine(Object... columns) {

        for (int i = 0; i != columns.length; ++i) {
            if (i > 0) {
                csv.append(",");
            }
            Object val = columns[i];
            if (val != null) {
                if (val instanceof String) {
                    String escaped = ((String) val).replace("\"", "\"\"");
                    csv.append("\"").append(escaped).append("\"");
                } else {
                    csv.append(val.toString());
                }
            }
        }
        csv.append("\n");
    }

    public String toString() {
        return csv.toString();
    }

}
