/*
 * ActivityInfo
 * Copyright (C) 2009-2013 UNICEF
 * Copyright (C) 2014-2018 BeDataDriven Groep B.V.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.activityinfo.server.endpoint.jsonrpc;

import com.google.api.client.repackaged.com.google.common.base.Strings;
import org.activityinfo.legacy.shared.command.Command;
import org.activityinfo.legacy.shared.command.GetSchema;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.deser.std.StdDeserializer;
import org.codehaus.jackson.map.exc.UnrecognizedPropertyException;
import org.codehaus.jackson.node.ObjectNode;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CommandDeserializer extends StdDeserializer<Command> {

    private static final Logger LOGGER = Logger.getLogger(CommandDeserializer.class.getName());
    
    public CommandDeserializer() {
        super(Command.class);
    }

    @Override
    public Command deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        ObjectMapper mapper = (ObjectMapper) jp.getCodec();
        ObjectNode root = (ObjectNode) mapper.readTree(jp);

        String typeName = root.path("type").asText();
        if(Strings.isNullOrEmpty(typeName)) {
            throw new BadRpcRequest("Expected 'type' property on root object. You must specify a command type.");
        }
        Class commandClass;
        try {
            commandClass = lookupCommandClass(typeName);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to find command class for " + typeName, e);
            throw new BadRpcRequest("Invalid command type '%s'", typeName);
        }
        JsonNode command = root.path("command");
        if(!command.isObject()) {
            throw new BadRpcRequest("Expected 'command' root object property.");
        }
        try {
            return (Command) mapper.readValue(command, commandClass);
        } catch(UnrecognizedPropertyException e) {
            throw new BadRpcRequest("Unexpected property '%s'", formatPath(e.getPath()));
        }
    }

    private String formatPath(List<JsonMappingException.Reference> path) {
        StringBuilder s = new StringBuilder();
        for (JsonMappingException.Reference reference : path) {
            if(reference.getFieldName() != null) {
                if(s.length() > 0) {
                    s.append(".");
                }
                s.append(reference.getFieldName());
            } else if(reference.getIndex() != -1) {
                s.append("[").append(reference.getIndex()).append("]");
            } 
        }
        return s.toString();
        
    }

    protected Class<?> lookupCommandClass(String type) {
        try {
            return Class.forName(GetSchema.class.getPackage().getName() + "." + type);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Couldn't resolve command type " + type);
        }
    }
}  