package org.activityinfo.ui.client.lookup.viewModel;

import com.google.common.base.Optional;
import org.activityinfo.model.expr.ConstantExpr;
import org.activityinfo.model.expr.Exprs;
import org.activityinfo.model.formTree.LookupKey;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.query.QueryModel;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.RecordRef;
import org.activityinfo.observable.Observable;
import org.activityinfo.store.query.shared.FormSource;
import org.activityinfo.ui.client.input.viewModel.ReferenceChoice;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class LookupKeyViewModel {

    private static final String ID_COLUMN_ID = "id";
    private static final String KEY_COLUMN_ID = "key";

    private LookupKey lookupKey;
    private final Observable<Boolean> enabled;
    private final Observable<Optional<String>> selectedKey;
    private final Observable<List<ReferenceChoice>> choices;

    LookupKeyViewModel(FormSource formSource, LookupKey lookupKey,
                       Map<LookupKey, Observable<Optional<String>>> selectedKeys) {

        this.lookupKey = lookupKey;
        this.selectedKey = selectedKeys.get(lookupKey).cache();

        if(lookupKey.isRoot()) {
            this.enabled = Observable.just(true);
            this.choices = formSource.query(query(lookupKey)).transform(this::buildChoiceList);
        } else {
            Observable<Optional<String>> parentKey = selectedKeys.get(lookupKey.getParentLevel()).cache();
            this.enabled = parentKey.transform(key -> key.isPresent());
            this.choices = parentKey.join(key -> {
                if(key.isPresent()) {
                    return formSource.query(query(lookupKey, key.get())).transform(this::buildChoiceList);
                } else {
                    return Observable.just(Collections.emptyList());
                }
            });
        }
    }

    public boolean isLeaf() {
        return lookupKey.getChildLevels().isEmpty();
    }

    private List<ReferenceChoice> buildChoiceList(ColumnSet columnSet) {
        ColumnView ids = columnSet.getColumnView(ID_COLUMN_ID);
        ColumnView keys = columnSet.getColumnView(KEY_COLUMN_ID);

        int numRows = columnSet.getNumRows();
        List<ReferenceChoice> choices = new ArrayList<>(numRows);
        for (int i = 0; i < numRows; i++) {
            ResourceId id = ResourceId.valueOf(ids.getString(i));
            RecordRef ref = new RecordRef(lookupKey.getFormId(), id);
            String key = keys.getString(i);
            choices.add(new ReferenceChoice(ref, key));
        }

        Collections.sort(choices, (o1, o2) -> o1.getLabel().compareTo(o2.getLabel()));

        return choices;
    }

    private QueryModel query(LookupKey lookupKey) {
        QueryModel model = new QueryModel(lookupKey.getFormId());
        model.selectResourceId().as(ID_COLUMN_ID);
        model.selectExpr(lookupKey.getKeyField()).as(KEY_COLUMN_ID);

        return model;
    }


    private QueryModel query(LookupKey level, String parentKey) {
        QueryModel model = query(level);
        model.setFilter(Exprs.equals(level.getParentKey(), new ConstantExpr(parentKey)));

        return model;
    }

    public Observable<Optional<String>> getSelectedKey() {
        return selectedKey;
    }

    public Observable<Boolean> isEnabled() {
        return enabled;
    }

    public Observable<List<ReferenceChoice>> getChoices() {
        return choices;
    }

    public String getLevelLabel() {
        return lookupKey.getLevelLabel();
    }

    public LookupKey getLookupKey() {
        return lookupKey;
    }
}
