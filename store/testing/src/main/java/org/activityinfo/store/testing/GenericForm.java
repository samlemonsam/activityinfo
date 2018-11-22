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
package org.activityinfo.store.testing;

import com.google.common.base.Supplier;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.form.TypedFormRecord;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.ReferenceType;
import org.activityinfo.model.type.primitive.TextType;
import org.activityinfo.model.type.subform.SubFormReferenceType;
import org.activityinfo.model.type.time.LocalDateType;

import java.util.List;

/**
 * Standard form which is auto-generated for user
 */
public class GenericForm implements TestForm {

    public static final ResourceId FORM_ID = ResourceId.valueOf("GENERIC_FORM");

    private final ResourceId START_DATE_FIELD_ID = ResourceId.valueOf("START_DATE");
    private final ResourceId END_DATE_FIELD_ID = ResourceId.valueOf("END_DATE");
    private final ResourceId PARTNER_FIELD_ID = ResourceId.valueOf("PARTNER");
    private final ResourceId PROJECT_FIELD_ID = ResourceId.valueOf("PROJECT");
    private final ResourceId COMMENTS_FIELD_ID = ResourceId.valueOf("COMMENTS");
    public static final ResourceId BLANK_SUB_FORM_FIELD_ID = ResourceId.valueOf("BLANK_SUB_FORM");

    private final int ROW_COUNT = 2048;

    private final FormClass formClass;
    private final FormField startDateField;
    private final FormField endDateField;
    private final FormField partnerField;
    private final FormField projectField;
    private final FormField commentsField;
    private FormField blankSubFormField;

    private List<TypedFormRecord> records;
    private RecordGenerator generator;

    public GenericForm() {
        this(new UnitTestingIds());
    }

    public GenericForm(Ids ids) {
        formClass = new FormClass(ids.formId("GENERIC_FORM"))
                .setLabel("Generic Form");

        startDateField = formClass.addField(ids.fieldId("START_DATE"))
                .setCode("date1")
                .setLabel("Start Date")
                .setVisible(true)
                .setRequired(true)
                .setType(LocalDateType.INSTANCE);

        endDateField = formClass.addField(ids.fieldId("END_DATE"))
                .setCode("date2")
                .setLabel("End Date")
                .setVisible(true)
                .setRequired(true)
                .setType(LocalDateType.INSTANCE);

        partnerField = formClass.addField(ids.fieldId("PARTNER"))
                .setCode("partner")
                .setLabel("Partner")
                .setVisible(true)
                .setRequired(true)
                .setType(new ReferenceType());

        projectField = formClass.addField(ids.fieldId("PROJECT"))
                .setCode("project")
                .setLabel("Project")
                .setVisible(false)
                .setRequired(false)
                .setType(new ReferenceType());

        commentsField = formClass.addField(ids.fieldId("COMMENTS"))
                .setCode("comments")
                .setLabel("Comments")
                .setVisible(true)
                .setRequired(false)
                .setType(TextType.SIMPLE);

        blankSubFormField = formClass.addField(ids.fieldId("BLANK_SUB_FORM"))
                .setLabel("Blank Sub-Form")
                .setVisible(true)
                .setRequired(false)
                .setType(new SubFormReferenceType(BlankSubForm.FORM_ID));

        generator = new RecordGenerator(ids, formClass)
                .distribution(startDateField.getId(), new DateGenerator(startDateField, 2016, 2017))
                .distribution(endDateField.getId(), new DateGenerator(endDateField, 2016, 2017))
                .distribution(partnerField.getId(), new RefGenerator(this)) // self-reference
                .distribution(projectField.getId(), new RefGenerator(this)) // self-reference
                .distribution(commentsField.getId(), new DiscreteTextGenerator(0.8,"comm"));
    }

    @Override
    public ResourceId getFormId() {
        return formClass.getId();
    }

    @Override
    public FormClass getFormClass() {
        return formClass;
    }

    @Override
    public List<TypedFormRecord> getRecords() {
        if (records == null) {
            this.records = generator.get(ROW_COUNT);
        }
        return records;
    }

    @Override
    public Supplier<TypedFormRecord> getGenerator() {
        return generator;
    }
}
