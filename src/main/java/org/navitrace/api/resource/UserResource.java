
package org.navitrace.api.resource;

import com.warrenstrange.googleauth.GoogleAuthenticator;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Context;
import org.navitrace.api.BaseObjectResource;
import org.navitrace.config.Config;
import org.navitrace.config.Keys;
import org.navitrace.helper.LogAction;
import org.navitrace.helper.model.UserUtil;
import org.navitrace.model.Device;
import org.navitrace.model.ManagedUser;
import org.navitrace.model.Permission;
import org.navitrace.model.User;
import org.navitrace.storage.StorageException;
import org.navitrace.storage.query.Columns;
import org.navitrace.storage.query.Condition;
import org.navitrace.storage.query.Request;

import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.Collection;

@Path("users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserResource extends BaseObjectResource<User> {

    @Inject
    private Config config;

    @Context
    private HttpServletRequest request;

    public UserResource() {
        super(User.class);
    }

    @GET
    public Collection<User> get(
            @QueryParam("userId") long userId, @QueryParam("deviceId") long deviceId) throws StorageException {
        if (userId > 0) {
            permissionsService.checkUser(getUserId(), userId);
            return storage.getObjects(baseClass, new Request(
                    new Columns.All(),
                    new Condition.Permission(User.class, userId, ManagedUser.class).excludeGroups()));
        } else if (deviceId > 0) {
            permissionsService.checkAdmin(getUserId());
            return storage.getObjects(baseClass, new Request(
                    new Columns.All(),
                    new Condition.Permission(User.class, Device.class, deviceId).excludeGroups()));
        } else if (permissionsService.notAdmin(getUserId())) {
            return storage.getObjects(baseClass, new Request(
                    new Columns.All(),
                    new Condition.Permission(User.class, getUserId(), ManagedUser.class).excludeGroups()));
        } else {
            return storage.getObjects(baseClass, new Request(new Columns.All()));
        }
    }

    @Override
    @PermitAll
    @POST
    public Response add(User entity) throws StorageException {
        User currentUser = getUserId() > 0 ? permissionsService.getUser(getUserId()) : null;
        if (currentUser == null || !currentUser.getAdministrator()) {
            permissionsService.checkUserUpdate(getUserId(), new User(), entity);
            if (currentUser != null && currentUser.getUserLimit() != 0) {
                int userLimit = currentUser.getUserLimit();
                if (userLimit > 0) {
                    int userCount = storage.getObjects(baseClass, new Request(
                            new Columns.All(),
                            new Condition.Permission(User.class, getUserId(), ManagedUser.class).excludeGroups()))
                            .size();
                    if (userCount >= userLimit) {
                        throw new SecurityException("Manager user limit reached");
                    }
                }
            } else {
                if (UserUtil.isEmpty(storage)) {
                    entity.setAdministrator(true);
                } else if (!permissionsService.getServer().getRegistration()) {
                    throw new SecurityException("Registration disabled");
                }
                if (permissionsService.getServer().getBoolean(Keys.WEB_TOTP_FORCE.getKey())
                        && entity.getTotpKey() == null) {
                    throw new SecurityException("One-time password key is required");
                }
                UserUtil.setUserDefaults(entity, config);
            }
        }

        entity.setId(storage.addObject(entity, new Request(new Columns.Exclude("id"))));
        storage.updateObject(entity, new Request(
                new Columns.Include("hashedPassword", "salt"),
                new Condition.Equals("id", entity.getId())));

        LogAction.create(getUserId(), entity);

        if (currentUser != null && currentUser.getUserLimit() != 0) {
            storage.addPermission(new Permission(User.class, getUserId(), ManagedUser.class, entity.getId()));
            LogAction.link(getUserId(), User.class, getUserId(), ManagedUser.class, entity.getId());
        }
        return Response.ok(entity).build();
    }

    @Path("{id}")
    @DELETE
    public Response remove(@PathParam("id") long id) throws Exception {
        Response response = super.remove(id);
        if (getUserId() == id) {
            request.getSession().removeAttribute(SessionResource.USER_ID_KEY);
        }
        return response;
    }

    @Path("totp")
    @PermitAll
    @POST
    public String generateTotpKey() throws StorageException {
        if (!permissionsService.getServer().getBoolean(Keys.WEB_TOTP_ENABLE.getKey())) {
            throw new SecurityException("One-time password is disabled");
        }
        return new GoogleAuthenticator().createCredentials().getKey();
    }

}
