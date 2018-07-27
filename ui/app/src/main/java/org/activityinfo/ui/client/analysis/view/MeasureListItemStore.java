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
package org.activityinfo.ui.client.analysis.view;

import com.sencha.gxt.data.shared.ListStore;
import org.activityinfo.observable.Observable;
import org.activityinfo.analysis.pivot.viewModel.AnalysisViewModel;
import org.activityinfo.analysis.pivot.viewModel.EffectiveMeasure;
import org.activityinfo.analysis.pivot.viewModel.EffectiveModel;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class MeasureListItemStore extends ListStore<MeasureListItem> {

    private static final Logger LOGGER = Logger.getLogger(MeasureListItemStore.class.getName());

    public MeasureListItemStore(AnalysisViewModel viewModel) {
        super(MeasureListItem::getId);

        Observable<EffectiveModel> effectiveModel = viewModel.getEffectiveModel();
        assert effectiveModel != null;

        Observable<List<MeasureListItem>> measures = effectiveModel.transform(em -> {
            List<MeasureListItem> list = new ArrayList<>();
            for (EffectiveMeasure effectiveMeasure : em.getMeasures()) {
                MeasureListItem li = new MeasureListItem(effectiveMeasure);
                list.add(li);
            }
            return list;
        });

        measures.subscribe(observer -> {
            clear();
            if(observer.isLoaded()) {
                replaceAll(observer.get());
            }
        });
    }
}
