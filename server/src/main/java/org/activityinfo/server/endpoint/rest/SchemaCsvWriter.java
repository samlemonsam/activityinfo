package org.activityinfo.server.endpoint.rest;


import org.activityinfo.legacy.shared.command.GetActivityForm;
import org.activityinfo.legacy.shared.command.GetSchema;
import org.activityinfo.legacy.shared.model.*;
import org.activityinfo.server.command.DispatcherSync;

public class SchemaCsvWriter {

    private final DispatcherSync dispatcher;
    private CsvWriter csv = new CsvWriter();

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

    private void writeHeaders() {
        csv.writeLine("DatabaseId",
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
                "AttributeValue",
                "Code",
                "Expression");
    }

    private void writeElementLine(ActivityFormDTO activity, IndicatorDTO indicator) {
        csv.writeLine(activity.getDatabaseId(),
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
                null,
                indicator.getNameInExpression(),
                indicator.getExpression()
        );
    }

    private void writeElementLine(ActivityFormDTO activity, AttributeGroupDTO attribGroup, AttributeDTO attrib) {
        csv.writeLine(activity.getDatabaseId(),
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
                attrib.getName(),
                null,
                null);
    }

    public String toString() {
        return csv.toString();
    }

}
