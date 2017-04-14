package org.activityinfo.ui.client.input.viewModel;

import com.google.common.base.Predicate;
import org.activityinfo.model.expr.ExprNode;
import org.activityinfo.model.expr.ExprParser;
import org.activityinfo.model.form.FormEvalContext;
import org.activityinfo.model.form.FormInstance;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.ui.client.input.model.FieldInput;
import org.activityinfo.ui.client.input.model.FormInputModel;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
public class FormInputViewModelBuilder {

    private final Logger LOGGER = Logger.getLogger(FormInputViewModelBuilder.class.getName());

    private final FormTree formTree;
    private final FormEvalContext evalContext;

    private Map<ResourceId, Predicate<FormInstance>> relevanceCalculators = new HashMap<>();


    public FormInputViewModelBuilder(FormTree formTree) {
        this.formTree = formTree;
        this.evalContext = new FormEvalContext(formTree.getRootFormClass());

        for (FormTree.Node node : formTree.getRootFields()) {
            if(node.getField().hasRelevanceCondition()) {
                buildRelevanceCalculator(node);
            }
        }
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

        relevanceCalculators.put(node.getFieldId(), new Predicate<FormInstance>() {
            @Override
            public boolean apply(@Nullable FormInstance instance) {
                evalContext.setInstance(instance);
                try {
                    return rootNode.evaluateAsBoolean(evalContext);
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Failed to evaluate relevance condition", e);
                    return true;
                }
            }
        });
    }

    public FormInputViewModel build(FormInputModel inputModel) {

        FormInstance record = new FormInstance(ResourceId.generateId(), formTree.getRootFormClass().getId());

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

        // Determine which fields are "relevant"
        Map<ResourceId, Boolean> relevantMap = new HashMap<>();
        for (FormTree.Node node : formTree.getRootFields()) {
            relevantMap.put(node.getFieldId(), true);
        }

        boolean changing;
        do {
            changing = false;

            for (Map.Entry<ResourceId, Predicate<FormInstance>> field : relevanceCalculators.entrySet()) {

                boolean relevant = field.getValue().apply(record);
                if(!relevant) {
                    record.set(field.getKey(), (FieldValue)null);
                }

                Boolean wasRelevant = relevantMap.put(field.getKey(), relevant);
                if(relevant != wasRelevant) {
                    changing = true;

                }
            }
        } while(changing);

        // Finally, check to ensure that all required -AND- relevant
        // values are provided

        Set<ResourceId> missing = new HashSet<>();
        for (FormTree.Node node : formTree.getRootFields()) {
            if(node.getField().isRequired()) {
                if(relevantMap.get(node.getFieldId()) == Boolean.TRUE) {
                    if(record.get(node.getFieldId()) == null) {
                        missing.add(node.getFieldId());
                        valid = false;
                    }
                }
            }
        }

        return new FormInputViewModel(formTree, inputModel, record.getFieldValueMap(),
                relevantMap, missing, valid);
    }
}