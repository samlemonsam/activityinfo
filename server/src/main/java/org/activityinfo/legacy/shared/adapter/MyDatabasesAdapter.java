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
import com.google.common.collect.Lists;
import org.activityinfo.legacy.shared.model.SchemaDTO;
import org.activityinfo.legacy.shared.model.UserDatabaseDTO;
import org.activityinfo.model.form.FormInstance;

import java.util.List;

/**
 * Created by yuriyz on 4/12/2016.
 */
public class MyDatabasesAdapter implements Function<SchemaDTO, List<FormInstance>> {

    @Override
    public List<FormInstance> apply(SchemaDTO schema) {
        List<FormInstance> result = Lists.newArrayList();
        for (UserDatabaseDTO db : schema.getDatabases()) {
            if (db.getAmOwner() || db.isDesignAllowed()) {
                result.add(FolderListAdapter.newFolder(db));
            }
        }
        return result;
    }
}
