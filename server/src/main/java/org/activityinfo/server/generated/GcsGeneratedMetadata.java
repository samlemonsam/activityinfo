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
package org.activityinfo.server.generated;

import com.google.appengine.api.datastore.*;
import com.google.common.escape.Escaper;
import com.google.common.escape.Escapers;
import com.google.common.net.UrlEscapers;
import org.activityinfo.legacy.shared.AuthenticatedUser;

import java.util.Date;

/**
 * Records metadata about a generated user.
 */
class GcsGeneratedMetadata {
    private String id;
    private long owner;
    private String filename;
    private String contentType;
    private boolean completed;
    private Double percentageComplete;
    private Date creationTime;
    
    public GcsGeneratedMetadata(String id) {
        this.id = id;
        this.creationTime = new Date();
    }

    public static GcsGeneratedMetadata load(String id) throws EntityNotFoundException {
        DatastoreService datastoreService = DatastoreServiceFactory.getDatastoreService();
        Entity entity = datastoreService.get(entityKey(id));
        GcsGeneratedMetadata metadata = new GcsGeneratedMetadata(id);
        metadata.filename = (String) entity.getProperty("filename");
        metadata.contentType = (String) entity.getProperty("contentType");
        metadata.owner = (Long)entity.getProperty("owner");
        metadata.completed = (entity.getProperty("completed") == Boolean.TRUE);
        metadata.creationTime = (Date) entity.getProperty("creationTime");
        metadata.percentageComplete = (Double)entity.getProperty("percentageComplete");
        return metadata;
    }
    
    public void save() {
        DatastoreService datastoreService = DatastoreServiceFactory.getDatastoreService();
        Entity entity = new Entity(entityKey(this.id));
        entity.setUnindexedProperty("filename", filename);
        entity.setUnindexedProperty("contentType", contentType);
        entity.setProperty("creationTime", creationTime);
        entity.setUnindexedProperty("completed", completed);
        entity.setProperty("owner", owner);
        entity.setProperty("percentageComplete", percentageComplete);
        datastoreService.put(entity);
    }
    
    
    public void markComplete() {
        DatastoreService datastoreService = DatastoreServiceFactory.getDatastoreService();
        Transaction tx = datastoreService.beginTransaction();
        Entity entity = null;
        try {
            entity = datastoreService.get(tx, entityKey(id));
            Boolean completed = (Boolean) entity.getProperty("completed");
            if(completed) {
                throw new IllegalStateException(id + " is already completed.");
            }
        } catch (EntityNotFoundException e) {
            tx.rollback();
            throw new IllegalStateException("Metadata entity " + entityKey(id) + " has not been saved.");
        }
        entity.setProperty("completed", true);
        datastoreService.put(tx, entity);
        tx.commit();
    }
    
    private static Key entityKey(String id) {
        return KeyFactory.createKey("Generated", id);
    }

    public long getOwner() {
        return owner;
    }

    public void setOwner(long owner) {
        this.owner = owner;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public boolean isCompleted() {
        return completed;
    }

    public Date getCreationTime() {
        return creationTime;
    }

    public double getPercentageComplete() {
        if(completed) {
            return 1.0;
        } else if(percentageComplete != null) {
            return percentageComplete;
        } else {
            return 0d;
        }
    }

    public void setPercentageComplete(Double percentageComplete) {
        this.percentageComplete = percentageComplete;
    }

    public String getId() {
        return id;
    }

    public void setOwner(AuthenticatedUser authenticatedUser) {
        this.owner = authenticatedUser.getUserId();
    }
    
    public String getGcsPath() {
        return getGcsPath(Escapers.nullEscaper());
    }
    
    public String getUrlEscapedGcsPath() {
        return getGcsPath(UrlEscapers.urlPathSegmentEscaper());
    }
    
    private String getGcsPath(Escaper escaper) {
        // Include the user-friendly filename in the GCS object path, which
        // will eliminate the need to include the filename in the Content-disposition header,
        // support for which is extremely unpredictable in IE:
        // http://www.jtricks.com/bits/content_disposition.html
        return "generated/" + id + "/" + escaper.escape(filename);
    }
}
