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
package org.activityinfo.ui.client.analysis;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import org.activityinfo.ui.client.analysis.view.AnalysisView;
import org.activityinfo.analysis.pivot.viewModel.AnalysisViewModel;
import org.activityinfo.ui.client.store.FormStore;


public class AnalysisActivity extends AbstractActivity {
    private FormStore formStore;
    private AnalysisPlace place;

    public AnalysisActivity(FormStore formStore, AnalysisPlace place) {
        this.formStore = formStore;
        this.place = place;
    }

    @Override
    public void start(AcceptsOneWidget panel, EventBus eventBus) {

        AnalysisViewModel model = new AnalysisViewModel(formStore, place.getId());

        AnalysisView view = new AnalysisView(model);
        panel.setWidget(view);
    }
}
