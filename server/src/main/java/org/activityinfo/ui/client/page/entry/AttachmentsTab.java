package org.activityinfo.ui.client.page.entry;

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

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.ListViewEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.ListView;
import com.extjs.gxt.ui.client.widget.ListViewSelectionModel;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.toolbar.SeparatorToolItem;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.safehtml.shared.UriUtils;
import com.google.gwt.text.shared.SafeHtmlRenderer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.legacy.client.Dispatcher;
import org.activityinfo.legacy.shared.command.GetSiteAttachments;
import org.activityinfo.legacy.shared.command.result.SiteAttachmentResult;
import org.activityinfo.legacy.shared.model.SiteAttachmentDTO;
import org.activityinfo.legacy.shared.model.SiteDTO;
import org.activityinfo.ui.client.EventBus;
import org.activityinfo.ui.client.page.common.toolbar.ActionToolBar;
import org.activityinfo.ui.client.page.common.toolbar.UIActions;

import java.util.List;

public class AttachmentsTab extends TabItem implements AttachmentsPresenter.View {

    interface Templates extends SafeHtmlTemplates {

        @Template("<dd><img src=\"{0}\" title=\"{1}\"><span>{1}</span></dd>")
        SafeHtml item(SafeUri iconUri, String filename);

    }

    private static final Templates TEMPLATES = GWT.create(Templates.class);

    protected ActionToolBar toolBar;
    private ContentPanel panel;
    protected ListStore<SiteAttachmentDTO> store;

    private AttachmentsPresenter presenter;
    private final Dispatcher dispatcher;
    private final EventBus eventBus;

    private ListView<SiteAttachmentDTO> attachmentList;

    public AttachmentsTab(Dispatcher service, final EventBus eventBus) {
        this.dispatcher = service;
        this.eventBus = eventBus;
        presenter = new AttachmentsPresenter(service, this);

        setText(I18N.CONSTANTS.attachment());
        setLayout(new FitLayout());

        createToolBar();

        panel = new ContentPanel();
        panel.setHeadingText(I18N.CONSTANTS.attachment());
        panel.setScrollMode(Style.Scroll.AUTOY);
        panel.setTopComponent(toolBar);
        panel.setLayout(new FitLayout());

        store = new ListStore<SiteAttachmentDTO>();

        attachmentList = new ListView<SiteAttachmentDTO>();
        attachmentList.setRenderer(new ListRenderer());
        attachmentList.setBorders(false);
        attachmentList.setStore(store);
        attachmentList.setItemSelector("dd");
        attachmentList.setOverStyle("over");
        
        ListViewSelectionModel<SiteAttachmentDTO> selectionModel = new ListViewSelectionModel<>();
        selectionModel.setSelectionMode(Style.SelectionMode.SINGLE);
        
        attachmentList.setSelectionModel(selectionModel);

        attachmentList.addListener(Events.Select, new Listener<ListViewEvent<SiteAttachmentDTO>>() {

            @Override
            public void handleEvent(ListViewEvent<SiteAttachmentDTO> event) {
                toolBar.setActionEnabled(UIActions.DELETE, true);
            }
        });

        attachmentList.addListener(Events.DoubleClick, new Listener<ListViewEvent<SiteAttachmentDTO>>() {

            @Override
            public void handleEvent(ListViewEvent<SiteAttachmentDTO> event) {
                event.getModel().getBlobId();
                Window.Location.assign(GWT.getModuleBaseURL() + "attachment?blobId=" + event.getModel().getBlobId());
            }
        });
        panel.add(attachmentList);

        add(panel);
    }

    public void createToolBar() {

        toolBar = new ActionToolBar();
        toolBar.addUploadButton();
        toolBar.add(new SeparatorToolItem());
        toolBar.addDeleteButton();
        toolBar.setListener(presenter);
        toolBar.setUploadEnabled(false);
        toolBar.setDeleteEnabled(false);

    }

    @Override
    public void setSelectionTitle(String title) {
        panel.setHeadingText(I18N.CONSTANTS.attachment() + " - " + title);

    }

    @Override
    public void setActionEnabled(String id, boolean enabled) {
        toolBar.setActionEnabled(id, enabled);
    }

    @Override
    public void setAttachmentStore(int siteId) {

        GetSiteAttachments getAttachments = new GetSiteAttachments();
        getAttachments.setSiteId(siteId);

        dispatcher.execute(getAttachments, new AsyncCallback<SiteAttachmentResult>() {
            @Override
            public void onFailure(Throwable caught) {
                // callback.onFailure(caught);
            }

            @Override
            public void onSuccess(SiteAttachmentResult result) {
                store.removeAll();
                store.add(result.getData());
            }
        });
    }

    @Override
    public String getSelectedItem() {
        return attachmentList.getSelectionModel().getSelectedItem().getBlobId();
    }

    @Override
    public void refreshList() {
        attachmentList.refresh();
    }

    public void setSite(SiteDTO site) {
        presenter.showSite(site);
        toolBar.setActionEnabled(UIActions.UPLOAD, true);
    }

    public void onNoSelection() {
        store.removeAll();
        toolBar.setActionEnabled(UIActions.UPLOAD, false);
        toolBar.setActionEnabled(UIActions.DELETE, false);
    }

    private class ListRenderer implements SafeHtmlRenderer<List<SiteAttachmentDTO>> {

        @Override
        public SafeHtml render(List<SiteAttachmentDTO> siteAttachmentDTOs) {
            return null;
        }

        @Override
        public void render(List<SiteAttachmentDTO> attachments, SafeHtmlBuilder html) {

            SafeUri iconUrl = UriUtils.fromTrustedString(GWT.getModuleBaseURL() + "/image/attach.png");

            html.appendHtmlConstant("<dl>");
            for (SiteAttachmentDTO attachment : attachments) {
                html.append(TEMPLATES.item(iconUrl, attachment.getFileName()));
            }

            html.appendHtmlConstant("<div style=\"clear:left;\"></div>");
            html.appendHtmlConstant("</dl>");
        }
    }
}
