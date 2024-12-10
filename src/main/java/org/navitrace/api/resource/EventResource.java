
package org.navitrace.api.resource;

import org.navitrace.api.BaseResource;
import org.navitrace.model.Device;
import org.navitrace.model.Event;
import org.navitrace.storage.StorageException;
import org.navitrace.storage.query.Columns;
import org.navitrace.storage.query.Condition;
import org.navitrace.storage.query.Request;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("events")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class EventResource extends BaseResource {

    @Path("{id}")
    @GET
    public Event get(@PathParam("id") long id) throws StorageException {
        Event event = storage.getObject(Event.class, new Request(
                new Columns.All(), new Condition.Equals("id", id)));
        if (event == null) {
            throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).build());
        }
        permissionsService.checkPermission(Device.class, getUserId(), event.getDeviceId());
        return event;
    }

}
