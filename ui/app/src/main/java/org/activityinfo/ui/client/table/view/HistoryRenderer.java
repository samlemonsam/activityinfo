package org.activityinfo.ui.client.table.view;

import com.google.common.base.Strings;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.model.form.FieldValueChange;
import org.activityinfo.model.form.RecordHistory;
import org.activityinfo.model.form.RecordHistoryEntry;

import java.util.Date;

public class HistoryRenderer {

    public HistoryRenderer() {}

    private void appendItemSpan(SafeHtmlBuilder html, String string) {
        html.appendHtmlConstant("<h3>");
        html.appendEscaped(string);
        html.appendHtmlConstant("</h3>");
    }

    private void appendTo(RecordHistoryEntry entry, SafeHtmlBuilder html) {
        Date changeTime = new Date(entry.getTime() * 1000L);

        html.appendHtmlConstant("<p>");
        if (entry.getChangeType().equals("created")) {
            if (entry.getSubFieldLabel() == null) {

                appendItemSpan(html, I18N.MESSAGES.siteHistoryCreated(
                        changeTime, entry.getUserName(), entry.getUserEmail()));
            } else {
                appendItemSpan(html, I18N.MESSAGES.siteHistorySubFormCreated(
                        changeTime, entry.getUserName(), entry.getUserEmail(),
                        entry.getSubFieldLabel()));
            }
        } else {
            if (entry.getSubFieldLabel() == null) {
                appendItemSpan(html, I18N.MESSAGES.siteHistoryUpdated(
                        changeTime, entry.getUserName(), entry.getUserEmail()));
            } else {
                appendItemSpan(html, I18N.MESSAGES.siteHistorySubFormUpdated(
                        changeTime, entry.getUserName(), entry.getUserEmail(),
                        entry.getSubFieldLabel()));
            }
        }

        if (!entry.getValues().isEmpty()) {
            html.appendHtmlConstant("<ul style='margin:0px 0px 10px 20px;'>");
            for (FieldValueChange change : entry.getValues()) {
                html.appendHtmlConstant("<li>");

                if (!Strings.isNullOrEmpty(change.getSubFormKey())) {
                    html.appendEscaped(change.getSubFormKey());
                    html.appendEscaped(", ");
                }

                html.appendEscaped(change.getFieldLabel());
                html.appendEscaped(": ");
                html.appendEscaped(change.getNewValueLabel());

                html.appendEscaped(" (");
                if (Strings.isNullOrEmpty(change.getOldValueLabel())) {
                    html.appendEscaped(I18N.MESSAGES.siteHistoryOldValueBlank());
                } else {
                    html.appendEscaped(I18N.MESSAGES.siteHistoryOldValue(change.getOldValueLabel()));
                }
                html.appendEscaped(")");
                html.appendHtmlConstant("</li>");
            }
            html.appendHtmlConstant("</ul>");
        }
    }

    public SafeHtml render(RecordHistory history) {
        SafeHtmlBuilder html = new SafeHtmlBuilder();

        if (history.getEntries().isEmpty()) {
            return renderNoHistory();
        }

        for (RecordHistoryEntry historyEntry : history.getEntries()) {
            appendTo(historyEntry, html);
        }

        return html.toSafeHtml();
    }

    public SafeHtml renderNoHistory() {
        return SafeHtmlUtils.fromTrustedString(I18N.MESSAGES.siteHistoryNotAvailable());
    }

}
