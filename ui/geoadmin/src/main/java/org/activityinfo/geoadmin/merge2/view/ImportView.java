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
package org.activityinfo.geoadmin.merge2.view;

import com.google.common.base.Function;
import com.vividsolutions.jts.geom.Geometry;
import org.activityinfo.geoadmin.merge2.model.ImportModel;
import org.activityinfo.geoadmin.merge2.view.mapping.FieldMapping;
import org.activityinfo.geoadmin.merge2.view.mapping.FormMapping;
import org.activityinfo.geoadmin.merge2.view.match.*;
import org.activityinfo.geoadmin.merge2.view.profile.FieldProfile;
import org.activityinfo.geoadmin.merge2.view.profile.FormProfile;
import org.activityinfo.geoadmin.merge2.view.swing.SwingSchedulers;
import org.activityinfo.geoadmin.model.GeoAdminClient;
import org.activityinfo.geoadmin.source.FeatureSourceStorageProvider;
import org.activityinfo.geoadmin.source.FeatureSourceStorage;
import org.activityinfo.geoadmin.source.GeometryConverter;
import org.activityinfo.model.formTree.FormTree;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.legacy.KeyGenerator;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.resource.RecordTransactionBuilder;
import org.activityinfo.model.resource.RecordUpdate;
import org.activityinfo.observable.Observable;
import org.activityinfo.observable.Scheduler;
import org.activityinfo.store.ResourceStore;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.type.GeometryType;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;


public class ImportView {

    private final ImportModel model;
    private final ResourceStore store;
    private final Scheduler scheduler;
    
    private final Observable<FormProfile> sourceProfile;
    private final Observable<FormProfile> targetProfile;
    private final Observable<InstanceMatchGraph> matchGraph;
    private final Observable<KeyFieldPairSet> keyFields;
    private final MatchTable matchTable;
    private final Observable<FormMapping> mapping;


    public ImportView(ResourceStore store, ImportModel model) {
        this.store = store;
        this.model = model;
        
        scheduler = SwingSchedulers.fromExecutor(Executors.newCachedThreadPool());
        
        sourceProfile = profile(model.getSourceFormId());
        targetProfile = profile(model.getTargetFormId());
        keyFields = KeyFieldPairSet.compute(sourceProfile, targetProfile);
        mapping = FormMapping.computeFromMatching(store, keyFields, model.getReferenceMatches());

        matchGraph = InstanceMatchGraph.build(scheduler, keyFields);
        matchTable = new MatchTable(model, matchGraph);
    }

    private Observable<FormProfile> profile(Observable<ResourceId> formId) {
        Observable<FormTree> formTree = formId.join(new Function<ResourceId, Observable<FormTree>>() {
            @Nullable
            @Override
            public Observable<FormTree> apply(ResourceId input) {
                return store.getFormTree(input);
            }
        });
    
        return FormProfile.profile(store, formTree);
    }

    public Observable<FormProfile> getSourceProfile() {
        return sourceProfile;
    }

    public Observable<FormProfile> getTargetProfile() {
        return targetProfile;
    }


    public Observable<FormProfile> getProfile(MatchSide side) {
        if(side == MatchSide.SOURCE) {
            return getSourceProfile();
        } else {
            return getTargetProfile();
        }
    }

    public MatchTable getMatchTable() {
        return matchTable;
    }

    public Observable<FormMapping> getMapping() {
        return mapping;
    }

    public ImportModel getModel() {
        return model;
    }


    /**
     * Based on the users explict choices and the automatic matching / mapping, 
     * build a transaction to effect the import.
     * @param client
     */
    public void runUpdate(GeoAdminClient client) {
        RecordTransactionBuilder tx = new RecordTransactionBuilder();

        ResourceId targetFormId = model.getTargetFormId().get();
        
        KeyGenerator generator = new KeyGenerator();

        Map<ResourceId, ResourceId> idMap = new HashMap<>();

        MatchTable matchTable = getMatchTable();
        int numRows = matchTable.getRowCount();
        for (int i = 0; i < numRows; i++) {
            MatchRow matchRow = matchTable.get(i);
            if (!matchRow.isMatched(MatchSide.SOURCE)) {
                // no corresponding row in the source:
                // delete unmatched target
                tx.delete(targetFormId, matchRow.getTargetId().get());

            } else {

                RecordUpdate update;
                ResourceId targetId;
                if (matchRow.isMatched(MatchSide.TARGET)) {
                    // update target with properties from the source
                    targetId = matchRow.getTargetId().get();
                    update = tx.update(targetFormId, targetId);
                } else {
                    // create a new instance with properties from the source
                    targetId = CuidAdapter.entity(generator.generateInt());
                    update = tx.create(targetFormId, targetId);
                }

                idMap.put(matchRow.getSourceId().get(), targetId);
                
                // apply properties from field mapping
                for (FieldMapping fieldMapping : mapping.get().getFieldMappings()) {
                    update.setFieldValue(
                            fieldMapping.getTargetFieldId(), 
                            fieldMapping.mapFieldValue(matchRow.getSourceRow()));
                }
            }
        }

        client.executeTransaction(tx);
        try {
            updateGeometry(client, idMap);
        } catch (IOException e) {
            throw new RuntimeException("Exception updating geometry");
        }
    }

    private void updateGeometry(GeoAdminClient client, Map<ResourceId, ResourceId> idMap) throws IOException {
        FeatureSourceStorageProvider catalog = new FeatureSourceStorageProvider();
        FeatureSourceStorage formStorage = (FeatureSourceStorage) catalog.getForm(getModel().getSourceFormId().get()).get();

        ResourceId targetFormId = getModel().getTargetFormId().get();
        FieldProfile targetField = getTargetProfile().get().getGeometryField();
        if(targetField == null) {
            System.err.println("No geometry field to update.");
            return;
        }

        int sourceIndex = formStorage.getGeometryAttributeIndex();
        if(sourceIndex == -1) {
            System.err.println("No source geometry field.");
            return;
        }

        SimpleFeatureSource featureSource = formStorage.getFeatureSource();
        GeometryType geometryType = (GeometryType) featureSource.getSchema().getDescriptor(sourceIndex).getType();
        GeometryConverter converter = new GeometryConverter(geometryType);

        SimpleFeatureIterator it = featureSource.getFeatures().features();
        while(it.hasNext()) {
            SimpleFeature feature = it.next();
            ResourceId sourceId = ResourceId.valueOf(feature.getID());
            ResourceId targetId = idMap.get(sourceId);

            if(targetId != null) {
                Geometry geometry = converter.toWgs84(feature.getAttribute(sourceIndex));

                System.out.print("Updating geometry for " + targetId + " [" + geometry.getGeometryType() + "] ... ");

                try {
                    client.updateGeometry(targetFormId, targetId, targetField.getId(), geometry);
                    System.out.println("OK");
                } catch (Exception e) {
                    System.out.println("ERROR: " + e.getMessage());
                }
            }
        }
    }
}


