package org.activityinfo.server.endpoint.rest;

import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.query.QueryModel;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldType;
import org.activityinfo.model.type.ReferenceType;
import org.activityinfo.model.type.barcode.BarcodeType;
import org.activityinfo.model.type.enumerated.EnumType;
import org.activityinfo.model.type.number.QuantityType;
import org.activityinfo.model.type.primitive.BooleanType;
import org.activityinfo.model.type.primitive.TextType;
import org.activityinfo.model.type.time.LocalDateType;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Builds a "default" query for a FormTree, including all
 * fields
 */
public class DefaultQueryBuilder {

    private static final Logger LOGGER = Logger.getLogger(DefaultQueryBuilder.class.getName());

    private FormTree tree;
    private QueryModel queryModel;

    private Set<ResourceId> visitedForms = new HashSet<>();

    public DefaultQueryBuilder(FormTree tree) {
        this.tree = tree;
        this.queryModel = new QueryModel(tree.getRootFormId());
        this.queryModel.selectResourceId().as("@id");
    }

    public QueryModel build() {
        LOGGER.info("No query fields provided, querying all.");

        addRootFields();

        return queryModel;
    }

    private void addRootFields() {

        visitedForms.add(tree.getRootFormId());

        FormClass formClass = tree.getRootFormClass();
        for (FormField formField : formClass.getFields()) {
            if(isSimple(formField)) {
                queryModel.selectField(formField.getId()).as(formatFieldAlias(formField));
            }
        }

        addReferencedForms(formClass);

    }

    private void addReferencedForms(FormClass formClass) {
        for (FormField formField : formClass.getFields()) {
            if(formField.getType() instanceof ReferenceType) {
                ReferenceType type = (ReferenceType) formField.getType();
                for (ResourceId referencedFormId : type.getRange()) {
                    addReferenceFields(referencedFormId);
                }
            }
        }
    }

    private void addReferenceFields(ResourceId referencedFormId) {
        if (!visitedForms.add(referencedFormId)) {
            // Already visited
            return;
        }

        FormClass formClass = tree.getFormClass(referencedFormId);
        for (FormField formField : formClass.getFields()) {
            if(isSimple(formField)) {
                queryModel.selectField(formField.getId()).as(formatReferencedFieldAlias(formClass, formField));
            }
        }

        addReferencedForms(formClass);
    }

    private boolean isSimple(FormField field) {
        FieldType type = field.getType();
        return type instanceof TextType ||
            type instanceof BarcodeType ||
            type instanceof QuantityType ||
            type instanceof EnumType ||
            type instanceof BooleanType ||
            type instanceof LocalDateType;
    }

    private String formatReferencedFieldAlias(FormClass formClass, FormField formField) {
        return formClass.getLabel() + "." + formatFieldAlias(formField);
    }

    private String formatFieldAlias(FormField field) {
        return field.getCode() == null ? field.getLabel() : field.getCode();
    }
}
