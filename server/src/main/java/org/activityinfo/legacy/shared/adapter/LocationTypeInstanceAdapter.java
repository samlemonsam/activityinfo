package org.activityinfo.legacy.shared.adapter;
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

import com.google.common.base.Function;
import org.activityinfo.legacy.shared.model.LocationTypeDTO;
import org.activityinfo.model.form.FormInstance;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;

import javax.annotation.Nonnull;

/**
 * Adapts location type into FormInstance (not FormClass). Special case for querying available LocationTypes for
 * given country.
 *
 * @author yuriyz on 03/12/2015.
 */
public class LocationTypeInstanceAdapter implements Function<LocationTypeDTO, FormInstance> {
    @Nonnull
    @Override
    public FormInstance apply(LocationTypeDTO input) {
        ResourceId id = CuidAdapter.cuid(CuidAdapter.LOCATION_TYPE_DOMAIN, input.getId());
        FormInstance instance = new FormInstance(id, id);

        instance.set(CuidAdapter.field(id, CuidAdapter.NAME_FIELD), input.getName());
        return instance;
    }
}
