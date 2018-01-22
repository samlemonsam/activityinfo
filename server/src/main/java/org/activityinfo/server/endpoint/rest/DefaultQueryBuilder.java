package org.activityinfo.server.endpoint.rest;

import org.activityinfo.model.form.FormField;
import org.activityinfo.model.formTree.FieldPath;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.query.ColumnModel;
import org.activityinfo.model.query.QueryModel;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldTypeVisitor;
import org.activityinfo.model.type.NarrativeType;
import org.activityinfo.model.type.ReferenceType;
import org.activityinfo.model.type.SerialNumberType;
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
import org.activityinfo.model.type.time.*;

import java.util.*;
import java.util.logging.Logger;

/**
 * Builds a "default" query for a FormTree, including all
 * fields in the form, AND those that are referenced.
 *
 */
public class DefaultQueryBuilder {

    private static final Logger LOGGER = Logger.getLogger(DefaultQueryBuilder.class.getName());

    private FormTree tree;
    private QueryModel queryModel;

    private Map<String, ColumnModel> columns = new HashMap<>();

    public DefaultQueryBuilder(FormTree tree) {
        this.tree = tree;

    }

    public QueryModel build() {
        LOGGER.info("No query fields provided, querying all.");

        addFields("", new FieldPath(), tree.getRootFields());

        this.queryModel = new QueryModel(tree.getRootFormId());
        this.queryModel.selectResourceId().as("@id");

        for (ColumnModel columnModel : columns.values()) {
            this.queryModel.addColumn(columnModel);
        }

        return queryModel;
    }

    private void addFields(String aliasPrefix, FieldPath pathPrefix, List<FormTree.Node> fields) {

        for (FormTree.Node node : fields) {
            if(node.isReference()) {
                addReferencedFields(aliasPrefix, pathPrefix, node);
            } else {
                addColumns(leafColumns(aliasPrefix, pathPrefix, node.getField()));
            }
        }
    }

    private void addReferencedFields(String parentAliasPrefix, FieldPath parentPathPrefix, FormTree.Node node) {

        // If this field has a 'code', then use this code both as the alias,
        // and as the query expression.

        // This has the effect of merging fields with the same code into a single column.

        String aliasPrefix = parentAliasPrefix + formatFieldAlias(node.getField()) + ".";
        FieldPath pathPrefix = new FieldPath(parentPathPrefix, fieldFormula(node.getField()));

        addFields(aliasPrefix, pathPrefix, node.getChildren());
    }

    private void addColumns(List<ColumnModel> columnModels) {
        for (ColumnModel columnModel : columnModels) {
            if(!columns.containsKey(columnModel.getId())) {
                columns.put(columnModel.getId(), columnModel);
            }
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
            public List<ColumnModel> visitWeek(EpiWeekType epiWeekType) {
                return simpleColumnModel(aliasPrefix, pathPrefix, formField);
            }

            @Override
            public List<ColumnModel> visitFortnight(FortnightType fortnightType) {
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
        column.setExpression(new FieldPath(pathPrefix, fieldFormula(formField)));

        return Collections.singletonList(column);
    }

    private List<ColumnModel> geoPointColumns(String aliasPrefix, FieldPath pathPrefix, FormField field) {
        String fieldAlias = aliasPrefix + formatFieldAlias(field);
        FieldPath fieldPath = new FieldPath(pathPrefix, fieldFormula(field));

        ColumnModel latitudeColumn = new ColumnModel();
        latitudeColumn.setId(fieldAlias + ".latitude");
        latitudeColumn.setExpression(new FieldPath(fieldPath, ResourceId.valueOf(GeoPointType.LATITUDE)));

        ColumnModel longitudeColumn = new ColumnModel();
        longitudeColumn.setId(fieldAlias + ".longitude");
        longitudeColumn.setExpression(new FieldPath(fieldPath, ResourceId.valueOf(GeoPointType.LONGITUDE)));

        return Arrays.asList(latitudeColumn, longitudeColumn);
    }

    private ResourceId fieldFormula(FormField field) {
        if(field.hasCode() && !field.getCode().isEmpty()) {
            return ResourceId.valueOf(field.getCode());
        } else {
            return field.getId();
        }
    }

    private String formatFieldAlias(FormField field) {
        if (field.hasCode() && !field.getCode().isEmpty()) {
            return field.getCode();
        } else {
            return field.getLabel();
        }
    }
}
