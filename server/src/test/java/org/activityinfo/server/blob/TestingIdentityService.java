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
package org.activityinfo.server.blob;

import com.google.appengine.api.appidentity.AppIdentityService;
import com.google.appengine.api.appidentity.PublicCertificate;
import org.junit.internal.AssumptionViolatedException;

import java.io.File;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Signature;
import java.util.Collection;

public class TestingIdentityService implements AppIdentityService {

    private static final String SERVICE_ACCOUNT_EMAIL = "135288259907-k64g5vuv9en1o89on1ru16hrusvimn9t@developer.gserviceaccount.com";
    private static final String PASSWORD = "notasecret";

    private PrivateKey privateKey;

    public TestingIdentityService(String privateKeyFilePath) throws Exception {
        privateKey = loadPrivateKey(privateKeyFilePath);
    }

    private PrivateKey loadPrivateKey(String privateKeyFilePath) throws Exception {
        File keyFile = new File(privateKeyFilePath);
        if (!keyFile.exists()) {
            throw new AssumptionViolatedException("Key file is not present");
        }

        KeyStore keystore = KeyStore.getInstance("PKCS12");
        try (FileInputStream in = new FileInputStream(keyFile)) {
            keystore.load(in, PASSWORD.toCharArray());
        }
        return (PrivateKey) keystore.getKey("privatekey", PASSWORD.toCharArray());
    }

    @Override
    public SigningResult signForApp(byte[] bytes) {
        try {
            Signature dsa = Signature.getInstance("SHA256withRSA");
            dsa.initSign(privateKey);
            dsa.update(bytes);
            return new SigningResult(SERVICE_ACCOUNT_EMAIL, dsa.sign());
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Collection<PublicCertificate> getPublicCertificatesForApp() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getServiceAccountName() {
        return SERVICE_ACCOUNT_EMAIL;
    }

    @Override
    public String getDefaultGcsBucketName() {
        throw new UnsupportedOperationException();
    }

    @Override
    public GetAccessTokenResult getAccessTokenUncached(Iterable<String> strings) {
        throw new UnsupportedOperationException();
    }

    @Override
    public GetAccessTokenResult getAccessToken(Iterable<String> strings) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ParsedAppId parseFullAppId(String s) {
        throw new UnsupportedOperationException();
    }
}
