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
package org.activityinfo.ui.client.page.entry.form;

import com.extjs.gxt.ui.client.widget.Component;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.activityinfo.legacy.shared.model.LocationDTO;
import org.activityinfo.legacy.shared.model.LocationTypeDTO;
import org.activityinfo.legacy.shared.model.SiteDTO;

/**
 * LocationFormSection for sites with no location
 */
public class NullLocationFormSection implements LocationFormSection {

    private final LocationTypeDTO locationType;

    public NullLocationFormSection(LocationTypeDTO locationType) {
        this.locationType = locationType;
    }

    @Override
    public void updateForm(LocationDTO location, boolean isNew) {

    }

    @Override
    public void save(AsyncCallback<Void> callback) {
        callback.onSuccess(null);
    }

    @Override
    public boolean validate() {
        return true;
    }

    @Override
    public void updateModel(SiteDTO m) {
        // as hack to support "No" location, we create a "None"
        // location type for each country and create a Location object
        // with the same id and the name of the country
        m.setLocationId(locationType.getId());
    }


    @Override
    public void updateForm(SiteDTO m, boolean isNew) {

    }

    @Override
    public Component asComponent() {
        throw new UnsupportedOperationException();
    }
}
