package org.activityinfo.server.endpoint.rest;

import org.activityinfo.model.expr.CompoundExpr;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.formTree.FieldPath;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.query.ColumnModel;
import org.activityinfo.model.query.QueryModel;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.*;
import org.activityinfo.model.type.attachment.AttachmentType;
import org.activityinfo.model.type.barcode.BarcodeType;
import org.activityinfo.model.type.enumerated.EnumType;
import org.activityinfo.model.type.expr.CalculatedFieldType;
import org.activityinfo.model.type.geo.GeoAreaType;
import org.activityinfo.model.type.geo.GeoPointType;
import org.activityinfo.model.type.number.QuantityType;
import org.activityinfo.model.type.primitive.BooleanType;
import org.activityinfo.model.type.primitive.TextType;
import org.activityinfo.model.type.subform.SubFormReferenceType;
import org.activityinfo.model.type.time.LocalDateIntervalType;
import org.activityinfo.model.type.time.LocalDateType;
import org.activityinfo.model.type.time.MonthType;
import org.activityinfo.model.type.time.YearType;

import java.util.*;
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

        addLeafFields("", new FieldPath(), tree.getRootFormClass());
        addReferencedForms(tree.getRootFormClass());
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

        FormClass referencedFormClass = tree.getFormClass(referencedFormId);
        addLeafFields(referencedFormClass.getLabel() + ".", new FieldPath(referencedFormId), referencedFormClass);
        addReferencedForms(referencedFormClass);
    }

    private void addLeafFields(String aliasPrefix, FieldPath pathPrefix, FormClass formClass) {
        for (FormField formField : formClass.getFields()) {
            queryModel.addColumns(leafColumns(aliasPrefix, pathPrefix, formField));
        }
    }

    private List<ColumnModel> leafColumns(final String aliasPrefix, final FieldPath pathPrefix, final FormField formField) {
        return formField.getType().accept(new FieldTypeVisitor<List<ColumnModel>>() {
            @Override
            public List<ColumnModel> visitAttachment(AttachmentType attachmentType) {
                return Collections.emptyList();
            }

            @Override
            public List<ColumnModel> visitCalculated(CalculatedFieldType calculatedFieldType) {
                return simpleColumnModel(aliasPrefix, pathPrefix, formField);
            }

            @Override
            public List<ColumnModel> visitReference(ReferenceType referenceType) {
                return Collections.emptyList();
            }

            @Override
            public List<ColumnModel> visitNarrative(NarrativeType narrativeType) {
                return simpleColumnModel(aliasPrefix, pathPrefix, formField);
            }

            @Override
            public List<ColumnModel> visitBoolean(BooleanType booleanType) {
                return simpleColumnModel(aliasPrefix, pathPrefix, formField);
            }

            @Override
            public List<ColumnModel> visitQuantity(QuantityType type) {
                return simpleColumnModel(aliasPrefix, pathPrefix, formField);
            }

            @Override
            public List<ColumnModel> visitGeoPoint(GeoPointType geoPointType) {
                return geoPointColumns(aliasPrefix, pathPrefix, formField);
            }

            @Override
            public List<ColumnModel> visitGeoArea(GeoAreaType geoAreaType) {
                return Collections.emptyList();
            }

            @Override
            public List<ColumnModel> visitEnum(EnumType enumType) {
                return simpleColumnModel(aliasPrefix, pathPrefix, formField);
            }

            @Override
            public List<ColumnModel> visitBarcode(BarcodeType barcodeType) {
                return simpleColumnModel(aliasPrefix, pathPrefix, formField);
            }

            @Override
            public List<ColumnModel> visitSubForm(SubFormReferenceType subFormReferenceType) {
                return Collections.emptyList();
            }

            @Override
            public List<ColumnModel> visitLocalDate(LocalDateType localDateType) {
                return simpleColumnModel(aliasPrefix, pathPrefix, formField);
            }

            @Override
            public List<ColumnModel> visitMonth(MonthType monthType) {
                return simpleColumnModel(aliasPrefix, pathPrefix, formField);

            }

            @Override
            public List<ColumnModel> visitYear(YearType yearType) {
                return simpleColumnModel(aliasPrefix, pathPrefix, formField);
            }

            @Override
            public List<ColumnModel> visitLocalDateInterval(LocalDateIntervalType localDateIntervalType) {
                return Collections.emptyList();
            }

            @Override
            public List<ColumnModel> visitText(TextType textType) {
                return simpleColumnModel(aliasPrefix, pathPrefix, formField);
            }

            @Override
            public List<ColumnModel> visitSerialNumber(SerialNumberType serialNumberType) {
                return simpleColumnModel(aliasPrefix, pathPrefix, formField);
            }
        });
    }

    private List<ColumnModel> simpleColumnModel(String aliasPrefix, FieldPath pathPrefix, FormField formField) {
        ColumnModel column = new ColumnModel();
        column.setId(aliasPrefix + formatFieldAlias(formField));
        column.setExpression(new FieldPath(pathPrefix, formField.getId()));

        return Collections.singletonList(column);
    }

    private List<ColumnModel> geoPointColumns(String aliasPrefix, FieldPath pathPrefix, FormField field) {
        String fieldAlias = aliasPrefix + formatFieldAlias(field);
        FieldPath fieldPath = new FieldPath(pathPrefix, field.getId());

        ColumnModel latitudeColumn = new ColumnModel();
        latitudeColumn.setId(fieldAlias + ".latitude");
        latitudeColumn.setExpression(new FieldPath(fieldPath, ResourceId.valueOf(GeoPointType.LATITUDE)));

        ColumnModel longitudeColumn = new ColumnModel();
        longitudeColumn.setId(fieldAlias + ".longitude");
        longitudeColumn.setExpression(new FieldPath(fieldPath, ResourceId.valueOf(GeoPointType.LONGITUDE)));

        return Arrays.asList(latitudeColumn, longitudeColumn);
    }

    private String formatFieldAlias(FormField field) {
        return field.getCode() == null ? field.getLabel() : field.getCode();
    }
}
