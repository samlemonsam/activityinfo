package org.activityinfo.analysis.pivot;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import org.activityinfo.model.analysis.pivot.*;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormField;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.Cardinality;
import org.activityinfo.model.type.attachment.AttachmentType;
import org.activityinfo.model.type.enumerated.EnumType;
import org.activityinfo.model.type.expr.CalculatedFieldType;
import org.activityinfo.model.type.number.QuantityType;
import org.activityinfo.model.util.Pair;

import java.util.List;
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
            if (field.getType() instanceof QuantityType) {
                return false;
            }
            if (field.getType() instanceof EnumType) {
                EnumType enumType = (EnumType) field.getType();
                return enumType.getCardinality() == Cardinality.SINGLE;
            }
            if (field.getType() instanceof AttachmentType) {
                return false;
            }
            return true;
        };
    }

    public static PivotModel build(List<FormClass> formScope) {
        return ImmutablePivotModel.builder()
                .measures(extractAllMeasures(formScope))
                .addDimensions(extractIdDimension(formScope))
                .addAllDimensions(extractAllDimensions(formScope))
                .build();
    }

    private static List<ImmutableMeasureModel> extractAllMeasures(List<FormClass> formScope) {
        return formScope.stream()
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

    private static ImmutableDimensionModel extractIdDimension(List<FormClass> formScope) {
        return ImmutableDimensionModel.builder()
                .id(ResourceId.generateCuid())
                .label("Resource Id")
                .mappings(extractIdMappings(formScope))
                .axis(Axis.ROW)
                .build();
    }

    private static List<DimensionMapping> extractIdMappings(List<FormClass> formScope) {
        return formScope.stream()
                .map(form -> new DimensionMapping(form.getId(), "_id"))
                .collect(Collectors.toList());
    }

    private static List<ImmutableDimensionModel> extractAllDimensions(List<FormClass> formScope) {
        Multimap<String,DimensionMapping> dimensionGroups = formScope.stream()
                .flatMap(LongFormatTableBuilder::extractFormDimensions)
                .collect(Multimaps.toMultimap(
                            dimLabelToDimMappingPair -> dimLabelToDimMappingPair.getFirst(),    // Dimension Label
                            dimLabelToDimMappingPair -> dimLabelToDimMappingPair.getSecond(),   // Dimension Mapping <FormId, FieldId>
                            ArrayListMultimap::create));

        return dimensionGroups.asMap().entrySet().stream()
                .map(dimensionGroup -> ImmutableDimensionModel.builder()
                        .id(ResourceId.generateCuid())
                        .label(dimensionGroup.getKey())
                        .mappings(dimensionGroup.getValue())
                        .axis(Axis.ROW)
                        .build())
                .collect(Collectors.toList());
    }

    private static Stream<Pair<String,DimensionMapping>> extractFormDimensions(FormClass form) {
        return form.getFields().stream()
                .filter(dimensionFilter())
                .map(dimensionField -> Pair.newPair(dimensionField.getLabel().toLowerCase().trim(),
                                                    new DimensionMapping(form.getId(), dimensionField.getId())));
    }

}
