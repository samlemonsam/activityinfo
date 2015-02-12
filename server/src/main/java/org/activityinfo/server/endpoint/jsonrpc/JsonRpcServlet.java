package org.activityinfo.server.endpoint.jsonrpc;

import com.bedatadriven.rebar.time.calendar.LocalDate;
import com.extjs.gxt.ui.client.data.RpcMap;
import com.google.common.io.ByteStreams;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sun.jersey.server.impl.container.servlet.Include;
import org.activityinfo.legacy.shared.command.Command;
import org.activityinfo.legacy.shared.command.result.CommandResult;
import org.activityinfo.legacy.shared.exception.CommandException;
import org.activityinfo.server.command.DispatcherSync;
import org.codehaus.jackson.Version;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.module.SimpleModule;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

@Singleton
public class JsonRpcServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(JsonRpcServlet.class.getName());

    private final DispatcherSync dispatcher;
    private final ObjectMapper objectMapper;

    @Inject
    public JsonRpcServlet(DispatcherSync dispatcher) {
        this.dispatcher = dispatcher;

        SimpleModule module = new SimpleModule("Command", new Version(1, 0, 0, null));
        module.addDeserializer(Command.class, new CommandDeserializer());
        module.addDeserializer(RpcMap.class, new RpcMapDeserializer());
        module.addSerializer(LocalDate.class, new LocalDateSerializer());
        module.addDeserializer(LocalDate.class, new LocalDateDeserializer());


        objectMapper = new ObjectMapper();
        objectMapper.registerModule(module);

        // to ensure that VoidResult is handled without error
        objectMapper.disable(SerializationConfig.Feature.FAIL_ON_EMPTY_BEANS);
        
        // Don't write out 'null' properties
        objectMapper.setSerializationInclusion(JsonSerialize.Inclusion.NON_NULL);

    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        Command command;
        try {
            String json = new String(ByteStreams.toByteArray(req.getInputStream()));
            command = objectMapper.readValue(json, Command.class);
        } catch (BadRpcRequest e) {
            LOGGER.log(Level.SEVERE, "Failed to deserialize command", e);
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
            return;
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to deserialize command", e);
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        CommandResult result;
        try {
            result = dispatcher.execute(command);
        } catch (CommandException e) {
            LOGGER.log(Level.SEVERE, "Command exception", e);
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
            return;
            
        }
        try {
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.setContentType("application/json");
            objectMapper.writeValue(resp.getOutputStream(), result);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Command exception", e);
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        }
    }
}
