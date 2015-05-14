package org.activityinfo.server.endpoint.export;

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

import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.RetryOptions;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.tools.cloudstorage.*;
import com.google.common.base.Strings;
import com.google.common.io.ByteStreams;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.activityinfo.legacy.shared.auth.AuthenticatedUser;
import org.activityinfo.server.DeploymentEnvironment;

import javax.inject.Provider;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.Channels;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Exports complete data to an Excel file
 *
 * @author Alex Bertram
 */
@Singleton
public class ExportSitesServlet extends HttpServlet {
    public static final String X_AI_STORAGE_PROXY = "X-AI-Storage-Proxy";
    
    private final Provider<AuthenticatedUser> authenticatedUserProvider;
    private final SecureRandom random = new SecureRandom();

    private static final Logger LOGGER = Logger.getLogger(ExportSitesServlet.class.getName());
    
    @Inject
    public ExportSitesServlet(Provider<AuthenticatedUser> authenticatedUserProvider) {
        this.authenticatedUserProvider = authenticatedUserProvider;
    }


    /**
     * Initiates an export to Excel task. A token is send back to the client as plain text
     * that can be use to poll the status of the export and retrieve the result.
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        // Create a unique key from which the user can retrieve the file from GCS
        String exportId = Long.toString(Math.abs(random.nextLong()), 16);

        TaskOptions options = TaskOptions.Builder.withUrl(ExportSitesTask.END_POINT);
        for(Map.Entry<String, String[]> entry : req.getParameterMap().entrySet()) {
            options.param(entry.getKey(), entry.getValue()[0]);
        }
        options.param("userId", Integer.toString(authenticatedUserProvider.get().getId()));
        options.param("userEmail", authenticatedUserProvider.get().getEmail());
        options.param("exportId", exportId);
        options.param("filename", fileName());
        options.retryOptions(RetryOptions.Builder.withTaskRetryLimit(3));

        QueueFactory.getDefaultQueue().add(options);

        LOGGER.info("Enqueued export with id " + exportId + " on behalf of " +
            authenticatedUserProvider.get().getEmail());
        
        resp.setStatus(HttpServletResponse.SC_ACCEPTED);
        resp.getOutputStream().print(exportId);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        String exportId = req.getParameter("id");
        
        if(Strings.isNullOrEmpty(exportId)) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        // First determine whether the file is available
        GcsService gcs = GcsServiceFactory.createGcsService();
        GcsFilename fileName = new GcsFilename("activityinfo-generated", exportId);
        GcsFileMetadata metadata = gcs.getMetadata(fileName);

        if(metadata == null) {
            resp.setStatus(HttpServletResponse.SC_NO_CONTENT);

        } else if(DeploymentEnvironment.isAppEngineDevelopment()) {
            if("true".equals(req.getParameter("download"))) {
                resp.setContentType(metadata.getOptions().getMimeType());
                resp.setHeader("Content-Disposition", metadata.getOptions().getContentDisposition());
                
                GcsInputChannel readChannel = gcs.openReadChannel(fileName, 1024);
                try(InputStream inputStream = Channels.newInputStream(readChannel)) {
                    ByteStreams.copy(inputStream, resp.getOutputStream());
                }
            } else {
                resp.setStatus(HttpServletResponse.SC_OK);
                resp.getOutputStream().print("/ActivityInfo/export?id=" + exportId + "&download=true");
            }
        } else {
            // First determine whether the file is available

            GcsAppIdentityServiceUrlSigner signer = new GcsAppIdentityServiceUrlSigner();
            String url;
            try {
                url = signer.getSignedUrl("GET", ExportSitesTask.EXPORT_BUCKET_NAME + "/" + exportId);
            } catch (Exception e) {
                throw new RuntimeException("Failed to sign url", e);
            }

            // Workaround for the great embargo of 2014
            // This will allow download links through our proxy instead of
            // through google's network.
            if(!Strings.isNullOrEmpty(req.getHeader(X_AI_STORAGE_PROXY))) {
                url = url.replace("https://storage.googleapis.com", "http://" + req.getHeader(X_AI_STORAGE_PROXY));
            }
            
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getOutputStream().print(url);
        }
    }

    private String fileName() {
        String date = new SimpleDateFormat("YYYY-MM-dd_HHmmss").format(new Date());
        return ("ActivityInfo_Export_" + date + ".xls").replace(" ", "_");
    }

}