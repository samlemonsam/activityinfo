/*
 * ActivityInfo
 * Copyright (C) 2009-2013 UNICEF
 * Copyright (C) 2014-2018 BeDataDriven Groep B.V.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.activityinfo.ui.client.page.report.editor;

import com.extjs.gxt.ui.client.Style.IconAlign;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.util.Padding;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.BoxLayout.BoxLayoutPack;
import com.extjs.gxt.ui.client.widget.layout.HBoxLayout;
import com.extjs.gxt.ui.client.widget.layout.HBoxLayout.HBoxLayoutAlign;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.inject.Inject;
import com.google.inject.Provider;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.legacy.shared.reports.model.ReportElement;
import org.activityinfo.legacy.shared.reports.model.TextReportElement;
import org.activityinfo.ui.client.dispatch.Dispatcher;
import org.activityinfo.ui.client.dispatch.ResourceLocator;
import org.activityinfo.ui.client.page.report.editor.ElementDialog.Callback;
import org.activityinfo.ui.client.page.report.template.ChartTemplate;
import org.activityinfo.ui.client.page.report.template.MapTemplate;
import org.activityinfo.ui.client.page.report.template.PivotTableTemplate;
import org.activityinfo.ui.client.page.report.template.ReportElementTemplate;
import org.activityinfo.ui.client.style.legacy.icon.IconImageBundle;

public class AddElementPanel extends LayoutContainer {

    private AddCallback addCallback;
    private Provider<ElementDialog> dialogProvider;

    public interface AddCallback {
        void onAdd(ReportElement element);
    }

    @Inject
    public AddElementPanel(final Dispatcher dispatcher,
                           final ResourceLocator resourceLocator,
                           Provider<ElementDialog> dialogProvider) {
        this.dialogProvider = dialogProvider;

        HBoxLayout layout = new HBoxLayout();
        layout.setHBoxLayoutAlign(HBoxLayoutAlign.STRETCHMAX);
        layout.setPack(BoxLayoutPack.CENTER);
        layout.setPadding(new Padding(15));
        setLayout(layout);

        add(createAddButton(I18N.CONSTANTS.addChart(),
                IconImageBundle.ICONS.barChart(),
                new SelectionListener<ButtonEvent>() {

                    @Override
                    public void componentSelected(ButtonEvent ce) {
                        addElement(new ChartTemplate(dispatcher));
                    }
                }));
        add(createAddButton(I18N.CONSTANTS.addTable(),
                IconImageBundle.ICONS.table(),
                new SelectionListener<ButtonEvent>() {

                    @Override
                    public void componentSelected(ButtonEvent ce) {
                        addElement(new PivotTableTemplate(dispatcher));
                    }
                }));
        add(createAddButton(I18N.CONSTANTS.addMap(), IconImageBundle.ICONS.map(), new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                addElement(new MapTemplate(dispatcher, resourceLocator));
            }
        }));
        add(createAddButton(I18N.CONSTANTS.addText(),
                IconImageBundle.ICONS.text(),
                new SelectionListener<ButtonEvent>() {

                    @Override
                    public void componentSelected(ButtonEvent ce) {
                        addElement(new TextReportElement());
                    }
                }));
    }

    public void setCallback(AddCallback callback) {
        this.addCallback = callback;
    }

    private void addElement(ReportElementTemplate template) {
        template.createElement(new AsyncCallback<ReportElement>() {

            @Override
            public void onSuccess(ReportElement result) {
                addElement(result);
            }

            @Override
            public void onFailure(Throwable caught) {
                // TODO Auto-generated method stub

            }
        });
    }

    private void addElement(final ReportElement element) {
        ElementDialog dialog = dialogProvider.get();
        dialog.show(element, new Callback() {

            @Override
            public void onOK(boolean dirty) {
                addCallback.onAdd(element);
            }

            @Override
            public void onClose(boolean dirty) {

            }
        });

    }

    private Button createAddButton(String text, AbstractImagePrototype icon, SelectionListener<ButtonEvent> listener) {
        Button button = new Button(text, icon, listener);
        button.setIconAlign(IconAlign.TOP);
        return button;
    }
}
