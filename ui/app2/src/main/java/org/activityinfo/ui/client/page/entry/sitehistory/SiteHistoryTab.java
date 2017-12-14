package org.activityinfo.ui.client.page.entry.sitehistory;

/*
 * #%L
 * ActivityInfo Server
 * %%
 * Copyright (C) 2009 - 2013 UNICEF
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.google.common.base.Strings;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.legacy.shared.model.SiteDTO;
import org.activityinfo.model.form.RecordHistory;
import org.activityinfo.model.form.RecordHistoryEntry;
import org.activityinfo.model.form.FieldValueChange;
import org.activityinfo.model.type.time.LocalDate;
import org.activityinfo.ui.client.dispatch.ResourceLocator;

import java.util.Date;
import java.util.List;

public class SiteHistoryTab extends TabItem {

    static final Date HISTORY_AVAILABLE_FROM = new LocalDate(2012, 12, 20).atMidnightInMyTimezone();

    private final Html content;
    private ResourceLocator resourceLocator;

    public SiteHistoryTab(ResourceLocator resourceLocator) {
        this.resourceLocator = resourceLocator;

        this.setScrollMode(Scroll.AUTO);

        setText(I18N.CONSTANTS.history());

        content = new Html();
        content.setStyleName("details");
        add(content);
    }

    public void setSite(final SiteDTO site) {

        renderStatus(I18N.CONSTANTS.loading());

        resourceLocator.getFormRecordHistory(site.getFormClassId(), site.getInstanceId())
                .then(new AsyncCallback<RecordHistory>() {
                    @Override
                    public void onFailure(Throwable throwable) {
                        renderStatus(I18N.CONSTANTS.serverError());
                    }

                    @Override
                    public void onSuccess(RecordHistory history) {
                        render(site, history.getEntries());
                    }
                });
    }

    private void renderStatus(String message) {
        SafeHtmlBuilder html = new SafeHtmlBuilder();
        appendItemSpan(html, message);
        content.setHtml(html.toSafeHtml());
    }

    private void render(SiteDTO site, List<RecordHistoryEntry> entries) {

        SafeHtmlBuilder html = new SafeHtmlBuilder();

        if(site.getDateCreated().before(HISTORY_AVAILABLE_FROM)) {
            appendItemSpan(html, I18N.MESSAGES.siteHistoryDateCreated(site.getDateCreated()));
            html.appendHtmlConstant("<br>");
            html.appendEscaped(I18N.MESSAGES.siteHistoryAvailableFrom(HISTORY_AVAILABLE_FROM));
        }

        for (RecordHistoryEntry entry : entries) {
            appendTo(entry, html);
        }
        content.setHtml(html.toSafeHtml());
    }

    private void appendItemSpan(SafeHtmlBuilder html, String string) {
        html.appendHtmlConstant("<span style='color: #15428B; font-weight: bold;'>");
        html.appendEscaped(string);
        html.appendHtmlConstant("</span>");
    }

    private void appendTo(RecordHistoryEntry entry, SafeHtmlBuilder html) {
        Date changeTime = new Date(entry.getTime() * 1000L);

        html.appendHtmlConstant("<p>");
        html.appendHtmlConstant("<span style='color: #15428B; font-weight: bold;'>");
        if(entry.getChangeType().equals("created")) {
            if(entry.getSubFieldLabel() == null) {

                appendItemSpan(html, I18N.MESSAGES.siteHistoryCreated(
                        changeTime, entry.getUserName(), entry.getUserEmail()));
            } else {
                appendItemSpan(html, I18N.MESSAGES.siteHistorySubFormCreated(
                        changeTime, entry.getUserName(), entry.getUserEmail(),
                        entry.getSubFieldLabel()));
            }
        } else {
            if(entry.getSubFieldLabel() == null) {
                appendItemSpan(html, I18N.MESSAGES.siteHistoryUpdated(
                        changeTime, entry.getUserName(), entry.getUserEmail()));
            } else {
                appendItemSpan(html, I18N.MESSAGES.siteHistorySubFormUpdated(
                        changeTime, entry.getUserName(), entry.getUserEmail(),
                        entry.getSubFieldLabel()));                
            }
        }
        html.appendHtmlConstant("<br>");

        if(!entry.getValues().isEmpty()) {
            html.appendHtmlConstant("<ul style='margin:0px 0px 10px 20px; font-size: 11px;'>");
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
                if(Strings.isNullOrEmpty(change.getOldValueLabel())) {
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

}
