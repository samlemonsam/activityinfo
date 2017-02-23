package org.activityinfo.store.mysql.collections;

import com.google.common.base.Optional;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.io.OutputStreamOutStream;
import com.vividsolutions.jts.io.WKBWriter;
import org.activityinfo.model.form.FormClass;
import org.activityinfo.model.form.FormRecord;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.store.mysql.cursor.MySqlCursorBuilder;
import org.activityinfo.store.mysql.cursor.QueryExecutor;
import org.activityinfo.store.mysql.cursor.RecordFetcher;
import org.activityinfo.store.mysql.mapping.TableMapping;
import org.activityinfo.store.mysql.update.BaseTableInserter;
import org.activityinfo.store.mysql.update.BaseTableUpdater;
import org.activityinfo.store.spi.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;


public class SimpleTableStorage implements FormStorage {

    protected final TableMapping mapping;
    protected final Authorizer authorizer;
    protected final QueryExecutor executor;

    public SimpleTableStorage(TableMapping mapping, Authorizer authorizer, QueryExecutor executor) {
        this.mapping = mapping;
        this.authorizer = authorizer;
        this.executor = executor;
    }

    @Override
    public FormPermissions getPermissions(int userId) {
        return authorizer.getPermissions(userId);
    }

    @Override
    public Optional<FormRecord> get(ResourceId resourceId) {
        return RecordFetcher.fetch(this, resourceId);
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
    public void update(RecordUpdate update) {
        BaseTableUpdater updater = new BaseTableUpdater(mapping, update.getRecordId());
        updater.update(executor, update);
    }

    @Override
    public void add(RecordUpdate update) {
        BaseTableInserter inserter = new BaseTableInserter(mapping, update.getRecordId());
        inserter.insert(executor, update);
    }

    @Override
    public ColumnQueryBuilder newColumnQuery() {
        return new SimpleTableColumnQueryBuilder(new MySqlCursorBuilder(mapping, executor));
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
