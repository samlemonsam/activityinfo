package org.activityinfo.ui.client.table.view;

import com.google.gwt.cell.client.DateCell;
import com.google.gwt.i18n.shared.DateTimeFormat;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.sencha.gxt.core.client.ValueProvider;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.widget.core.client.form.NumberPropertyEditor;
import com.sencha.gxt.widget.core.client.grid.ColumnConfig;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;
import com.sencha.gxt.widget.core.client.grid.HeaderGroupConfig;
import com.sencha.gxt.widget.core.client.grid.filters.DateFilter;
import com.sencha.gxt.widget.core.client.grid.filters.ListFilter;
import com.sencha.gxt.widget.core.client.grid.filters.NumericFilter;
import com.sencha.gxt.widget.core.client.grid.filters.StringFilter;
import org.activityinfo.analysis.table.*;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.model.type.enumerated.EnumItem;
import org.activityinfo.model.type.enumerated.EnumType;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Constructs a GXT Grid column model from our EffectiveTableModel.
 */
public class ColumnModelBuilder {

    private final ColumnSetProxy proxy;
    private final List<ColumnConfig<Integer, ?>> columnConfigs = new ArrayList<>();
    private final List<HeaderGroupConfig> headerGroupConfigs = new ArrayList<>();
    private final List<ColumnView> filters = new ArrayList<>();

    public ColumnModelBuilder(ColumnSetProxy proxy) {
        this.proxy = proxy;
    }

    public void addAll(List<EffectiveTableColumn> columns) {

        for (EffectiveTableColumn tableColumn : columns) {

            tableColumn.accept(new TableColumnVisitor<Void>() {
                @Override
                public Void visitTextColumn(EffectiveTableColumn columnModel, TextFormat textFormat) {
                    addTextColumn(tableColumn, textFormat);
                    return null;
                }

                @Override
                public Void visitNumberColumn(EffectiveTableColumn columnModel, NumberFormat numberFormat) {
                    addNumberColumn(tableColumn, numberFormat);
                    return null;
                }

                @Override
                public Void visitErrorColumn(EffectiveTableColumn columnModel, ErrorFormat errorFormat) {
                    return null;
                }

                @Override
                public Void visitGeoPointColumn(EffectiveTableColumn columnModel, GeoPointFormat geoPointFormat) {
                    addGeoPointColumn(columnModel, geoPointFormat);
                    return null;
                }

                @Override
                public Void visitMultiEnumColumn(EffectiveTableColumn columnModel, MultiEnumFormat multiEnumFormat) {
                    addMultiEnumColumn(columnModel, multiEnumFormat);
                    return null;
                }

                @Override
                public Void visitBooleanColumn(EffectiveTableColumn columnModel, BooleanFormat booleanFormat) {
                    return null;
                }

                @Override
                public Void visitDateColumn(EffectiveTableColumn columnModel, DateFormat dateFormat) {
                    addDateColumn(columnModel, dateFormat);
                    return null;
                }

                @Override
                public Void visitSingleEnumColumn(EffectiveTableColumn columnModel, SingleEnumFormat singleEnumFormat) {
                    addEnumType(columnModel, singleEnumFormat);
                    return null;
                }
            });

        }
    }


    private void addTextColumn(EffectiveTableColumn tableColumn, TextFormat textFormat) {
        ValueProvider<Integer, String> valueProvider = proxy.getValueProvider(textFormat);

        ColumnConfig<Integer, String> config = new ColumnConfig<>(valueProvider);
        config.setHeader(tableColumn.getLabel());
        columnConfigs.add(config);

        StringFilter<Integer> filter = new StringFilter<>(valueProvider);
        filters.add(new ColumnView(tableColumn.getFormula(), filter));
    }


    private void addNumberColumn(EffectiveTableColumn tableColumn, NumberFormat numberFormat) {
        ValueProvider<Integer, Double> valueProvider = proxy.getValueProvider(numberFormat);

        ColumnConfig<Integer, Double> config = new ColumnConfig<>(valueProvider);
        config.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
        config.setHeader(tableColumn.getLabel());
        columnConfigs.add(config);

        NumericFilter<Integer, Double> filter = new NumericFilter<>(valueProvider,
                new NumberPropertyEditor.DoublePropertyEditor());

        filters.add(new ColumnView(tableColumn.getFormula(), filter));
    }

