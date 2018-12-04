package org.activityinfo.ui.client.page.report;

import com.extjs.gxt.ui.client.widget.form.Radio;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import org.activityinfo.i18n.shared.I18N;

import java.util.function.Function;

/**
 * Allows users to select between Classic structure or Flexible Form structure for Activity exports
 */
public class ActivityExportTypeDialog extends ExportTypeDialog {

    public static final String CLASSIC_STRUCTURE = "classicStructure";
    public static final String FORM_STRUCTURE = "formStructure";

    private Radio classicStructure;
    private Radio formStructure;

    public ActivityExportTypeDialog(Function<String, Void> callback) {
        super(callback);
    }

    public ActivityExportTypeDialog() {
        super();
    }

    @Override
    void addRadios() {
        classicStructure = createRadio(CLASSIC_STRUCTURE, true);
        formStructure = createRadio(FORM_STRUCTURE, false);
        addRadiosToGroup(classicStructure, formStructure);
    }

    @Override
    void addOptionPanels() {
        createOptionPanel(classicStructure,
                I18N.CONSTANTS.classicExportStructure(),
                SafeHtmlUtils.fromTrustedString(I18N.CONSTANTS.classicExportStructureDescription()));
        createOptionPanel(formStructure,
                I18N.CONSTANTS.formExportStructure(),
                SafeHtmlUtils.fromTrustedString(I18N.CONSTANTS.formExportStructureDescription()));
    }

}
