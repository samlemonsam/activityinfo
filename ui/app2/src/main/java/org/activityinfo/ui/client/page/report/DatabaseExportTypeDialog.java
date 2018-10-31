package org.activityinfo.ui.client.page.report;

import com.extjs.gxt.ui.client.widget.form.Radio;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import org.activityinfo.i18n.shared.I18N;

import java.util.function.Function;

/**
 * Allows users to select between Wide Format and Long Format for Database exports
 */
public class DatabaseExportTypeDialog extends ExportTypeDialog {

    public static final String WIDE_FORMAT = "wideFormat";
    public static final String LONG_FORMAT = "longFormat";

    private Radio wideFormat;
    private Radio longFormat;

    public DatabaseExportTypeDialog(Function<String, Void> callback) {
        super(callback);
    }

    public DatabaseExportTypeDialog() {
        super();
    }

    @Override
    void addRadios() {
        wideFormat = createRadio(WIDE_FORMAT, true);
        longFormat = createRadio(LONG_FORMAT, false);
        addRadiosToGroup(wideFormat, longFormat);
    }

    @Override
    void addOptionPanels() {
        createOptionPanel(wideFormat,
                I18N.CONSTANTS.wideFormat(),
                SafeHtmlUtils.fromTrustedString(I18N.CONSTANTS.wideFormatDescription()));
        createOptionPanel(longFormat,
                I18N.CONSTANTS.longFormat(),
                SafeHtmlUtils.fromTrustedString(I18N.CONSTANTS.longFormatDescription()));
    }

}
