package org.activityinfo.ui.client.input.viewModel;

import com.google.common.base.Predicate;
import org.activityinfo.model.expr.ExprNode;
import org.activityinfo.model.expr.ExprParser;
import org.activityinfo.model.form.FormEvalContext;
import org.activityinfo.model.form.FormInstance;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.subform.SubFormReferenceType;
import org.activityinfo.ui.client.input.model.FieldInput;
import org.activityinfo.ui.client.input.model.FormInputModel;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Helper class which constructs a {@link FormInputViewModel}.
 *
 * <p>This class is built from a form's tree and includes all parsed formulas needed to calculate
 * field values and relevancy, so they don't need to be re-parsed every time the {@link FormInputModel} changes.</p>
 */
public class FormInputViewModelBuilder {

    private final Logger LOGGER = Logger.getLogger(FormInputViewModelBuilder.class.getName());

    private final FormTree formTree;
    private final FormEvalContext evalContext;

    private Map<ResourceId, Predicate<FormInstance>> relevanceCalculators = new HashMap<>();

    private List<SubFormInputViewModelBuilder> subBuilders = new ArrayList<>();


    public FormInputViewModelBuilder(FormTree formTree) {
        this.formTree = formTree;
        this.evalContext = new FormEvalContext(formTree.getRootFormClass());

        for (FormTree.Node node : formTree.getRootFields()) {
            if(node.isSubForm()) {
                subBuilders.add(buildSubBuilder(node));
            }
            if(node.getField().hasRelevanceCondition()) {
                buildRelevanceCalculator(node);
            }
        }
    }

    private SubFormInputViewModelBuilder buildSubBuilder(FormTree.Node node) {
        SubFormReferenceType subFormType = (SubFormReferenceType) node.getType();
        FormTree subTree = formTree.subTree(subFormType.getClassId());
        return new SubFormInputViewModelBuilder(node, subTree);
    }

    private void buildRelevanceCalculator(FormTree.Node node) {

        String formula = node.getField().getRelevanceConditionExpression();

        ExprNode rootNode;
        try {
            rootNode = ExprParser.parse(formula);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Invalid relevance formula: " + formula, e);
            return;
        }

        relevanceCalculators.put(node.getFieldId(), instance -> {
            evalContext.setInstance(instance);
            try {
                return rootNode.evaluateAsBoolean(evalContext);
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Failed to evaluate relevance condition", e);
                return true;
            }
        });
    }

    public FormInputViewModel build(FormInputModel inputModel) {

        FormInstance record = new FormInstance(ResourceId.generateId(), formTree.getRootFormId());

        // Keep track if this form is valid and ready to submit
        boolean valid = true;

        // First build up the values as input
        for (FormTree.Node node : formTree.getRootFields()) {
            if(node.getType().isUpdatable()) {
                FieldInput fieldInput = inputModel.get(node.getFieldId());
                switch (fieldInput.getState()) {
                    case VALID:
                        record.set(node.getFieldId(), fieldInput.getValue());
                        break;
                    case INVALID:
                        valid = false;
                        break;
                }
            }
        }

        // Determine which fields are "relevant" and can be enabled
        Set<ResourceId> relevantSet = computeRelevance(record);

        // Finally, check to ensure that all required -AND- relevant
        // values are provided
        Set<ResourceId> missing = computeMissing(record, relevantSet);

        if(!missing.isEmpty()) {
            valid = false;
        }

        // Build subform view models
        Map<ResourceId, SubFormInputViewModel> subFormMap = new HashMap<>();
        for (SubFormInputViewModelBuilder subBuilder : subBuilders) {
            subFormMap.put(subBuilder.getFieldId(), subBuilder.build(inputModel));
        }

        return new FormInputViewModel(formTree, inputModel,
                record.getFieldValueMap(),
                subFormMap,
                relevantSet,
                missing, valid);
    }

    private Set<ResourceId> computeRelevance(FormInstance record) {
        // All fields are relevant by default
        Set<ResourceId> relevantSet = new HashSet<>();
        for (FormTree.Node node : formTree.getRootFields()) {
            relevantSet.add(node.getFieldId());
        }

        // Now keep updating the set until it converges
        boolean changing;
        do {
            changing = false;

            for (Map.Entry<ResourceId, Predicate<FormInstance>> field : relevanceCalculators.entrySet()) {

                boolean relevant = field.getValue().apply(record);
                if(!relevant) {
                    record.set(field.getKey(), (FieldValue)null);
                }

                boolean wasRelevant = toggle(relevantSet, field.getKey(), relevant);
                if(relevant != wasRelevant) {
                    changing = true;

                }
            }
        } while(changing);
        return relevantSet;
    }

    /**
     * Adds or removes a field from the relevancy set.
     * @param relevantSet the relevancy set
     * @param fieldId the id of the field to add or remove
     * @param relevant true if the field is relevant
     * @return {@code true} if the field was previously relevant.
     */
    private boolean toggle(Set<ResourceId> relevantSet, ResourceId fieldId, boolean relevant) {
        if(relevant) {
            boolean wasNotPresent = relevantSet.add(fieldId);
            return !wasNotPresent;
        } else {
            boolean wasPresent = relevantSet.remove(fieldId);
            return wasPresent;
        }
    }


    /**
     * Computes the set of fields that are relevant, required, but still missing values.
     */
    private Set<ResourceId> computeMissing(FormInstance record, Set<ResourceId> relevantSet) {
        Set<ResourceId> missing = new HashSet<>();
        for (FormTree.Node node : formTree.getRootFields()) {
            if(node.getField().isRequired()) {
                if(relevantSet.contains(node.getFieldId())) {
                    if(record.get(node.getFieldId()) == null) {
                        missing.add(node.getFieldId());
                    }
                }
            }
        }
        return missing;
    }
}