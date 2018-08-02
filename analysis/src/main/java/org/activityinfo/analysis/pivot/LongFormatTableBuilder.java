package org.activityinfo.analysis.pivot;

import com.google.common.base.Strings;
import com.google.common.collect.*;
import org.activityinfo.model.analysis.pivot.*;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.Cardinality;
import org.activityinfo.model.type.NarrativeType;
import org.activityinfo.model.type.ReferenceType;
import org.activityinfo.model.type.attachment.AttachmentType;
import org.activityinfo.model.type.enumerated.EnumType;
import org.activityinfo.model.type.expr.CalculatedFieldType;
import org.activityinfo.model.type.geo.GeoAreaType;
import org.activityinfo.model.type.geo.GeoPointType;
import org.activityinfo.model.type.number.QuantityType;
import org.activityinfo.model.type.subform.SubFormReferenceType;
import org.activityinfo.model.util.Pair;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LongFormatTableBuilder {

    private LongFormatTableBuilder() {
    }

    public static Predicate<FormField> measureFilter() {
        return field -> field.getType() instanceof QuantityType || field.getType() instanceof CalculatedFieldType;
    }

    public static Predicate<FormField> dimensionFilter() {
        return field -> {
            if (referenceFilter().test(field)) {
                return false;
            }
            if (measureFilter().test(field)) {
                return false;
            }
            if (field.getType() instanceof SubFormReferenceType) {
                return false;
            }
            if (field.getType() instanceof EnumType) {
                EnumType enumType = (EnumType) field.getType();
                return enumType.getCardinality() == Cardinality.SINGLE;
            }
            if (field.getType() instanceof AttachmentType) {
                return false;
            }
            if (field.getType() instanceof NarrativeType) {
                return false;
            }
            if (field.getType() instanceof GeoPointType || field.getType() instanceof GeoAreaType) {
                return false;
            }
            return true;
        };
    }

    public static Predicate<FormField> referenceFilter() {
        return field -> field.getType() instanceof ReferenceType;
    }

    public static PivotModel build(List<FormTree> formScope) {
        return ImmutablePivotModel.builder()
                .measures(extractAllMeasures(formScope))
                .addDimensions(extractIdDimension(formScope))
                .addDimensions(extractFormIdDimension(formScope))
                .addAllDimensions(extractAllDimensions(formScope))
                .build();
    }

    private static List<ImmutableMeasureModel> extractAllMeasures(List<FormTree> formScope) {
        return formScope.stream()
                .map(FormTree::getRootFormClass)
                .flatMap(LongFormatTableBuilder::extractFormMeasures)
                .collect(Collectors.toList());
    }

    private static Stream<ImmutableMeasureModel> extractFormMeasures(FormClass form) {
        return form.getFields().stream()
                .filter(measureFilter())
                .map(measureField -> ImmutableMeasureModel.builder()
                        .formId(form.getId())
                        .label(measureField.getLabel())
                        .formula(measureField.getId().asString())
                        .build());
    }

    private static ImmutableDimensionModel extractIdDimension(List<FormTree> formScope) {
        return ImmutableDimensionModel.builder()
                .id(ResourceId.generateCuid())
                .label("RecordId")
                .mappings(extractIdMappings(formScope))
                .axis(Axis.ROW)
                .build();
    }

    private static List<DimensionMapping> extractIdMappings(List<FormTree> formScope) {
        return formScope.stream()
                .map(FormTree::getRootFormClass)
                .map(form -> new DimensionMapping(form.getId(), "_id"))
                .collect(Collectors.toList());
    }

    private static ImmutableDimensionModel extractFormIdDimension(List<FormTree> formScope) {
        return ImmutableDimensionModel.builder()
                .id(ResourceId.generateCuid())
                .label("FormId")
                .mappings(extractFormIdMappings(formScope))
                .axis(Axis.ROW)
                .build();
    }

    private static List<DimensionMapping> extractFormIdMappings(List<FormTree> formScope) {
        return formScope.stream()
                .map(FormTree::getRootFormClass)
                .map(form -> new DimensionMapping(form.getId(), "_class"))
                .collect(Collectors.toList());
    }

    private static List<ImmutableDimensionModel> extractAllDimensions(List<FormTree> formScope) {
        Multimap<String,DimensionMapping> dimensionGroups = formScope.stream()
                .map(FormTree::getRootFields)
                .flatMap(List::stream)
                .flatMap(LongFormatTableBuilder::mapDimensions)
                .collect(Multimaps.toMultimap(Pair::getFirst, Pair::getSecond, ArrayListMultimap::create));
        return buildModels(dimensionGroups);
    }

    private static Stream<Pair<String,DimensionMapping>> mapDimensions(FormTree.Node node) {
        if (node.isReference() && !node.isSubForm()) {
            return node.getChildren().stream().flatMap(LongFormatTableBuilder::mapDimensions);
        }
        if (dimensionFilter().test(node.getField())) {
            return Stream.of(mapDimension(node));
        }
        if (node.getField().getType() instanceof GeoPointType) {
            return mapGeoDimension(node);
        }
        return Stream.empty();
    }

    private static Pair<String,DimensionMapping> mapDimension(FormTree.Node node) {
        return Pair.newPair(label(node), map(node));
    }

    private static Stream<Pair<String,DimensionMapping>> mapGeoDimension(FormTree.Node node) {
        return Stream.of(Pair.newPair(label(node, GeoPointType.LATITUDE), geoMap(node, GeoPointType.LATITUDE)),
                Pair.newPair(label(node, GeoPointType.LONGITUDE), geoMap(node, GeoPointType.LONGITUDE)));
    }

    private static String label(FormTree.Node node, String suffix) {
        return suffix == null
                ? label(node)
                : label(node) + "." + suffix;
    }

    private static String label(FormTree.Node node) {
        if (node.isRoot()) {
            return node.fieldLabel();
        } else if (node.getParent().isRoot() && node.getParent().getDefiningFormClass().isSubForm()) {
            return node.fieldLabel();
        } else if (node.getParent().isRoot() && node.getParent().getDefiningFormClass().getId().getDomain() == CuidAdapter.MONTHLY_REPORT_FORM_CLASS) {
            return node.fieldLabel();
        } else {
            return node.formFieldLabel();
        }
    }

    private static DimensionMapping map(FormTree.Node node) {
        return new DimensionMapping(node.getRootFormClass().getId(), node.getPath().toString());
    }

    private static DimensionMapping geoMap(FormTree.Node node, String pointAxis) {
        return new DimensionMapping(node.getRootFormClass().getId(), node.getPath().toString() + "." + pointAxis);
    }

    private static List<ImmutableDimensionModel> buildModels(Multimap<String, DimensionMapping> dimensionGroups) {
        return dimensionGroups.asMap().entrySet().stream()
                .map(dimensionGroup -> ImmutableDimensionModel.builder()
                        .id(ResourceId.generateCuid())
                        .label(dimensionGroup.getKey())
                        .mappings(dimensionGroup.getValue())
                        .axis(Axis.ROW)
                        .build())
                .collect(Collectors.toList());
    }

}
