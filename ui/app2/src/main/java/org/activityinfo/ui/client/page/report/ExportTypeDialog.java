package org.activityinfo.ui.client.page.report;


import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.*;
import com.extjs.gxt.ui.client.widget.form.Radio;
import com.extjs.gxt.ui.client.widget.form.RadioGroup;
import com.extjs.gxt.ui.client.widget.layout.HBoxLayout;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import org.activityinfo.i18n.shared.I18N;

import java.util.function.Function;
import java.util.logging.Logger;

public class ExportTypeDialog extends Dialog {

    private static final Logger LOGGER = Logger.getLogger(ExportTypeDialog.class.getName());

    public static final String WIDE_FORMAT = "wideFormat";
    public static final String LONG_FORMAT = "longFormat";

    private final Radio wideFormat = new Radio();
    private final Radio longFormat = new Radio();
    private final RadioGroup formatGroup = new RadioGroup();
    private Function<String,Void> callback;

    public ExportTypeDialog(Function<String, Void> callback) {
        super();
        this.callback = callback;
    }

    public ExportTypeDialog() {
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
        setHeight(175);
        setHeadingText(I18N.CONSTANTS.export());
        setButtonAlign(Style.HorizontalAlignment.RIGHT);
        HBoxLayout layout = new HBoxLayout();
        layout.setHBoxLayoutAlign(HBoxLayout.HBoxLayoutAlign.MIDDLE);
        setLayout(layout);
        setClosable(true);
    }

    private void addButtons() {
        setButtons(Dialog.OKCANCEL);
        getButtonById(Dialog.OK).setText(I18N.CONSTANTS.export());

        getButtonById(Dialog.OK).addSelectionListener(new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent buttonEvent) {
                String optionSelected = formatGroup.getValue().getValueAttribute();
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

    private void addRadios() {
        wideFormat.setValueAttribute(WIDE_FORMAT);
        wideFormat.setValue(true);

        longFormat.setValueAttribute(LONG_FORMAT);
        longFormat.setValue(false);

        formatGroup.add(wideFormat);
        formatGroup.add(longFormat);
    }

    private void addOptionPanels() {
        HorizontalPanel panel = new HorizontalPanel();
        panel.setSpacing(5);
        panel.add(createOptionPanel(wideFormat,
                I18N.CONSTANTS.wideFormat(),
                SafeHtmlUtils.fromTrustedString(I18N.CONSTANTS.wideFormatDescription())));
        panel.add(createOptionPanel(longFormat,
                I18N.CONSTANTS.longFormat(),
                SafeHtmlUtils.fromTrustedString(I18N.CONSTANTS.longFormatDescription())));
        add(panel);
    }

    private VerticalPanel createOptionPanel(Radio radio, String radioLabel, SafeHtml optionDescription) {
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
        return optionPanel;
    }

}
