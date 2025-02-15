
package org.navitrace.api.resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.navitrace.BaseProtocol;
import org.navitrace.ServerManager;
import org.navitrace.api.ExtendedObjectResource;
import org.navitrace.database.CommandsManager;
import org.navitrace.helper.LogAction;
import org.navitrace.helper.model.DeviceUtil;
import org.navitrace.model.Command;
import org.navitrace.model.Device;
import org.navitrace.model.Group;
import org.navitrace.model.Position;
import org.navitrace.model.QueuedCommand;
import org.navitrace.model.Typed;
import org.navitrace.model.User;
import org.navitrace.model.UserRestrictions;
import org.navitrace.storage.StorageException;
import org.navitrace.storage.query.Columns;
import org.navitrace.storage.query.Condition;
import org.navitrace.storage.query.Request;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Path("commands")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CommandResource extends ExtendedObjectResource<Command> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommandResource.class);

    @Inject
    private CommandsManager commandsManager;

    @Inject
    private ServerManager serverManager;

    public CommandResource() {
        super(Command.class);
    }

    private BaseProtocol getDeviceProtocol(long deviceId) throws StorageException {
        Position position = storage.getObject(Position.class, new Request(
                new Columns.All(), new Condition.LatestPositions(deviceId)));
        if (position != null) {
            return serverManager.getProtocol(position.getProtocol());
        } else {
            return null;
        }
    }

    @GET
    @Path("send")
    public Collection<Command> get(@QueryParam("deviceId") long deviceId) throws StorageException {
        permissionsService.checkPermission(Device.class, getUserId(), deviceId);
        BaseProtocol protocol = getDeviceProtocol(deviceId);

        var commands = storage.getObjects(baseClass, new Request(
                new Columns.All(),
                Condition.merge(List.of(
                        new Condition.Permission(User.class, getUserId(), baseClass),
                        new Condition.Permission(Device.class, deviceId, baseClass)
                ))));

        return commands.stream().filter(command -> {
            String type = command.getType();
            if (protocol != null) {
                return command.getTextChannel() && protocol.getSupportedTextCommands().contains(type)
                        || !command.getTextChannel() && protocol.getSupportedDataCommands().contains(type);
            } else {
                return type.equals(Command.TYPE_CUSTOM);
            }
        }).collect(Collectors.toList());
    }

    @POST
    @Path("send")
    public Response send(Command entity, @QueryParam("groupId") long groupId) throws Exception {
        if (entity.getId() > 0) {
            permissionsService.checkPermission(baseClass, getUserId(), entity.getId());
            long deviceId = entity.getDeviceId();
            entity = storage.getObject(baseClass, new Request(
                    new Columns.All(), new Condition.Equals("id", entity.getId())));
            entity.setDeviceId(deviceId);
        } else {
            permissionsService.checkRestriction(getUserId(), UserRestrictions::getLimitCommands);
        }

        if (groupId > 0) {
            permissionsService.checkPermission(Group.class, getUserId(), groupId);
            var devices = DeviceUtil.getAccessibleDevices(storage, getUserId(), List.of(), List.of(groupId));
            List<QueuedCommand> queuedCommands = new ArrayList<>();
            for (Device device : devices) {
                Command command = QueuedCommand.fromCommand(entity).toCommand();
                command.setDeviceId(device.getId());
                QueuedCommand queuedCommand = commandsManager.sendCommand(command);
                if (queuedCommand != null) {
                    queuedCommands.add(queuedCommand);
                }
            }
            if (!queuedCommands.isEmpty()) {
                return Response.accepted(queuedCommands).build();
            }
        } else {
            permissionsService.checkPermission(Device.class, getUserId(), entity.getDeviceId());
            QueuedCommand queuedCommand = commandsManager.sendCommand(entity);
            if (queuedCommand != null) {
                return Response.accepted(queuedCommand).build();
            }
        }

        LogAction.command(getUserId(), groupId, entity.getDeviceId(), entity.getType());
        return Response.ok(entity).build();
    }

    @GET
    @Path("types")
    public Collection<Typed> get(
            @QueryParam("deviceId") long deviceId,
            @QueryParam("textChannel") boolean textChannel) throws StorageException {
        if (deviceId != 0) {
            permissionsService.checkPermission(Device.class, getUserId(), deviceId);
            BaseProtocol protocol = getDeviceProtocol(deviceId);
            if (protocol != null) {
                if (textChannel) {
                    return protocol.getSupportedTextCommands().stream().map(Typed::new).collect(Collectors.toList());
                } else {
                    return protocol.getSupportedDataCommands().stream().map(Typed::new).collect(Collectors.toList());
                }
            } else {
                return Collections.singletonList(new Typed(Command.TYPE_CUSTOM));
            }
        } else {
            List<Typed> result = new ArrayList<>();
            Field[] fields = Command.class.getDeclaredFields();
            for (Field field : fields) {
                if (Modifier.isStatic(field.getModifiers()) && field.getName().startsWith("TYPE_")) {
                    try {
                        result.add(new Typed(field.get(null).toString()));
                    } catch (IllegalArgumentException | IllegalAccessException error) {
                        LOGGER.warn("Get command types error", error);
                    }
                }
            }
            return result;
        }
    }

}
