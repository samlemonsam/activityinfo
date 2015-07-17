package org.activityinfo.service.blob;
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

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.activityinfo.service.DeploymentConfiguration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author yuriyz on 8/8/14.
 */
@Singleton
public class BlobDownloadServlet extends HttpServlet {

    public static final String GOOGLE_STORAGE_PREFIX = "/gs/";

    private final BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
    private final DeploymentConfiguration config;

    @Inject
    public BlobDownloadServlet(DeploymentConfiguration config) {
        this.config = config;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doGet(req, resp);

        String blobId = req.getParameter("blobId");
        String filename = req.getParameter("filename");
        String mimeType = req.getParameter("mimetype");

        BlobKey blobKey = blobstoreService.createGsBlobKey(GOOGLE_STORAGE_PREFIX + config.getBlobServiceBucketName() + "/" + blobId);
        resp.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
        resp.setContentType(mimeType);

        blobstoreService.serve(blobKey, resp);
    }

//    private FormClass fetchFormClass(ResourceId formClassId) {
//        return FormClass.fromResource(resourceStore.get(formClassId));
//    }
}
