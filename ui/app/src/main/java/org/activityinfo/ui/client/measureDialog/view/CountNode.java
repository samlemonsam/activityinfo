package org.activityinfo.ui.client.measureDialog.view;

import com.google.gwt.resources.client.ImageResource;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.ui.client.analysis.model.ImmutableMeasureModel;
import org.activityinfo.ui.client.analysis.model.MeasureModel;
import org.activityinfo.ui.client.icons.IconBundle;

public class CountNode extends MeasureTreeNode {

    private FormClass formClass;

    public CountNode(FormClass formClass) {
        this.formClass = formClass;
    }

    @Override
    public String getId() {
        return "_count";
    }

    @Override
    public String getLabel() {
        return "Count of all records in the form";
    }

    @Override
    public ImageResource getIcon() {
        return IconBundle.INSTANCE.count();
    }

    @Override
    public MeasureModel newMeasure() {
        return ImmutableMeasureModel.builder()
                .label(getLabel())
                .formId(formClass.getId())
                .formula("1")
                .build();
    }
}
