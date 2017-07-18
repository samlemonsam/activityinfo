package org.activityinfo.server.util.blob;

import com.google.appengine.api.appidentity.AppIdentityService;
import com.google.appengine.api.appidentity.PublicCertificate;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import org.activityinfo.server.DeploymentConfiguration;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.AccessControlException;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Signature;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DevAppIdentityService implements AppIdentityService {

    private static final Logger LOGGER = Logger.getLogger(DevAppIdentityService.class.getName());

    private String serviceAccountName;
    private PrivateKey privateKey = null;

    public DevAppIdentityService(DeploymentConfiguration config) {
        try {
            loadPrivateKey(config);
        } catch(Exception e) {
            LOGGER.log(Level.WARNING, "Could not load primary key", e);
        }
        Preconditions.checkNotNull(privateKey);
    }

    private void loadPrivateKey(DeploymentConfiguration config) throws Exception {
        serviceAccountName = config.getProperty("service.account.name");
        String keyPath = config.getProperty("service.account.p12.key.path");
        String password = config.getProperty("service.account.p12.key.password");

        if (Strings.isNullOrEmpty(serviceAccountName) ||
                Strings.isNullOrEmpty(keyPath) ||
                Strings.isNullOrEmpty(password)) {

            LOGGER.warning("Service account is not configured for blob store testing, add the following to your " +
                    "~/activityinfo.properties file:\n" +
                    "service.account.name=135288259907-k64g5vuv9en1o89on1ru16hrusvimn9t@developer" +
                    ".gserviceaccount.com\n" +
                    "service.account.p12.key.path=/home/alex/.ssh/bdd-dev-0bcc29c72426.p12\n" +
                    "service.account.p12.key.password=notasecret");

        } else {

            InputStream in = readFromFile(keyPath);

            // trick, if for test we don't want to add FilePermission exlusion to java.policy to read from file we can
            // load private key from classpath
            if (in == null) {
                in = DevAppIdentityService.class.getResourceAsStream(config.getProperty("service.account.p12.classpath.fileName"));
            }

            if (in == null) {
                throw new IOException("Keystore not found at " + keyPath + ".");
            }

            KeyStore keystore = KeyStore.getInstance("PKCS12");
            try {
                keystore.load(in, password.toCharArray());
            } finally {
                IOUtils.closeQuietly(in);
            }
            privateKey = (PrivateKey)keystore.getKey("privatekey", password.toCharArray());
        }
    }

    private InputStream readFromFile(String keyPath) throws IOException {
        try {
            File keyFile = new File(keyPath);
            if (keyFile.exists()) {
                return new FileInputStream(keyFile);
            }
        } catch (AccessControlException e) { // if java has no access to specified directory
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
        return null;
    }

    @Override
    public SigningResult signForApp(byte[] bytes) {
        if (privateKey == null) {
            throw new UnsupportedOperationException("Service account not configured for local development.");
        }
        try {
            Signature dsa = Signature.getInstance("SHA256withRSA");
            dsa.initSign(privateKey);
            dsa.update(bytes);
            return new SigningResult(serviceAccountName, dsa.sign());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Collection<PublicCertificate> getPublicCertificatesForApp() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getServiceAccountName() {
        return serviceAccountName;
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
