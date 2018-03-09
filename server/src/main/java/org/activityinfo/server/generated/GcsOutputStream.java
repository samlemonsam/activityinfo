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

import com.google.appengine.tools.cloudstorage.GcsFileOptions;
import com.google.appengine.tools.cloudstorage.GcsFilename;
import com.google.appengine.tools.cloudstorage.GcsService;
import com.google.appengine.tools.cloudstorage.GcsServiceFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.util.logging.Logger;

/**
 * Wraps an output stream to a GCS object, marking the resource as complete when 
 * the stream is closed.
 */
class GcsOutputStream extends OutputStream {
    
    private static final Logger LOGGER = Logger.getLogger(GcsOutputStream.class.getName());

    private final GcsGeneratedMetadata metadata;
    private final OutputStream out;
    private boolean closed = false;

    public GcsOutputStream(String bucket, GcsGeneratedMetadata metadata) throws IOException {
        GcsService gcs = GcsServiceFactory.createGcsService();
        GcsFileOptions fileOptions = new GcsFileOptions.Builder()
                .mimeType(metadata.getContentType())
                .contentDisposition("attachment")
                .build();
        GcsFilename fileName = new GcsFilename(bucket, metadata.getGcsPath());

        this.metadata = metadata;
        this.out = Channels.newOutputStream(gcs.createOrReplace(fileName, fileOptions));
    }

    @Override
    public void write(int b) throws IOException {
        out.write(b);
    }

    @Override
    public void flush() throws IOException {
        out.flush();
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        out.write(b, off, len);
    }

    @Override
    public void write(byte[] b) throws IOException {
        out.write(b);
    }

    @Override
    public void close() throws IOException {
        if(closed) {
            LOGGER.warning("Output stream for " + metadata + " is already closed.");
        } else {
            closed = true;
            LOGGER.info("Closing and finalizing generated resource " + metadata.getId());
            out.close();
            metadata.markComplete();
        }
    }
}
