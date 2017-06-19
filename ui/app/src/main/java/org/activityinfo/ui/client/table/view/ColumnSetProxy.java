package org.activityinfo.ui.client.table.view;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.client.loader.RpcProxy;
import com.sencha.gxt.data.shared.loader.PagingLoadConfig;
import com.sencha.gxt.data.shared.loader.PagingLoadResult;
import com.sencha.gxt.data.shared.loader.PagingLoadResultBean;
import org.activityinfo.analysis.table.ColumnRenderer;
import org.activityinfo.analysis.table.EffectiveTableModel;
import org.activityinfo.analysis.table.SimpleColumnFormat;
import org.activityinfo.model.query.ColumnSet;
import org.activityinfo.model.query.ColumnView;
import org.activityinfo.model.resource.ResourceId;

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

    private static class ValueProviderProxy<T> implements ValueProvider<Integer, T> {

        private String id;
        private ColumnRenderer<T> renderer;

        public ValueProviderProxy(String id, ColumnRenderer<T> renderer) {
            this.id = id;
            this.renderer = renderer;
        }

        @Override
        public final String getPath() {
            return id;
        }

        @Override
        public T getValue(Integer rowIndex) {
            return renderer.render(rowIndex);
        }

        @Override
        public final void setValue(Integer object, T value) {
        }
    }


    private ColumnSet columnSet;
    private PendingRequest pendingRequest;

    private final List<ValueProviderProxy> valueProviders = new ArrayList<>();

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

        for (ValueProviderProxy valueProvider : valueProviders) {
            valueProvider.renderer.updateColumnSet(columnSet);
        }

        if(pendingRequest != null) {
            pendingRequest.onSuccess(columnSet);
            pendingRequest = null;
            return true;
        }
        return false;
    }

    public <T> ValueProvider<Integer, T> getValueProvider(String id, ColumnRenderer<T> renderer) {
        if(columnSet != null) {
            renderer.updateColumnSet(columnSet);
        }
        ValueProviderProxy provider = new ValueProviderProxy(id, renderer);
        valueProviders.add(provider);

        return provider;
    }

    public <T> ValueProvider<Integer, T> getValueProvider(SimpleColumnFormat<T> format) {
        return getValueProvider(format.getId(), format.createRenderer());
    }

    public boolean isLoaded() {
        return columnSet != null;
    }

    public ResourceId getRecordId(int rowIndex) {
        if(columnSet == null) {
            throw new IllegalStateException("ColumnSet not loaded");
        }
        ColumnView idView = columnSet.getColumnView(EffectiveTableModel.ID_COLUMN_ID);
        return ResourceId.valueOf(idView.getString(rowIndex));
    }

}
