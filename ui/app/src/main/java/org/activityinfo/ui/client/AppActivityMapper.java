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
package org.activityinfo.ui.client;

import com.google.gwt.activity.shared.Activity;
import com.google.gwt.activity.shared.ActivityMapper;
import com.google.gwt.place.shared.Place;
import org.activityinfo.storage.LocalStorage;
import org.activityinfo.ui.client.analysis.AnalysisActivity;
import org.activityinfo.ui.client.analysis.AnalysisPlace;
import org.activityinfo.ui.client.catalog.CatalogActivity;
import org.activityinfo.ui.client.catalog.CatalogPlace;
import org.activityinfo.ui.client.input.RecordActivity;
import org.activityinfo.ui.client.input.RecordPlace;
import org.activityinfo.ui.client.store.FormStore;
import org.activityinfo.ui.client.table.TableActivity;
import org.activityinfo.ui.client.table.TablePlace;


public class AppActivityMapper implements ActivityMapper {

    private FormStore formStore;
    private LocalStorage localStorage;

    public AppActivityMapper(FormStore formStore, LocalStorage localStorage) {
        this.formStore = formStore;
        this.localStorage = localStorage;
    }

    @Override
    public Activity getActivity(Place place) {
        if (place instanceof TablePlace) {
            return new TableActivity(formStore, localStorage, (TablePlace) place);
        }

        if (place instanceof AnalysisPlace) {
            return new AnalysisActivity(formStore, ((AnalysisPlace) place));
        }

        if (place instanceof RecordPlace) {
            return new RecordActivity(formStore, (RecordPlace) place);
        }

        if (place instanceof CatalogPlace) {
            return new CatalogActivity(formStore, (CatalogPlace) place);
        }

        return null;
    }
}
