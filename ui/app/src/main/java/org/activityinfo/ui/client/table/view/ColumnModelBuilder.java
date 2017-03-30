package org.activityinfo.ui.client.table.view;

import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.widget.core.client.form.NumberPropertyEditor;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;
import com.sencha.gxt.widget.core.client.grid.filters.Filter;
import com.sencha.gxt.widget.core.client.grid.filters.NumericFilter;
import org.activityinfo.model.type.number.QuantityType;
import org.activityinfo.model.type.primitive.TextType;
import org.activityinfo.ui.client.table.viewModel.EffectiveColumn;

import java.util.ArrayList;
import java.util.List;

public class ColumnModelBuilder {

    private final ColumnSetProxy proxy;
    private final List<ColumnConfig<Integer, ?>> columnConfigs = new ArrayList<>();
    private final List<Filter<Integer, ?>> filters = new ArrayList<>();

    public ColumnModelBuilder(ColumnSetProxy proxy) {
        this.proxy = proxy;
    }

    public void addAll(List<EffectiveColumn> columns) {

        for (EffectiveColumn tableColumn : columns) {
            if(tableColumn.getType() instanceof TextType) {
                addTextColumn(tableColumn);

            } else if(tableColumn.getType() instanceof QuantityType) {
                addQuantityType(tableColumn);
            }
        }
    }

    private void addQuantityType(EffectiveColumn tableColumn) {
        ValueProvider<Integer, Double> valueProvider = proxy.doubleValueProvider(tableColumn.getId());

        ColumnConfig<Integer, Double> config = new ColumnConfig<>(valueProvider);
        config.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
        config.setHeader(tableColumn.getLabel());
        columnConfigs.add(config);

        NumericFilter<Integer, Double> filter = new NumericFilter<>(valueProvider, new NumberPropertyEditor.DoublePropertyEditor());
        filters.add(filter);
    }

    private void addTextColumn(EffectiveColumn tableColumn) {
        ValueProvider<Integer, String> valueProvider = proxy.stringValueProvider(tableColumn.getId());

        ColumnConfig<Integer, String> config = new ColumnConfig<>(valueProvider);
        config.setHeader(tableColumn.getLabel());
        columnConfigs.add(config);

    }

    public ColumnModel<Integer> buildColumnModel() {
        return new ColumnModel<>(columnConfigs);
    }

    public List<Filter<Integer, ?>> getFilters() {
        return filters;
    }
}
