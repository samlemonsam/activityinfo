package org.activityinfo.server.endpoint.odk;

import com.google.appengine.api.datastore.*;
import com.google.inject.Inject;
import org.activityinfo.model.auth.AuthenticatedUser;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.server.authentication.ServerSideAuthProvider;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.security.SecureRandom;

public class AuthenticationTokenServiceImpl implements AuthenticationTokenService {

    public static final String KIND = "XFormAuthToken";
    private DatastoreService datastoreService = DatastoreServiceFactory.getDatastoreService();
    private ServerSideAuthProvider authProvider;

    @Inject
    public AuthenticationTokenServiceImpl(ServerSideAuthProvider authProvider) {
        this.authProvider = authProvider;
    }

    @Override
    public String createAuthenticationToken(int userId, ResourceId formClassId) {
        String token = "token:" + Long.toHexString(new SecureRandom().nextLong());
        Entity entity = new Entity(key(token));
        entity.setUnindexedProperty("userId", userId);
        entity.setUnindexedProperty("formClassId", formClassId.asString());
        entity.setUnindexedProperty("creationTime", System.currentTimeMillis());
        datastoreService.put(null, entity);

        return token;
    }

    private Key key(String token) {
        return KeyFactory.createKey(KIND, token);
    }

    @Override
    public AuthenticatedUser authenticate(String authenticationToken) {
        Entity authEntity = null;
        try {
            authEntity = datastoreService.get(key(authenticationToken));
        } catch (EntityNotFoundException e) {
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        }
        long userId = (Long) authEntity.getProperty("userId");
        AuthenticatedUser user = new AuthenticatedUser("", (int) userId, "@");
        authProvider.set(user);

        return user;
    }
}
