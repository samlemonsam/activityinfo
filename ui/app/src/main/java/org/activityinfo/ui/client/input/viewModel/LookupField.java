package org.activityinfo.ui.client.input.viewModel;

import org.activityinfo.model.formTree.LookupKey;
import org.activityinfo.model.query.ColumnModel;
import org.activityinfo.model.query.QueryModel;
import org.activityinfo.observable.Observable;
import org.activityinfo.ui.client.store.FormStore;

import java.util.Map;

public class LookupField {
    private FormStore formStore;
    private LookupKey lookupKey;

    private Observable<LookupChoices> choices;
    private Observable<Boolean> enabled;

    LookupField(FormStore formStore, LookupKey lookupKey) {
        this.formStore = formStore;
        this.lookupKey = lookupKey;
        enabled = Observable.just(true);
        choices = formStore.query(buildQuery()).transform(
            columnSet -> new LookupChoices(lookupKey.getFormId(), columnSet));
    }


    private QueryModel buildQuery() {

        QueryModel queryModel = new QueryModel(lookupKey.getFormId());
        queryModel.selectExpr(ColumnModel.ID_SYMBOL).as("id");
        queryModel.selectExpr(lookupKey.getLabelFormula()).as("label");

        return queryModel;
    }

    public Observable<LookupChoices> getChoices() {
        return choices;
    }
}
