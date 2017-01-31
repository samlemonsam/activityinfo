package org.activityinfo.ui.client.component.report.view;
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

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.VBoxLayout;
import com.google.gwt.event.dom.client.ClickHandler;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.ui.client.widget.loading.ExceptionOracle;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author yuriyz on 07/14/2015.
 */
public class ReportViewRetrier {

    private static final Logger LOGGER = Logger.getLogger(ReportViewRetrier.class.getName());
    
    public static void onFailure(LayoutContainer container, Throwable caught, ClickHandler retryCallback) {
        
        LOGGER.log(Level.SEVERE, "Exception while loading reports contents", caught);
        
        container.el().unmask();
        container.removeAll();
        container.add(createRetryPanel(caught, retryCallback));
        container.layout();
    }

    private static ContentPanel createRetryPanel(final Throwable caught, final ClickHandler retryCallback) {
        Button retryButton = new Button(I18N.CONSTANTS.retry());
        retryButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent ce) {
                if (retryCallback != null) {
                    retryCallback.onClick(null);
                }
            }
        });
        Text label = new Text(ExceptionOracle.getExplanation(caught));
        label.setWidth("50%");

        VBoxLayout layout = new VBoxLayout(VBoxLayout.VBoxLayoutAlign.CENTER);

        ContentPanel panel = new ContentPanel();
        panel.setHeaderVisible(false);
        panel.setLayout(layout);
        panel.add(label, new RowData(1, -1, new Margins(4)));
        panel.add(retryButton, new RowData(-1, -1, new Margins(4)));

        return panel;
    }
}
