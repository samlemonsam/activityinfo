package org.activityinfo.geoadmin.merge2.view.swing.lookup;

import org.activityinfo.geoadmin.merge2.view.mapping.ReferenceFieldMapping;
import org.activityinfo.geoadmin.merge2.view.swing.Step;
import org.activityinfo.geoadmin.merge2.view.swing.StepPanel;


public class LookupStep implements Step {

    private final ReferenceFieldMapping fieldMapping;

    public LookupStep(ReferenceFieldMapping fieldMapping) {
        this.fieldMapping = fieldMapping;
    }

    @Override
    public String getLabel() {
        return fieldMapping.getTargetFieldLabel() + " Lookup";
    }

    @Override
    public StepPanel createView() {
        return new LookupStepPanel();
    }
}
