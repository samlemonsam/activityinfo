package org.activityinfo.server.endpoint.rest;

import com.google.api.client.repackaged.com.google.common.base.Strings;
import org.activityinfo.server.DeploymentEnvironment;
import org.activityinfo.server.database.hibernate.entity.User;
import org.activityinfo.server.util.config.DeploymentConfiguration;

import javax.inject.Provider;
import javax.persistence.EntityManager;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Date;

/**
 * Allows creation of user accounts through the REST API
 *
 * <p>Currently only supported for debugging purposes, but could also be used to
 * sync external directory systems.</p>
 */
public class UsersResource {

    public static final String USER_API_ENABLED = "user.api.enabled";

    private DeploymentConfiguration deploymentConfiguration;
    private Provider<EntityManager> entityManager;

    public UsersResource(DeploymentConfiguration deploymentConfiguration, Provider<EntityManager> entityManager) {
        this.deploymentConfiguration = deploymentConfiguration;
        this.entityManager = entityManager;

    }

    private boolean isApiEnabled() {
        return DeploymentEnvironment.isAppEngineDevelopment() ||
                "true".equals(deploymentConfiguration.getProperty(USER_API_ENABLED));
    }

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response createUser(
            @FormParam("email") String email, 
            @FormParam("name") String name, 
            @FormParam("password") String password,
            @FormParam("locale") String locale) {

        if(!isApiEnabled()) {
            return Response.status(Response.Status.SERVICE_UNAVAILABLE).entity("User API is disabled.").build();
        }

        if(Strings.isNullOrEmpty(email) ||
            Strings.isNullOrEmpty(name) ||
            Strings.isNullOrEmpty(password)) {
           
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("The email, name, and password properties are required.")
                    .build();
        }

        User user = new User();
        user.setDateCreated(new Date());
        user.setName(name);
        user.setEmail(email);
        user.setEmailNotification(false);
        user.changePassword(password);
        
        if(Strings.isNullOrEmpty(locale)) {
            user.setLocale("en");
        } else if("en".equals(locale) || "fr".equals(locale)) {
            user.setLocale(locale);
        } else {
            return Response.status(Response.Status.BAD_REQUEST).entity("Invalid locale").build();
        }
        
        entityManager.get().getTransaction().begin();
        entityManager.get().persist(user);
        entityManager.get().getTransaction().commit();
        
        return Response.status(Response.Status.CREATED).build();
    }
}
