package org.activityinfo.server.endpoint.rest;


import org.activityinfo.legacy.shared.command.GetActivityForm;
import org.activityinfo.legacy.shared.command.GetSchema;
import org.activityinfo.legacy.shared.model.*;
import org.activityinfo.server.command.DispatcherSync;

import java.io.IOException;

public class SchemaCsvWriter {

    private final DispatcherSync dispatcher;
    private CsvWriter csv = new CsvWriter();

    public SchemaCsvWriter(DispatcherSync dispatcher) throws IOException {
        this.dispatcher = dispatcher;
    }

    public void write(int databaseId) throws IOException {

        UserDatabaseDTO db = dispatcher.execute(new GetSchema()).getDatabaseById(databaseId);

        writeHeaders();

        for (ActivityDTO activity : db.getActivities()) {
            writeActivity(dispatcher.execute(new GetActivityForm(activity.getId())));
        }

    }

    private void writeActivity(ActivityFormDTO activity) throws IOException {

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

    private void writeHeaders() throws IOException {
        csv.writeLine("DatabaseId",
                "DatabaseName",
                "FormVersion",
                "ActivityId",
                "ActivityCategory",
                "ActivityName",
                "FormFieldType",
                "AttributeGroup/IndicatorId",
                "Category",
                "Name",
                "Mandatory",
                "Description",
                "Units",
                "MultipleAllowed",
                "AttributeId",
                "AttributeValue",
                "Code",
                "Expression");
    }

    private void writeElementLine(ActivityFormDTO activity, IndicatorDTO indicator) throws IOException {
        csv.writeLine(activity.getDatabaseId(),
                activity.getDatabaseName(),
                activity.getClassicView() ? "2.0" : "3.0",
                activity.getId(),
                activity.getCategory(),
                activity.getName(),
                "Indicator",
                indicator.getId(),
                indicator.getCategory(),
                indicator.getName(),
                toString(indicator.isMandatory()),
                indicator.getDescription(),
                indicator.getUnits(),
                null,
                null,
                null,
                indicator.getNameInExpression(),
                indicator.getExpression()
        );
    }

    private void writeElementLine(ActivityFormDTO activity, AttributeGroupDTO attribGroup, AttributeDTO attrib) throws IOException {
        csv.writeLine(activity.getDatabaseId(),
                activity.getDatabaseName(),
                activity.getClassicView() ? "2.0" : "3.0",
                activity.getId(),
                activity.getCategory(),
                activity.getName(),
                "AttributeGroup",
                attribGroup.getId(),
                null,
                attribGroup.getName(),
                toString(attribGroup.isMandatory()),
                null,
                null,
                toString(attribGroup.isMultipleAllowed()),
                attrib.getId(),
                attrib.getName(),
                null,
                null);
    }

    private String toString(boolean value) {
        if(value) {
            return "Yes";
        } else {
            return "No";
        }
    }

    public String toString() {
        return csv.toString();
    }

}
