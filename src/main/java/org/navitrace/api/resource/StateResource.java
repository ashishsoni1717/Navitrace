package org.navitrace.api.resource;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.navitrace.api.BaseResource;
import org.navitrace.model.State;
import org.navitrace.storage.StorageException;
import org.navitrace.storage.query.Columns;
import org.navitrace.storage.query.Condition;
import org.navitrace.storage.query.Order;
import org.navitrace.storage.query.Request;

import java.util.Collection;

@Path("states")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class StateResource extends BaseResource {

    @Path("{id}")
    @GET
    public Response getSingle(@PathParam("id") long id) throws StorageException {
        State entity = storage.getObject(State.class, new Request(
                new Columns.All(), new Condition.Equals("id", id)));
        if (entity != null) {
            return Response.ok(entity).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @GET
    public Response getAll(@QueryParam("pageNo") int pageNo,
                           @QueryParam("pageSize") int pageSize,
                           @QueryParam("sortField") String sortField,
                           @QueryParam("desc") boolean desc) throws StorageException {
        Collection<State> states = storage.getObjects(State.class,
                new Request(new Columns.All(), new Order(sortField, desc, pageNo, pageSize)));
        return Response.ok(states).build();
    }

    @POST
    public Response add(State entity) throws Exception {
        entity.setId(storage.addObject(entity, new Request(new Columns.Exclude("id"))));
        return Response.ok(entity).build();
    }

    @Path("{id}")
    @PUT
    public Response update(State entity) throws Exception {
        State state = storage.getObject(State.class, new Request(
                    new Columns.All(), new Condition.Equals("id", entity.getId())));
        return Response.ok(entity).build();
    }

    @Path("{id}")
    @DELETE
    public Response remove(@PathParam("id") long id) throws Exception {
        storage.removeObject(State.class, new Request(new Condition.Equals("id", id)));
        return Response.noContent().build();
    }
}