    private void addEnumType(EffectiveTableColumn tableColumn, SingleEnumFormat format) {

        ValueProvider<Integer, String> valueProvider = proxy.getValueProvider(format);

        ColumnConfig<Integer, String> config = new ColumnConfig<>(valueProvider);
        config.setHeader(tableColumn.getLabel());
        columnConfigs.add(config);

        addEnumFilter(valueProvider.getPath(), tableColumn, (EnumType) tableColumn.getType());
    }

    private void addEnumFilter(String path, EffectiveTableColumn columnModel, EnumType enumType) {
        ListStore<EnumItemViewModel> store = new ListStore<>(x -> x.getId());
        for (EnumItem enumItem : enumType.getValues()) {
            store.add(new EnumItemViewModel(enumItem));
        }

        ListFilter<Integer, EnumItemViewModel> filter = new ListFilter<>(new NullValueProvider<>(path), store);
        filter.setUseStoreKeys(true);

        filters.add(new ColumnView(columnModel.getFormula(), filter));
    }

    private void addDateColumn(EffectiveTableColumn tableColumn, DateFormat dateFormat) {
        ValueProvider<Integer, Date> valueProvider = proxy.getValueProvider(dateFormat);

        ColumnConfig<Integer, Date> config = new ColumnConfig<>(valueProvider);
        config.setHeader(tableColumn.getLabel());
        config.setCell(new DateCell(DateTimeFormat.getFormat(DateTimeFormat.PredefinedFormat.DATE_SHORT)));
        columnConfigs.add(config);

        DateFilter<Integer> filter = new DateFilter<>(valueProvider);
        filters.add(new ColumnView(tableColumn.getFormula(), filter));
    }


    private void addMultiEnumColumn(EffectiveTableColumn tableColumn, MultiEnumFormat multiEnumFormat) {
        // Add a single, comma-delimited list for now
        ValueProvider<Integer, String> valueProvider = proxy.getValueProvider(tableColumn.getId(), multiEnumFormat.createRenderer());

        ColumnConfig<Integer, String> config = new ColumnConfig<>(valueProvider);
        config.setHeader(tableColumn.getLabel());
        columnConfigs.add(config);

        addEnumFilter(valueProvider.getPath(), tableColumn, (EnumType) tableColumn.getType());
    }

    private void addGeoPointColumn(EffectiveTableColumn columnModel, GeoPointFormat format) {
        // Add a single, comma-delimited list for now
        ValueProvider<Integer, Double> latProvider =
                proxy.getValueProvider(format.getLatitudeId(), format.createLatitudeRenderer());
        ValueProvider<Integer, Double> lngProvider =
                proxy.getValueProvider(format.getLongitudeId(), format.createLongitudeRenderer());

        int latitudeColumnIndex = columnConfigs.size();

        ColumnConfig<Integer, Double> latitudeConfig = new ColumnConfig<>(latProvider);
        latitudeConfig.setHeader(I18N.CONSTANTS.latitude());
        latitudeConfig.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
        columnConfigs.add(latitudeConfig);

        ColumnConfig<Integer, Double> longitudeConfig = new ColumnConfig<>(lngProvider);
        longitudeConfig.setHeader(I18N.CONSTANTS.longitude());
        longitudeConfig.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
        columnConfigs.add(longitudeConfig);

        HeaderGroupConfig groupConfig = new HeaderGroupConfig(SafeHtmlUtils.fromString(columnModel.getLabel()), 1, 2);
        groupConfig.setRow(0);
        groupConfig.setColumn(latitudeColumnIndex);

        headerGroupConfigs.add(groupConfig);
    }



    public ColumnModel<Integer> buildColumnModel() {
        ColumnModel<Integer> cm = new ColumnModel<>(columnConfigs);

        for (HeaderGroupConfig headerGroupConfig : headerGroupConfigs) {
            cm.addHeaderGroup(headerGroupConfig.getRow(), headerGroupConfig.getColumn(), headerGroupConfig);
        }

        return cm;
    }

    public List<ColumnView> getFilters() {
        return filters;
    }

    private static class NullValueProvider<T, V> implements ValueProvider<T, V> {

        private String path;

        public NullValueProvider(String path) {
            this.path = path;
        }

        @Override
        public V getValue(T object) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setValue(T object, V value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getPath() {
            return path;
        }
    }

    /**
     * We need this instead of EnumItem, because {@link com.sencha.gxt.widget.core.client.grid.filters.ListMenu}
     * calls toString() to get the text for the menu.
     */
    private static class EnumItemViewModel {
        private String id;
        private String label;

        public EnumItemViewModel(EnumItem item) {
            id = item.getId().asString();
            label = item.getLabel();
        }

        public String getId() {
            return id;
        }

        @Override
        public String toString() {
            return label;
        }
    }
}
