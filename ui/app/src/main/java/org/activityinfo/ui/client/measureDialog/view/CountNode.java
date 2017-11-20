package org.activityinfo.ui.client.measureDialog.view;

import com.google.gwt.resources.client.ImageResource;
import org.activityinfo.model.analysis.ImmutableTableColumn;
import org.activityinfo.model.analysis.TableColumn;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.ui.client.analysis.model.ImmutableMeasureModel;
import org.activityinfo.ui.client.analysis.model.MeasureModel;
import org.activityinfo.ui.client.icons.IconBundle;

import java.util.Optional;

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

    @Override
    public Optional<TableColumn> newTableColumn() {
        return Optional.of(ImmutableTableColumn.builder()
            .label(getLabel())
            .formula("1")
            .build());
    }
}
