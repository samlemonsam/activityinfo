package org.activityinfo.server.util.blob;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;

import java.io.File;

public class BlobServiceModuleStub extends AbstractModule {

    @Override
    protected void configure() {

    }

    @Provides
    public BlobService provideBlobService() {
        File target = new File("build");
        File blobRoot = new File(target, "blobs");
        blobRoot.mkdirs();

        return new FsBlobService(blobRoot);
    }
}
