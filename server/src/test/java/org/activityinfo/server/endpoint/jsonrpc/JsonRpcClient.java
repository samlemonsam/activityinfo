package org.activityinfo.server.endpoint.jsonrpc;
/*
 * #%L
 * ActivityInfo Server
 * %%
 * Copyright (C) 2009 - 2013 UNICEF
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import com.extjs.gxt.ui.client.data.RpcMap;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import org.activityinfo.legacy.shared.command.Command;
import org.codehaus.jackson.Version;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.module.SimpleModule;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.net.URI;

/**
 * @author yuriyz on 6/24/14.
 */
public class JsonRpcClient {

    private final String endpoint;
    private final String username;
    private final String password;

    private Client client;
    private URI uri;
    private ObjectMapper objectMapper;

    public JsonRpcClient(String endpoint, String username, String password) {
        this.endpoint = endpoint;
        this.username = username;
        this.password = password;

        SimpleModule module = new SimpleModule("Command", new Version(1, 0, 0, null));
        module.addDeserializer(Command.class, new CommandDeserializer());
        module.addDeserializer(RpcMap.class, new RpcMapDeserializer());

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(module);
        objectMapper.disable(SerializationConfig.Feature.FAIL_ON_EMPTY_BEANS);

        ClientConfig clientConfig = new DefaultClientConfig();
//        clientConfig.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.TRUE);
//        clientConfig.getClasses().add(ObjectMapperProvider.class);

        client = Client.create(clientConfig);
        client.addFilter(new HTTPBasicAuthFilter(username, password));

        uri = UriBuilder.fromUri(endpoint).build();
    }

    public String execute(Command command) throws IOException {
        String entity = objectMapper.writeValueAsString(new CommandRequest(command));
        System.out.println(entity);
        return client.resource(uri).entity(entity, MediaType.APPLICATION_JSON_TYPE).post(String.class);
    }
}
