package org.activityinfo.server.branding;

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

import com.google.appengine.api.appidentity.AppIdentityService;
import com.google.appengine.api.appidentity.AppIdentityServiceFactory;
import com.google.appengine.tools.cloudstorage.*;
import com.google.common.base.Charsets;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.io.CharStreams;
import com.google.inject.Inject;
import com.google.inject.Provider;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.channels.Channels;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.google.common.base.Strings.emptyToNull;

/**
 * Provides information on the domain branding to use based
 * on this thread's current request.
 */
public class DomainProvider implements Provider<Domain> {

    private static final Logger LOGGER = Logger.getLogger(DomainProvider.class.getName());

    private final Provider<HttpServletRequest> request;
    private final LoadingCache<String, Domain> domainCache;
    
    @Inject
    public DomainProvider(Provider<HttpServletRequest> request) {
        this.request = request;
        this.domainCache = CacheBuilder.newBuilder()
                .concurrencyLevel(10)
                .build(new DomainLoader());
    }

    @Override
    public Domain get() {
        String host = getBrandHostName();
        try {
            return domainCache.get(host);
        } catch (ExecutionException e) {
            LOGGER.log(Level.SEVERE, "Exception while loading domain properties for " + host, e);
            return defaultDomain();
        }
    }

    private Domain defaultDomain() {
        Domain result;
        result = new Domain();
        result.setTitle("ActivityInfo");
        result.setHost(getBrandHostName());
        result.setPort(request.get().getServerPort());
        result.setSignUpAllowed(true);
        return result;
    }

    private String getExternalHostName() {
        String host = getHeader("X-Forwarded-Host");
        if(host != null) {
            return host;
        }
        return request.get().getServerName();
    }

    /**
     *
     * Return the hostname to use for looking up the branded domain.
     *
     * If the request is forwarded from a proxy server, this host name might
     * be different from both the requested host name ('proxy.default.activityinfoeu.appspot.com')
     * and the host name requested by the end user ('proxy.activityinfo.org') if we are
     * are setting up an alias to an existing host.
     *
     * @return the host name to use for looking up the branded version of AI to serve.
     *
     */
    private String getBrandHostName() {

        String host = getHeader("X-AI-Domain");
        if(host != null) {
            return host;
        }
        host = getHeader("X-Forwarded-Host");
        if(host != null) {
            return host;
        }
        return request.get().getServerName();
    }

    private String getHeader(String headerName) {
        return emptyToNull(request.get().getHeader(headerName));
    }

    private class DomainLoader extends CacheLoader<String, Domain> {

        private final GcsService gcsService =
                GcsServiceFactory.createGcsService(RetryParams.getDefaultInstance());

        private final String bucketName;

        public DomainLoader() {
            AppIdentityService appIdentityService = AppIdentityServiceFactory.getAppIdentityService();
            bucketName = appIdentityService.getDefaultGcsBucketName();
        }
        
        @Override
        public Domain load(String domainName) throws Exception {
                
            LOGGER.info("Loading domain theme for " + domainName);

            GcsFilename propertiesFile = domainResource(domainName, "domain.properties");

            if(exists(propertiesFile)) {

                Properties domainProperties = new Properties();
                domainProperties.load(new StringReader(toString(propertiesFile)));
         
                Domain domain = new Domain();
                domain.setHost(domainName);
                domain.setTitle(domainProperties.getProperty("title", "ActivityInfo"));
                domain.setSignUpAllowed(Boolean.valueOf(domainProperties.getProperty("signUpAllowed", "false")));
                domain.setScaffolding(toString(domainResource(domainName, "Scaffolding.ftl")));
                domain.setHomePageBody(toString(domainResource(domainName, "HomePageBody.html")));
                return domain;
                
            } else {
                LOGGER.info(propertiesFile + " does not exist, using default theme.");
                return defaultDomain();
            }
        }

        private GcsFilename domainPropertiesFilename(String domainName) {
            return domainResource(domainName, "domain.properties");
        }

        private String toString(GcsFilename filename) throws IOException {
            try(Reader reader = Channels.newReader(gcsService.openReadChannel(filename, 0), Charsets.UTF_8.name())) {
                return CharStreams.toString(reader);
            }
        }
        
        private boolean exists(GcsFilename filename) throws IOException {
            GcsFileMetadata metadata = gcsService.getMetadata(filename);
            return metadata != null;
        }

        private GcsFilename domainResource(String domainName, String resourceName) {
            return new GcsFilename(bucketName, String.format("domains/%s/%s", domainName, resourceName));
        }
    }

}
