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
package org.activityinfo.ui.client.page.config.design.importer;

import com.google.common.base.Strings;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.legacy.shared.SchemaCsv;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.form.SubFormKind;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.legacy.KeyGenerator;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.*;
import org.activityinfo.model.type.enumerated.EnumItem;
import org.activityinfo.model.type.enumerated.EnumType;
import org.activityinfo.model.type.expr.CalculatedFieldType;
import org.activityinfo.model.type.number.QuantityType;
import org.activityinfo.model.type.subform.SubFormReferenceType;
import org.activityinfo.promise.Promise;
import org.activityinfo.promise.PromiseExecutionOperation;
import org.activityinfo.promise.PromisesExecutionGuard;
import org.activityinfo.ui.client.component.importDialog.model.source.SourceColumn;
import org.activityinfo.ui.client.component.importDialog.model.source.SourceRow;
import org.activityinfo.ui.client.component.importDialog.model.source.SourceTable;
import org.activityinfo.ui.client.dispatch.ResourceLocator;

import javax.annotation.Nullable;
import java.util.*;

/**
 * Imports a V3 schema CSV exported by {@link SchemaCsvWriterV3}
 */
public class SchemaImporterV3 extends SchemaImporter {


    private class EnumBuilder {
        private FormField formField;
        private Cardinality cardinality;
        private List<EnumItem> items = new ArrayList<>();

        public EnumBuilder(FormField field, Cardinality cardinality) {
            formField = field;
            this.cardinality = cardinality;
        }
    }

    private final KeyGenerator keyGenerator = new KeyGenerator();

    // columns
    private Column formName;
    private Column formFieldType;
    private Column fieldName;
    private Column fieldDescription;
    private Column fieldUnits;
    private Column fieldRequired;
    private Column choiceLabel;
    private Column fieldCode;
    private Column fieldExpression;
    private Column subForm;
    private Column subFormType;
    private Column references;

    private Map<String, FormClass> formMap = new HashMap<>();
    private Map<String, FormClass> subFormMap = new HashMap<>();
    private Map<String, EnumBuilder> enumMap = new HashMap<>();
    private Map<FormClass, FormField> refMap = new HashMap<>();
    private int databaseId;
    private ResourceLocator locator;

    private AsyncCallback<Void> validationCallback;

    public SchemaImporterV3(int databaseId, ResourceLocator locator, WarningTemplates templates) {
        super(templates);
        this.databaseId = databaseId;
        this.locator = locator;
    }

    public SchemaImporterV3(int databaseId, ResourceLocator locator) {
        this(databaseId, locator, GWT.<WarningTemplates>create(WarningTemplates.class));
    }

    public boolean accept(SourceTable table) {
        for (SourceColumn sourceColumn : table.getColumns()) {
            if(sourceColumn.getHeader().equalsIgnoreCase("FormName")) {
                return true;
            }
        }
        return false;
    }

    protected void findColumns() {
        formName = findColumn(SchemaCsv.FORM_NAME_COLUMN);
        formFieldType = findColumn(SchemaCsv.FIELD_TYPE_COLUMN);
        fieldName = findColumn(SchemaCsv.FIELD_NAME_COLUMN);
        fieldCode = findColumn(SchemaCsv.FIELD_CODE_COLUMN, "");
        fieldDescription = findColumn(SchemaCsv.FIELD_DESCRIPTION, "");
        fieldUnits = findColumn(SchemaCsv.UNITS_COLUMN, "units");
        fieldRequired = findColumn(SchemaCsv.REQUIRED_COLUMN, "false");
        fieldExpression = findColumn(SchemaCsv.EXPRESSION, "");
        choiceLabel = findColumn(SchemaCsv.CHOICE_LABEL, "");
        subForm = findColumn(SchemaCsv.SUB_FORM_COLUMN, "");
        subFormType = findColumn(SchemaCsv.SUB_FORM_TYPE_COLUMN, SubFormKind.REPEATING.name());
        references = findColumn(SchemaCsv.REFERENCES, "");
    }

