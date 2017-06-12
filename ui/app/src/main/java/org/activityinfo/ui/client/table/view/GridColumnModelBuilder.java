package org.activityinfo.ui.client.table.view;

import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.widget.core.client.form.NumberPropertyEditor;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;
import com.sencha.gxt.widget.core.client.grid.filters.Filter;
import com.sencha.gxt.widget.core.client.grid.filters.ListFilter;
import com.sencha.gxt.widget.core.client.grid.filters.NumericFilter;
import com.sencha.gxt.widget.core.client.grid.filters.StringFilter;
import org.activityinfo.analysis.table.EffectiveTableColumn;
import org.activityinfo.model.type.NarrativeType;
import org.activityinfo.model.type.barcode.BarcodeType;
import org.activityinfo.model.type.enumerated.EnumItem;
import org.activityinfo.model.type.enumerated.EnumType;
import org.activityinfo.model.type.number.QuantityType;
import org.activityinfo.model.type.primitive.TextType;
import org.activityinfo.model.type.time.LocalDateType;

import java.util.ArrayList;
import java.util.List;

/**
 * Constructs a GXT Grid column model from our EffectiveTableModel.
 */
public class GridColumnModelBuilder {

    private final ColumnSetProxy proxy;
    private final List<ColumnConfig<Integer, ?>> columnConfigs = new ArrayList<>();
    private final List<Filter<Integer, ?>> filters = new ArrayList<>();

    public GridColumnModelBuilder(ColumnSetProxy proxy) {
        this.proxy = proxy;
    }

    public void addAll(List<EffectiveTableColumn> columns) {

        for (EffectiveTableColumn tableColumn : columns) {
            if(tableColumn.getType() instanceof TextType ||
               tableColumn.getType() instanceof BarcodeType ||
               tableColumn.getType() instanceof NarrativeType) {
                addTextColumn(tableColumn);

            } else if(tableColumn.getType() instanceof QuantityType) {
                addQuantityType(tableColumn);

            } else if(tableColumn.getType() instanceof EnumType) {

                addEnumType(tableColumn);

            } else if(tableColumn.getType() instanceof LocalDateType) {
                addDateType(tableColumn);
            }
        }
    }


    private void addQuantityType(EffectiveTableColumn tableColumn) {
        ValueProvider<Integer, Double> valueProvider = proxy.doubleValueProvider(tableColumn.getId());

        ColumnConfig<Integer, Double> config = new ColumnConfig<>(valueProvider);
        config.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
        config.setHeader(tableColumn.getLabel());
        columnConfigs.add(config);

        NumericFilter<Integer, Double> filter = new NumericFilter<>(valueProvider, new NumberPropertyEditor.DoublePropertyEditor());
        filters.add(filter);
    }

    private void addTextColumn(EffectiveTableColumn tableColumn) {
        ValueProvider<Integer, String> valueProvider = proxy.stringValueProvider(tableColumn.getId());

        ColumnConfig<Integer, String> config = new ColumnConfig<>(valueProvider);
        config.setHeader(tableColumn.getLabel());
        columnConfigs.add(config);

        StringFilter<Integer> filter = new StringFilter<>(valueProvider);
        filters.add(filter);
    }

    private void addEnumType(EffectiveTableColumn tableColumn) {

        ValueProvider<Integer, String> valueProvider = proxy.stringValueProvider(tableColumn.getId());

        ColumnConfig<Integer, String> config = new ColumnConfig<>(valueProvider);
        config.setHeader(tableColumn.getLabel());
        columnConfigs.add(config);

        ListStore<String> store = new ListStore<>(x -> x);
        EnumType type = (EnumType) tableColumn.getType();
        for (EnumItem enumItem : type.getValues()) {
            store.add(enumItem.getLabel());
        }

        ListFilter<Integer, String> filter = new ListFilter<>(valueProvider, store);
        filters.add(filter);
    }


    private void addDateType(EffectiveTableColumn tableColumn) {
        addTextColumn(tableColumn);
    }

    public ColumnModel<Integer> buildColumnModel() {
        return new ColumnModel<>(columnConfigs);
    }

    public List<Filter<Integer, ?>> getFilters() {
        return filters;
    }
}
