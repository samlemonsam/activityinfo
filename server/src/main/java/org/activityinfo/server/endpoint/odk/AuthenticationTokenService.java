package org.activityinfo.server.endpoint.odk;

import com.google.inject.ImplementedBy;
import org.activityinfo.model.auth.AuthenticatedUser;
import org.activityinfo.model.resource.ResourceId;

@ImplementedBy(AuthenticationTokenServiceImpl.class)
public interface AuthenticationTokenService {

    String createAuthenticationToken(int userId, ResourceId formClassId);

    AuthenticatedUser authenticate(String authenticationToken);

}