    public boolean processRows(final AsyncCallback<Void> validationCallback) {
        this.validationCallback = validationCallback;
        return processRows();
    }

    @Override
    public boolean processRows() {
        formMap.clear();
        subFormMap.clear();
        enumMap.clear();
        refMap.clear();

        fatalError = false;
        for (SourceRow row : source.getRows()) {
            try {
                FormClass parentFormClass = getFormClass(row);
                FormClass formClass = getSubFormClass(parentFormClass, row);

                String type = formFieldType.get(row);
                if (isEnum(type)) {
                    addChoice(formClass, row);
                } else {
                    FieldType fieldType = parseFieldType(row);
                    FormField newField = addField(formClass, fieldType.getTypeClass(), row);
                    newField.setType(fieldType);
                    if (newField.getType() instanceof ReferenceType) {
                        refMap.put(formClass, newField);
                    }
                }
            } catch (UnableToParseRowException e) {
                warnings.add(SafeHtmlUtils.fromString(e.getMessage()));
                fatalError = true;
            }
        }

        for (EnumBuilder enumBuilder : enumMap.values()) {
            enumBuilder.formField.setType(new EnumType(enumBuilder.cardinality, enumBuilder.items));
        }

        if (validationCallback != null) {
            validateReferences();
        }

        return !fatalError;
    }

    private void validateReferences() {
        List<ResourceId> references = determineReferencesToValidate();
        promiseToValidate(references).then(validationCallback);
    }

    private List<ResourceId> determineReferencesToValidate() {
        List<ResourceId> validationList = new ArrayList<>(refMap.size());
        for (FormField refField : refMap.values()) {
            ReferenceType refType = (ReferenceType) refField.getType();
            ResourceId reference = refType.getRange().iterator().next();
            if (!isImportedReference(reference)) {
                validationList.add(reference);
            }
        }
        return validationList;
    }

    private boolean isImportedReference(ResourceId reference) {
        for (FormClass form : formMap.values()) {
            if (form.getId().equals(reference)) {
                return true;
            }
            try {
                if (form.getField(reference) != null) {
                    return true;
                }
            } catch (IllegalArgumentException e) {
                continue;
            }
        }
        return false;
    }

    private Promise<Void> promiseToValidate(List<ResourceId> references) {
        List<Promise<FormClass>> promises = new ArrayList<>(references.size());
        for (final ResourceId reference : references) {
            promises.add(locator.getFormClass(reference));
        }
        return Promise.waitAll(promises);
    }

    public List<FormClass> toSave() {
        List<FormClass> formClasses = new ArrayList<>();
        formClasses.addAll(formMap.values());
        formClasses.addAll(subFormMap.values());
        return formClasses;
    }

    private void addChoice(FormClass formClass, SourceRow row) {

        String fieldLabel = fieldName.get(row);
        String fieldKey = formClass.getId() + fieldLabel;

        EnumBuilder enumField = enumMap.get(fieldKey);
        if(enumField == null) {
            FormField newField = addField(formClass, EnumType.TYPE_CLASS, row);
            enumField = new EnumBuilder(newField, parseCardinality(row));
            enumMap.put(fieldKey, enumField);
        }

        enumField.items.add(new EnumItem(EnumItem.generateId(), choiceLabel.get(row)));
    }

    private FormField addField(FormClass formClass, FieldTypeClass typeClass, SourceRow row) {
        FormField field = new FormField(ResourceId.generateFieldId(typeClass));
        field.setLabel(fieldName.getOrThrow(row));
        field.setCode(fieldCode.get(row));
        field.setDescription(fieldDescription.get(row));
        field.setRequired(isTruthy(fieldRequired.get(row)));
        field.setVisible(true);

        formClass.addElement(field);
        return field;
    }

    private Cardinality parseCardinality(SourceRow row) {
        String type = formFieldType.get(row);
        if(type.equalsIgnoreCase(SchemaCsv.MULTIPLE_SELECT)) {
            return Cardinality.MULTIPLE;
        } else {
            return Cardinality.SINGLE;
        }
    }

