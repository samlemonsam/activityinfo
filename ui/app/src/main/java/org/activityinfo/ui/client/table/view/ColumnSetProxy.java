package org.activityinfo.ui.client.table.view;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.client.loader.RpcProxy;
import com.sencha.gxt.data.shared.loader.PagingLoadConfig;
import com.sencha.gxt.data.shared.loader.PagingLoadResult;
import com.sencha.gxt.data.shared.loader.PagingLoadResultBean;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.ui.client.table.viewModel.EffectiveTableModel;

import java.util.ArrayList;
import java.util.List;

class ColumnSetProxy extends RpcProxy<PagingLoadConfig, PagingLoadResult<Integer>> {


    private class PendingRequest {
        PagingLoadConfig config;
        AsyncCallback<PagingLoadResult<Integer>> callback;

        public PendingRequest(PagingLoadConfig config, AsyncCallback<PagingLoadResult<Integer>> callback) {
            this.config = config;
            this.callback = callback;
        }

        public void onSuccess(ColumnSet columnSet) {

            int actualRows = columnSet.getNumRows() - config.getOffset();
            if(actualRows < 0) {
                actualRows = 0;
            } else if(actualRows > config.getLimit()) {
                actualRows = config.getLimit();
            }

            PagingLoadResultBean<Integer> result = new PagingLoadResultBean<>();
            result.setOffset(config.getOffset());
            result.setData(new RowIndexList(config.getOffset(), actualRows));
            result.setTotalLength(columnSet.getNumRows());
            callback.onSuccess(result);
        }
    }

    private static abstract class ColumnValueProvider<T> implements ValueProvider<Integer, T> {
        protected final String id;
        protected ColumnView view;

        public ColumnValueProvider(String id) {
            this.id = id;
        }

        @Override
        public final String getPath() {
            return id;
        }

        @Override
        public final void setValue(Integer object, T value) {
        }
    }

    private static class StringValueProvider extends ColumnValueProvider<String> {

        public StringValueProvider(String id) {
            super(id);
        }

        @Override
        public String getValue(Integer index) {
            return view.getString(index);
        }
    }

    private static class DoubleValueProvider extends ColumnValueProvider<Double> {
        public DoubleValueProvider(String id) {
            super(id);
        }

        @Override
        public Double getValue(Integer index) {
            double value = view.getDouble(index);
            if(Double.isNaN(value)) {
                return null;
            }
            return value;
        }
    }

    private ColumnSet columnSet;
    private PendingRequest pendingRequest;

    private final List<ColumnValueProvider> valueProviders = new ArrayList<>();

    @Override
    public void load(PagingLoadConfig loadConfig, AsyncCallback<PagingLoadResult<Integer>> callback) {

        PendingRequest request = new PendingRequest(loadConfig, callback);

        if(columnSet != null) {
            request.onSuccess(columnSet);
            return;
        }

        if(pendingRequest != null) {
           pendingRequest.callback.onFailure(new RuntimeException());
        }

        pendingRequest = request;
    }

    public boolean push(ColumnSet columnSet) {
        this.columnSet = columnSet;

        for (ColumnValueProvider valueProvider : valueProviders) {
            valueProvider.view = columnSet.getColumnView(valueProvider.id);
        }

        if(pendingRequest != null) {
            pendingRequest.onSuccess(columnSet);
            pendingRequest = null;
            return true;
        }
        return false;
    }

    private <T> ColumnValueProvider<T> addProvider(ColumnValueProvider<T> provider) {
        if(columnSet != null) {
            provider.view = columnSet.getColumnView(provider.id);
        }
        valueProviders.add(provider);
        return provider;
    }

    public boolean isLoaded() {
        return columnSet != null;
    }

    public ValueProvider<Integer, String> stringValueProvider(String columnId) {
        return addProvider(new StringValueProvider(columnId));
    }

    public ValueProvider<Integer, Double> doubleValueProvider(String columnId) {
        return addProvider(new DoubleValueProvider(columnId));
    }

    public ResourceId getRecordId(int rowIndex) {
        if(columnSet == null) {
            throw new IllegalStateException("ColumnSet not loaded");
        }
        ColumnView idView = columnSet.getColumnView(EffectiveTableModel.ID_COLUMN_ID);
        return ResourceId.valueOf(idView.getString(rowIndex));
    }

}
