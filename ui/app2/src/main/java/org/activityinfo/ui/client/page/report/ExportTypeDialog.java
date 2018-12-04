package org.activityinfo.ui.client.page.report;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.*;
import com.extjs.gxt.ui.client.widget.form.Radio;
import com.extjs.gxt.ui.client.widget.form.RadioGroup;
import com.extjs.gxt.ui.client.widget.layout.HBoxLayout;
import com.google.gwt.safehtml.shared.SafeHtml;
import org.activityinfo.i18n.shared.I18N;

import java.util.function.Function;

/**
 * Allows users to select between multiple options for export
 */
public abstract class ExportTypeDialog extends Dialog {

    private static final int SPACING = 5;

    private final RadioGroup optionGroup = new RadioGroup();
    private final HorizontalPanel optionsPanel = new HorizontalPanel();

    private Function<String,Void> callback;

    ExportTypeDialog(Function<String, Void> callback) {
        this();
        this.callback = callback;
    }

    ExportTypeDialog() {
        super();
        initializeComponent();
        addButtons();
        addRadios();
        addOptionPanels();
    }

    public void setCallback(Function<String, Void> callback) {
        this.callback = callback;
    }

    private void initializeComponent() {
        setWidth(400);
        setHeight(225);
        setHeadingText(I18N.CONSTANTS.export());
        setButtonAlign(Style.HorizontalAlignment.RIGHT);
        HBoxLayout layout = new HBoxLayout();
        layout.setHBoxLayoutAlign(HBoxLayout.HBoxLayoutAlign.MIDDLE);
        setLayout(layout);
        setClosable(true);
        optionsPanel.setSpacing(SPACING);
    }

    private void addButtons() {
        setButtons(Dialog.OKCANCEL);
        getButtonById(Dialog.OK).setText(I18N.CONSTANTS.export());

        getButtonById(Dialog.OK).addSelectionListener(new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent buttonEvent) {
                String optionSelected = optionGroup.getValue().getValueAttribute();
                hide();
                if (callback != null) {
                    callback.apply(optionSelected);
                }
            }
        });

        getButtonById(Dialog.CANCEL).addSelectionListener(new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent buttonEvent) {
                hide();
            }
        });
    }

    abstract void addRadios();

    abstract void addOptionPanels();

    void addRadiosToGroup(Radio... radios) {
        for (Radio radio : radios) {
            optionGroup.add(radio);
        }
    }

    Radio createRadio(String valueAttribute, boolean initialValue) {
        Radio radio = new Radio();
        radio.setValueAttribute(valueAttribute);
        radio.setValue(initialValue);
        return radio;
    }

    void createOptionPanel(Radio radio, String radioLabel, SafeHtml optionDescription) {
        VerticalPanel optionPanel = new VerticalPanel();
        optionPanel.setStyleAttribute("background-color", "white");
        optionPanel.setBorders(true);
        optionPanel.setSpacing(5);
        optionPanel.setAutoHeight(true);

        HorizontalPanel selectOptionPanel = new HorizontalPanel();
        selectOptionPanel.add(radio);
        selectOptionPanel.add(new Label(radioLabel));
        selectOptionPanel.setStyleAttribute("background-color", "grey");
        selectOptionPanel.setStyleAttribute("color", "white");
        selectOptionPanel.setStyleAttribute("font-weight", "bold");
        selectOptionPanel.setSpacing(2);
        selectOptionPanel.setVerticalAlign(Style.VerticalAlignment.MIDDLE);

        optionPanel.add(selectOptionPanel);
        optionPanel.add(new Html(optionDescription));
        optionsPanel.add(optionPanel);
    }

    @Override
    public void show() {
        add(optionsPanel);
        super.show();
    }
}