    private boolean isEnum(String type) {
        return SchemaCsv.SINGLE_SELECT.equalsIgnoreCase(type) ||
                SchemaCsv.MULTIPLE_SELECT.equalsIgnoreCase(type);
    }

    private FieldType parseFieldType(SourceRow row) {
        FieldTypeClass fieldTypeClass = parseFieldTypeClass(row);
        if(fieldTypeClass == QuantityType.TYPE_CLASS) {
            return new QuantityType(fieldUnits.get(row));
        } else if(fieldTypeClass == ReferenceType.TYPE_CLASS) {
            return new ReferenceType()
                    .setCardinality(Cardinality.SINGLE)
                    .setRange(parseRange(references.get(row)));

        } else if(fieldTypeClass == CalculatedFieldType.TYPE_CLASS) {
            return new CalculatedFieldType(fieldExpression.get(row));

        } else {
            return fieldTypeClass.createType();
        }
    }

    private Set<ResourceId> parseRange(String reference) {
        if(Strings.isNullOrEmpty(reference)) {
            throw new UnableToParseRowException(
                    I18N.MESSAGES.referenceFieldRequiresRange("REFERENCE",
                            SchemaCsv.REFERENCES));
        }
        return Collections.singleton(ResourceId.valueOf(reference));
    }

    private FieldTypeClass parseFieldTypeClass(SourceRow row) {
        String type = formFieldType.get(row);
        for (FieldTypeClass fieldTypeClass : TypeRegistry.get().getTypeClasses()) {
            if(fieldTypeClass.getId().equalsIgnoreCase(type)) {
                return fieldTypeClass;
            }
        }
        return QuantityType.TYPE_CLASS;
    }

    private FormClass getFormClass(SourceRow row) {
        String name = formName.get(row);

        FormClass formClass = formMap.get(name);
        if (formClass == null) {
            formClass = new FormClass(CuidAdapter.activityFormClass(keyGenerator.generateInt()));
            formClass.setDatabaseId(databaseId);
            formClass.setLabel(name);

            formMap.put(name, formClass);
        }
        return formClass;
    }

    private FormClass getSubFormClass(FormClass masterForm, SourceRow row) {
        String subFormName = subForm.get(row);
        if(subFormName == null) {
            return masterForm;
        }

        FormClass subFormClass = subFormMap.get(subFormName);
        if(subFormClass == null) {
            subFormClass = new FormClass(ResourceId.generateId());
            subFormClass.setSubFormKind(parseSubFormKind(subFormType.get(row)));
            subFormClass.setParentFormId(masterForm.getId());
            subFormClass.setLabel(subFormName);
            subFormClass.setDatabaseId(databaseId);
            subFormMap.put(subFormName, subFormClass);

            FormField subFormField = new FormField(ResourceId.generateFieldId(SubFormReferenceType.TYPE_CLASS));
            subFormField.setLabel(subFormName);
            subFormField.setType(new SubFormReferenceType(subFormClass.getId()));
            subFormField.setVisible(true);
            masterForm.addElement(subFormField);
        }

        return subFormClass;
    }

    private SubFormKind parseSubFormKind(String kind) {
        for (SubFormKind subFormKind : SubFormKind.values()) {
            if(subFormKind.name().equalsIgnoreCase(kind)) {
                return subFormKind;
            }
        }
        return SubFormKind.REPEATING;
    }

    @Override
    public void persist(final AsyncCallback<Void> callback) {

        List<PromiseExecutionOperation> operations = new ArrayList<>();
        for (final FormClass formClass : toSave()) {
            operations.add(new PromiseExecutionOperation() {
                @Nullable
                @Override
                public Promise<Void> apply(@Nullable Void aVoid) {
                    return locator.persist(formClass);
                }
            });
        }

        PromisesExecutionGuard.newInstance().executeSerially(operations).then(callback);
    }
}
