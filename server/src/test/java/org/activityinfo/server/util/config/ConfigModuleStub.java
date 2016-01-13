package org.activityinfo.server.util.config;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import org.activityinfo.service.DeploymentConfiguration;

import java.util.Properties;

public class ConfigModuleStub extends AbstractModule {

    @Override
    protected void configure() {

    }

    @Provides
    public DeploymentConfiguration provideDeploymentConfig() {
        Properties properties = new Properties();
        properties.setProperty(DeploymentConfiguration.BLOBSERVICE_GCS_BUCKET_NAME, "app_default_bucket");
        return new DeploymentConfiguration(properties);
    }

}
