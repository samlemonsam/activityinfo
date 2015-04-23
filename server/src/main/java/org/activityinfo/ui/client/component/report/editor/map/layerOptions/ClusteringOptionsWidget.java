package org.activityinfo.ui.client.component.report.editor.map.layerOptions;

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

import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FieldEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.form.Radio;
import com.extjs.gxt.ui.client.widget.form.RadioGroup;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HasValue;
import org.activityinfo.i18n.shared.I18N;
import org.activityinfo.legacy.client.Dispatcher;
import org.activityinfo.legacy.shared.command.GetAdminLevels;
import org.activityinfo.legacy.shared.command.GetSchema;
import org.activityinfo.legacy.shared.command.result.AdminLevelResult;
import org.activityinfo.legacy.shared.model.*;
import org.activityinfo.legacy.shared.reports.model.clustering.AdministrativeLevelClustering;
import org.activityinfo.legacy.shared.reports.model.clustering.AutomaticClustering;
import org.activityinfo.legacy.shared.reports.model.clustering.Clustering;
import org.activityinfo.legacy.shared.reports.model.clustering.NoClustering;
import org.activityinfo.legacy.shared.reports.model.layers.MapLayer;
import org.activityinfo.server.database.hibernate.entity.AdminLevel;
import org.activityinfo.server.endpoint.rest.AdminLevelResource;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/*
 * Shows a list of options to aggregate markers on the map
 */
public class ClusteringOptionsWidget extends LayoutContainer implements HasValue<Clustering> {

    private Clustering value = new NoClustering();

    private class ClusteringRadio extends Radio {
        private Clustering clustering;

        ClusteringRadio(String label, Clustering clustering) {
            this.clustering = clustering;
            this.setBoxLabel(label);
        }

        public Clustering getClustering() {
            return clustering;
        }
    }

    private List<ClusteringRadio> radios;
    private RadioGroup radioGroup;
    private Dispatcher service;

    public ClusteringOptionsWidget(Dispatcher service) {
        super();

        this.service = service;
    }

    private void destroyForm() {
        if (radioGroup != null) {
            radioGroup.removeAllListeners();
        }
        radioGroup = null;
        removeAll();
    }

    public void loadForm(final MapLayer layer) {
        // mask();
        destroyForm();

        GetAdminLevels query = new GetAdminLevels();
        query.setIndicatorIds(Sets.newHashSet(layer.getIndicatorIds()));

        service.execute(query, new AsyncCallback<AdminLevelResult>() {

            @Override
            public void onFailure(Throwable caught) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onSuccess(AdminLevelResult result) {
                buildForm(result.getData());
            }
        });
    }

    private void buildForm(Collection<AdminLevelDTO> adminLevels) {

        radios = Lists.newArrayList();
        radios.add(new ClusteringRadio(I18N.CONSTANTS.none(), new NoClustering()));
        radios.add(new ClusteringRadio(I18N.CONSTANTS.automatic(), new AutomaticClustering()));

        for (AdminLevelDTO level : adminLevels) {

            AdministrativeLevelClustering clustering = new AdministrativeLevelClustering();
            clustering.getAdminLevels().add(level.getId());

            radios.add(new ClusteringRadio(level.getName(), clustering));
        }
        radioGroup = new RadioGroup();
        radioGroup.setOrientation(Orientation.VERTICAL);
        radioGroup.setStyleAttribute("padding", "5px");
        for (ClusteringRadio radio : radios) {
            radioGroup.add(radio);
            if (radio.getClustering().equals(value)) {
                radioGroup.setValue(radio);
            }
        }
        add(radioGroup);

        radioGroup.addListener(Events.Change, new Listener<FieldEvent>() {
            @Override
            public void handleEvent(FieldEvent be) {
                ClusteringRadio radio = (ClusteringRadio) radioGroup.getValue();
                setValue(radio.getClustering(), true);
            }
        });
        layout();
        // unmask();
    }

    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<Clustering> handler) {
        return addHandler(handler, ValueChangeEvent.getType());
    }

    @Override
    public Clustering getValue() {
        return ((ClusteringRadio) radioGroup.getValue()).getClustering();
    }

    @Override
    public void setValue(Clustering value) {
        setValue(value, true);
    }

    @Override
    public void setValue(Clustering value, boolean fireEvents) {
        this.value = value;
        if (radioGroup != null) {
            updateSelectedRadio();
        }
        if (fireEvents) {
            ValueChangeEvent.fire(this, value);
        }
    }

    private void updateSelectedRadio() {
        for (ClusteringRadio radio : radios) {
            if (radio.getClustering().equals(value)) {
                radioGroup.setValue(radio);
                return;
            }
        }
    }
}