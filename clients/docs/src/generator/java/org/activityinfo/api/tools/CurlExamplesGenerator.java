/*
 * ActivityInfo
 * Copyright (C) 2009-2013 UNICEF
 * Copyright (C) 2014-2018 BeDataDriven Groep B.V.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.activityinfo.api.tools;

import org.activityinfo.json.JsonValue;
import org.activityinfo.json.impl.JsonUtil;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormRecord;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.Cardinality;
import org.activityinfo.model.type.ReferenceType;
import org.activityinfo.model.type.enumerated.EnumItem;
import org.activityinfo.model.type.enumerated.EnumType;
import org.activityinfo.model.type.enumerated.EnumValue;
import org.activityinfo.model.type.geo.GeoPoint;
import org.activityinfo.model.type.number.Quantity;
import org.activityinfo.model.type.number.QuantityType;
import org.activityinfo.model.type.primitive.TextValue;
import org.activityinfo.model.type.time.LocalDate;
import org.activityinfo.model.type.time.LocalDateType;

/**
 * Generates example results for CURL and JSON.
 *
 */
@SuppressWarnings("unused")
public class CurlExamplesGenerator {

    private static class Example {
        String commandLine;
        JsonValue result;

        public Example(String commandLine, JsonValue result) {
            this.commandLine = commandLine;
            this.result = result;
        }

        public String getCommandLine() {
            return commandLine;
        }

        public JsonValue getResult() {
            return result;
        }

        public String format() {
            StringBuilder sb = new StringBuilder();
            sb.append("$ ");
            sb.append(commandLine);
            sb.append("\n");

            return JsonUtil.stringify(result, 2);
        }
    }

    public static Example getFormSchema() {

        FormClass exampleForm = new FormClass(ResourceId.generateId())
                .setDatabaseId(54)
                .setLabel("NFI Distribution")
                .setDescription("Form for collecting results of NFI distributions in North Kivu");

        exampleForm.addField(ResourceId.generateId())
                .setLabel("Date of Distribution")
                .setType(LocalDateType.INSTANCE)
                .setRequired(true);

        exampleForm.addField(CuidAdapter.partnerField(33))
                .setLabel("Partner")
                .setType(ReferenceType.single(CuidAdapter.partnerFormId(54)))
                .setRequired(true)
                .setDescription("The implementing partner who conducted the distribution");

        exampleForm.addField(ResourceId.generateId())
                .setLabel("Donor")
                .setType(new EnumType(Cardinality.SINGLE,
                        new EnumItem(ResourceId.generateId(), "USAID"),
                        new EnumItem(ResourceId.generateId(), "DFID"),
                        new EnumItem(ResourceId.generateId(), "ECHO")))
                .setRequired(true);

        exampleForm.addField(ResourceId.generateId())
                .setLabel("Number of households receiving a kit")
                .setType(new QuantityType("households"))
                .setRequired(true);


        return new Example(
                "curl https://www.activityinfo.org/form/" + exampleForm.getId() + "/schema",
                exampleForm
                    .toJson()
        );

    }

    public static Example getRecord() {

        ResourceId formId = ResourceId.generateId();
        ResourceId recordId = ResourceId.generateId();

        FormRecord.Builder record = new FormRecord.Builder();
        record.setFormId(formId);
        record.setRecordId(recordId);
        record.setFieldValue(ResourceId.generateId(), TextValue.valueOf("Text Value"));
        record.setFieldValue(ResourceId.generateId(), new Quantity(1500));
        record.setFieldValue(ResourceId.generateId(), new LocalDate(2016, 10, 5));
        record.setFieldValue(ResourceId.generateId(), new GeoPoint(52.078663, 4.288788));
        record.setFieldValue(ResourceId.generateId(), new EnumValue(ResourceId.generateId()));

        return new Example(
                String.format("curl https://www.activityinfo.org/resources/form/%s/record/%s",
                        formId, recordId),
                record.build().toJson()
        );
    }

    public static ExampleModel getExample(String operationName) {
        try {
            Example example = (Example) CurlExamplesGenerator.class.getMethod(operationName).invoke(null);
            return new ExampleModel("shell", example.format());
        } catch (NoSuchMethodException e) {
            return null;
        } catch (Exception e) {
            throw new RuntimeException("Could not invoke curl example generator for " + operationName, e);
        }
    }

}
