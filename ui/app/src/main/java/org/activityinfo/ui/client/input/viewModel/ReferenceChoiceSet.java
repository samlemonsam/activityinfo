package org.activityinfo.ui.client.input.viewModel;

import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.RecordRef;

import java.util.ArrayList;
import java.util.List;

/**
 * A set of choices for a reference field.
 */
public class ReferenceChoiceSet {

    private ResourceId formId;
    private ColumnView id;
    private ColumnView label;

    public ReferenceChoiceSet(ResourceId formId, ColumnSet columnSet) {
        this.formId = formId;
        this.id = columnSet.getColumnView("id");
        this.label = columnSet.getColumnView("label");
    }

    public int getCount() {
        return id.numRows();
    }

    public RecordRef getId(int index) {
        return new RecordRef(formId, ResourceId.valueOf(id.getString(index)));
    }

    public String getLabel(int index) {
        return label.getString(index);
    }

    public List<ReferenceChoice> buildChoiceList() {
        List<ReferenceChoice> choiceList = new ArrayList<>();
        for (int i = 0; i < getCount(); i++) {
            choiceList.add(new ReferenceChoice(getId(i), getLabel(i)));
        }
        return choiceList;
    }
}
