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
package org.activityinfo.store.mysql.collections;

import com.google.common.base.Optional;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.io.OutputStreamOutStream;
import com.vividsolutions.jts.io.WKBWriter;
import org.activityinfo.model.form.*;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.store.mysql.cursor.MySqlCursorBuilder;
import org.activityinfo.store.mysql.cursor.QueryExecutor;
import org.activityinfo.store.mysql.cursor.RecordCursor;
import org.activityinfo.store.mysql.cursor.RecordFetcher;
import org.activityinfo.store.mysql.mapping.TableMapping;
import org.activityinfo.store.mysql.update.BaseTableInserter;
import org.activityinfo.store.mysql.update.BaseTableUpdater;
import org.activityinfo.store.spi.ColumnQueryBuilder;
import org.activityinfo.store.spi.RecordVersion;
import org.activityinfo.store.spi.TypedRecordUpdate;
import org.activityinfo.store.spi.VersionedFormStorage;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.function.Predicate;


public class SimpleTableStorage implements VersionedFormStorage {

    protected final TableMapping mapping;
    protected final QueryExecutor executor;

    public SimpleTableStorage(TableMapping mapping, QueryExecutor executor) {
        this.mapping = mapping;
        this.executor = executor;
    }

    @Override
    public Optional<FormRecord> get(ResourceId resourceId) {
        return RecordFetcher.fetch(this, resourceId);
    }

    @Override
    public List<FormRecord> getSubRecords(ResourceId resourceId) {
        return Collections.emptyList();
    }

    @Override
    public List<RecordVersion> getVersions(ResourceId recordId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<RecordVersion> getVersionsForParent(ResourceId parentRecordId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public FormClass getFormClass() {
        return mapping.getFormClass();
    }

    @Override
    public void updateFormClass(FormClass formClass) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void update(TypedRecordUpdate update) {
        BaseTableUpdater updater = new BaseTableUpdater(mapping, update.getRecordId());
        updater.update(executor, update);
    }

    @Override
    public void add(TypedRecordUpdate update) {
        BaseTableInserter inserter = new BaseTableInserter(mapping, update.getRecordId());
        inserter.insert(executor, update);
    }

    @Override
    public ColumnQueryBuilder newColumnQuery() {
        return new SimpleTableColumnQueryBuilder(new MySqlCursorBuilder(mapping, executor));
    }

    @Override
    public FormSyncSet getVersionRange(long localVersion, long toVersion, Predicate<ResourceId> visibilityPredicate, java.util.Optional<String> cursor) {
        if(localVersion == mapping.getVersion()) {
            return FormSyncSet.emptySet(getFormClass().getId());
        }

        // Otherwise send the whole shebang...
        RecordCursor recordCursor = new RecordCursor(mapping, executor);
        Iterator<TypedFormRecord> it = recordCursor.execute();

        List<FormRecord> records = new ArrayList<>();
        while(it.hasNext()) {
            records.add(FormRecord.fromInstance(it.next()));
        }

        return FormSyncSet.initial(getFormClass().getId(), records, java.util.Optional.empty());
    }

    @Override
    public long cacheVersion() {
        return mapping.getVersion();
    }

    @Override
    public void updateGeometry(ResourceId recordId, ResourceId fieldId, Geometry geometry)  {

        // Only applies to admin table
        if(!mapping.getBaseTable().equals("adminentity")) {
            throw new UnsupportedOperationException();
        }

        Envelope envelope = geometry.getEnvelopeInternal();

        executor.update("UPDATE adminentity SET X1 = ?, Y1 = ?, X2 = ?, Y2 = ?, geometry = GeomFromWKB(?, 4326) " +
                "WHERE adminentityid = ?",
                Arrays.asList(
                    envelope.getMinX(),
                    envelope.getMinY(),
                    envelope.getMaxX(),
                    envelope.getMaxY(),
                    toBinary(geometry),
                    CuidAdapter.getLegacyIdFromCuid(recordId)));

    }

    /**
     * Convert MultiPolygons to Geometry collections as MySQL does not seem to like them.
     */
    private Geometry fixUpGeometry(Geometry geometry) {
        if(geometry instanceof MultiPolygon) {
            MultiPolygon multiPolygon = (MultiPolygon) geometry;
            if(multiPolygon.getNumGeometries() == 1) {
                return multiPolygon.getGeometryN(0);
            } else {
                Geometry[] polygons = new Geometry[multiPolygon.getNumGeometries()];
                for (int i = 0; i < polygons.length; i++) {
                    polygons[i] = multiPolygon.getGeometryN(i);
                }
                return new GeometryCollection(polygons, multiPolygon.getFactory());
            }
        }
        return geometry;
    }

    private byte[] toBinary(Geometry geometry) {

        WKBWriter writer = new WKBWriter();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            writer.write(fixUpGeometry(geometry), new OutputStreamOutStream(baos));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return baos.toByteArray();
    }
}
