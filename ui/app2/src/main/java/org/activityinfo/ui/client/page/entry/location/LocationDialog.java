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
package org.activityinfo.ui.client.page.entry.location;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.*;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.FlowLayout;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.legacy.shared.model.LocationDTO;
import org.activityinfo.legacy.shared.model.LocationTypeDTO;
import org.activityinfo.ui.client.dispatch.Dispatcher;
import org.activityinfo.ui.client.page.entry.form.resources.SiteFormResources;
import org.activityinfo.ui.client.style.legacy.icon.IconImageBundle;

/**
 * Dialog that presents a choice to the user to select an existing location or
 * add a new location.
 */
public class LocationDialog extends Window {

    private Dispatcher dispatcher;
    private final LocationSearchPresenter searchPresenter;
    private final NewLocationPresenter newLocationPresenter;
    private final LocationTypeDTO locationType;

    private Html formHeader;
    private Html addLocationHeader;
    private Html addLocationHelp;
    private Button addLocationButton;

    private Button useLocationButton;

    public interface Callback {
        void onSelected(LocationDTO location, boolean isNew);
    }

    private Callback callback;

    public LocationDialog(Dispatcher dispatcher, LocationTypeDTO locationType) {

        this.dispatcher = dispatcher;
        this.searchPresenter = new LocationSearchPresenter(dispatcher, locationType);
        this.newLocationPresenter = new NewLocationPresenter(locationType.getCountryBounds());
        this.locationType = locationType;

        setHeadingText(I18N.CONSTANTS.chooseLocation());
        setWidth((int) (com.google.gwt.user.client.Window.getClientWidth() * 0.95));
        setHeight((int) (com.google.gwt.user.client.Window.getClientHeight() * 0.95));
        setLayout(new BorderLayout());

        addSearchPanel(locationType);
        addMap();

        getButtonBar().setAlignment(HorizontalAlignment.LEFT);
        getButtonBar().add(useLocationButton = new Button(I18N.CONSTANTS.useLocation(),
                IconImageBundle.ICONS.useLocation(),
                new SelectionListener<ButtonEvent>() {

                    @Override
                    public void componentSelected(ButtonEvent ce) {
                        searchPresenter.accept();
                    }
                }));
        useLocationButton.disable();

        getButtonBar().add(new Button(I18N.CONSTANTS.cancel(), new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent ce) {
                hide();
            }
        }));

        newLocationPresenter.addListener(NewLocationPresenter.ACTIVE_STATE_CHANGED, new Listener<BaseEvent>() {

            @Override
            public void handleEvent(BaseEvent be) {
                if (newLocationPresenter.isActive()) {
                    formHeader.setText(I18N.CONSTANTS.addLocation());
                } else {
                    formHeader.setText(I18N.CONSTANTS.searchLocations());
                }
                addLocationHeader.setVisible(!newLocationPresenter.isActive());
                addLocationHelp.setVisible(!newLocationPresenter.isActive());
                addLocationButton.setVisible(!newLocationPresenter.isActive());
                layout();
            }
        });

        newLocationPresenter.addAcceptedListener(new Listener<LocationEvent>() {

            @Override
            public void handleEvent(LocationEvent event) {
                hide();
                callback.onSelected(event.getLocation(), true);
            }
        });

        searchPresenter.addListener(Events.Select, new Listener<BaseEvent>() {

            @Override
            public void handleEvent(BaseEvent be) {
                if (searchPresenter.getSelection() == null) {
                    useLocationButton.disable();
                } else {
                    useLocationButton.enable();
                    useLocationButton.setText(I18N.MESSAGES.useLocation(searchPresenter.getSelection().getName()));
                }
            }
        });

        searchPresenter.addAcceptListener(new Listener<BaseEvent>() {

            @Override
            public void handleEvent(BaseEvent be) {
                hide();
                callback.onSelected(searchPresenter.getSelection(), false);
            }
        });
    }

    private void addSearchPanel(LocationTypeDTO locationType) {

        LayoutContainer container = new LayoutContainer();
        container.setLayout(new FlowLayout());
        container.setScrollMode(Scroll.AUTOY);
        container.addStyleName(SiteFormResources.INSTANCE.style().locationDialogPane());

        container.add(newHeader(I18N.CONSTANTS.chooseLocation()));
        container.add(newExplanation(I18N.CONSTANTS.chooseLocationDescription()));

        container.add(formHeader = newHeader(I18N.CONSTANTS.searchLocations()));
        container.add(new LocationForm(dispatcher, locationType, searchPresenter, newLocationPresenter));

        container.add(newHeader(I18N.CONSTANTS.searchResults()));
        container.add(new SearchListView(searchPresenter));
        container.add(new SearchStatusView(searchPresenter));

        addLocationHeader = newHeader(I18N.CONSTANTS.addLocation());
        addLocationHelp = newExplanation(I18N.CONSTANTS.addLocationDescription());
        addLocationButton = new Button(I18N.CONSTANTS.newLocation(),
                IconImageBundle.ICONS.add(),
                new SelectionListener<ButtonEvent>() {

                    @Override
                    public void componentSelected(ButtonEvent ce) {
                        newLocationPresenter.setActive(true, true);
                    }
                });
        addLocationButton.addStyleName(SiteFormResources.INSTANCE.style().addLocationButton());

        if (isAddAllowed()) {
            container.add(addLocationHeader);
            container.add(addLocationHelp);
            container.add(addLocationButton);
        }

        BorderLayoutData layout = new BorderLayoutData(LayoutRegion.WEST);
        layout.setSize(350);

        add(container, layout);
    }

    private Html newHeader(String string) {
        Html html = new Html(string);
        html.addStyleName(SiteFormResources.INSTANCE.style().locationDialogHeader());
        return html;
    }

    private Html newExplanation(String string) {
        Html html = new Html(string);
        html.addStyleName(SiteFormResources.INSTANCE.style().locationDialogHelp());
        return html;
    }

    private void addMap() {
        LocationMap mapView = new LocationMap(searchPresenter, newLocationPresenter);
        add(mapView, new BorderLayoutData(LayoutRegion.CENTER));
    }

    public void show(Callback callback) {
        this.callback = callback;
        show();
    }

    /**
     * @return true if this LocationType's workflow allows adding new locations
     */
    private boolean isAddAllowed() {
        return !LocationTypeDTO.CLOSED_WORKFLOW_ID.equals(locationType.getWorkflowId());
    }
}
