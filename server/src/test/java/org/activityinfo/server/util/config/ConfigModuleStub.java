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
        return new DeploymentConfiguration(properties);
    }

}
