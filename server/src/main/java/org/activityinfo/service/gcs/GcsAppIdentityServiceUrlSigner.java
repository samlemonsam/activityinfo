package org.activityinfo.service.gcs;
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
import com.google.common.io.BaseEncoding;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Calendar;

/**
 * @author yuriyz on 8/8/14.
 */
public class GcsAppIdentityServiceUrlSigner {

    private static final int EXPIRATION_TIME = 5;

    private static final String BASE_URL = "https://storage.googleapis.com";

    private final AppIdentityService identityService = AppIdentityServiceFactory.getAppIdentityService();

    public String getSignedUrl(final String httpVerb, final String path) throws Exception {
        final long expiration = expiration();
        final String unsigned = stringToSign(expiration, path, httpVerb);
        final String signature = sign(unsigned);
        return new StringBuilder(BASE_URL)
                .append("/")
                .append(path)
                .append("?GoogleAccessId=")
                .append(clientId())
                .append("&Expires=")
                .append(expiration)
                .append("&Signature=")
                .append(URLEncoder.encode(signature, "UTF-8")).toString();
    }

    private static long expiration() {
        final long unitMil = 1000l;
        final Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, EXPIRATION_TIME);
        return calendar.getTimeInMillis() / unitMil;
    }

    private String stringToSign(final long expiration, String path, String httpVerb) {
        final String contentType = "";
        final String contentMD5 = "";
        final String canonicalizedExtensionHeaders = "";
        final String canonicalizedResource = "/" + path;
        return httpVerb + "\n" + contentMD5 + "\n" + contentType + "\n"
                + expiration + "\n" + canonicalizedExtensionHeaders + canonicalizedResource;
    }

    protected String sign(final String stringToSign) throws UnsupportedEncodingException {
        final AppIdentityService.SigningResult signingResult = identityService
                .signForApp(stringToSign.getBytes());
        return BaseEncoding.base64().encode(signingResult.getSignature());
    }

    protected String clientId() {
        return identityService.getServiceAccountName();
    }
}
