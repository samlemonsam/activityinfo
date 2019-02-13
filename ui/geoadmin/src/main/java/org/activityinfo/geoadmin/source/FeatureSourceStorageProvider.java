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
package org.activityinfo.geoadmin.source;

import com.google.common.base.Charsets;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.query.QueryModel;
import org.activityinfo.model.query.RowSource;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.store.spi.FormStorage;
import org.activityinfo.store.spi.FormStorageProvider;
import org.geotools.data.shapefile.ShapefileDataStore;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


public class FeatureSourceStorageProvider implements FormStorageProvider {

    public static final String FILE_PREFIX = "file://";
    private Map<ResourceId, FeatureSourceStorage> sources = new HashMap<>();
    
    public boolean isLocalResource(ResourceId resourceId) {
        return resourceId.asString().startsWith(FILE_PREFIX);
    }

    public boolean isLocalQuery(QueryModel queryModel) {
        for (RowSource rowSource : queryModel.getRowSources()) {
            if(!isLocalResource(rowSource.getRootFormId())) {
                return false;
            }
        }
        return true;
    }
    
    public void add(ResourceId id, String path) throws IOException {
        File shapeFile = new File(path);
        ShapefileDataStore dataStore = new ShapefileDataStore(shapeFile.toURI().toURL(), false, Charsets.UTF_8);
        sources.put(id, new FeatureSourceStorage(id, dataStore.getFeatureSource()));
    }
    
    @Override
    public Optional<FormStorage> getForm(ResourceId formId) {

        FeatureSourceStorage accessor = sources.get(formId);
        if(accessor == null) {

            Preconditions.checkArgument(formId.asString().startsWith(FILE_PREFIX),
                    "FeatureSourceCatalog supports only resourceIds starting with file://");

            try {
                File shapeFile = new File(formId.asString().substring(FILE_PREFIX.length()));
                ShapefileDataStore dataStore = new ShapefileDataStore(shapeFile.toURI().toURL(), false, Charsets.UTF_8);
                accessor = new FeatureSourceStorage(formId, dataStore.getFeatureSource());
                sources.put(formId, accessor);

            } catch (Exception e) {
                throw new IllegalArgumentException("Could not load " + formId, e);
            }
        }
        return Optional.<FormStorage>of(accessor);
    }

    @Override
    public Map<ResourceId, FormClass> getFormClasses(Collection<ResourceId> formIds) {
        Map<ResourceId, FormClass> map = new HashMap<>();
        for (ResourceId collectionId : formIds) {
            Optional<FormStorage> collection = getForm(collectionId);
            if(collection.isPresent()) {
                map.put(collectionId, collection.get().getFormClass());
            }
        }
        return map;
    }

    @Override
    public FormClass getFormClass(ResourceId formId) {
        return getForm(formId).get().getFormClass();
    }


}
