package org.activityinfo.ui.client.measureDialog.view;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.sencha.gxt.core.client.util.ToggleGroup;
import com.sencha.gxt.widget.core.client.container.FlowLayoutContainer;
import com.sencha.gxt.widget.core.client.form.Radio;
import org.activityinfo.ui.client.measureDialog.model.MeasureSelectionModel;
import org.activityinfo.ui.client.measureDialog.model.MeasureType;

/**
 * Allows the user to choose the type of measure to add
 */
public class MeasureTypePanel implements IsWidget {

    private Radio countRadio;
    private Radio fieldRadio;
    private Radio percentRadio;
    private Radio calculationRadio;
    private final FlowLayoutContainer container;
    private final ToggleGroup toggleGroup;
    private MeasureSelectionModel model;


    public MeasureTypePanel(MeasureSelectionModel model) {
        this.model = model;

        HTML instructions = new HTML("Choose the type of measure:");

        countRadio = new Radio();
        countRadio.setBoxLabel("Count of all records in the selected form(s)");

        percentRadio = new Radio();
        percentRadio.setBoxLabel("Percentage of records matching a criteria");

        fieldRadio = new Radio();
        fieldRadio.setBoxLabel("Totals for one or more fields in the selected form(s)");

        calculationRadio = new Radio();
        calculationRadio.setBoxLabel("Totals for a calculation based on multiple fields");

        toggleGroup = new ToggleGroup();
        toggleGroup.add(countRadio);
        toggleGroup.add(percentRadio);
        toggleGroup.add(fieldRadio);
        toggleGroup.add(calculationRadio);
        toggleGroup.addValueChangeHandler(this::onChange);

        container = new FlowLayoutContainer();
        container.setStylePrimaryName(MeasureResources.INSTANCE.styles().measureTypes());
        container.add(instructions);
        container.add(countRadio);
        container.add(percentRadio);
        container.add(fieldRadio);
        container.add(calculationRadio);
    }

    private void onChange(ValueChangeEvent<HasValue<Boolean>> event) {
        if(event.getValue() == countRadio) {
            model.selectMeasureType(MeasureType.COUNT);

        } else if(event.getValue() == percentRadio) {
            model.selectMeasureType(MeasureType.PERCENT);

        } else if(event.getValue() == fieldRadio) {
            model.selectMeasureType(MeasureType.FIELDS);

        } else if(event.getValue() == calculationRadio) {
            model.selectMeasureType(MeasureType.CALCULATION);
        }
    }


    @Override
    public Widget asWidget() {
        return container;
    }
}
