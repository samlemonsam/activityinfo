package org.activityinfo.service.blob;

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
    public static final String SERVICE_ACCOUNT_EMAIL = "135288259907-k64g5vuv9en1o89on1ru16hrusvimn9t@developer.gserviceaccount.com";
    public static final String PASSWORD = "notasecret";

    private PrivateKey privateKey;

    public TestingIdentityService() throws Exception {
        privateKey = loadPrivateKey();
    }

    private PrivateKey loadPrivateKey() throws Exception {
        File keyFile = new File("C:\\Users\\Jorden\\BeDataDriven Development-7ca1136cac21.p12");
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
