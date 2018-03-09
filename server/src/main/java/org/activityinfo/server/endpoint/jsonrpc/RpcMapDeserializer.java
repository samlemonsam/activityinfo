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

import com.extjs.gxt.ui.client.data.RpcMap;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.deser.std.StdDeserializer;
import org.codehaus.jackson.node.ObjectNode;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

public class RpcMapDeserializer extends StdDeserializer<RpcMap> {

    public RpcMapDeserializer() {
        super(RpcMap.class);
    }

    @Override
    public RpcMap deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {

        ObjectMapper mapper = (ObjectMapper) jp.getCodec();
        ObjectNode root = (ObjectNode) mapper.readTree(jp);

        RpcMap map = new RpcMap();
        Iterator<Map.Entry<String, JsonNode>> fieldIt = root.getFields();
        while (fieldIt.hasNext()) {
            Map.Entry<String, JsonNode> field = fieldIt.next();
            if (field.getValue().isNumber()) {
                map.put(field.getKey(), field.getValue().getNumberValue());
            } else if (field.getValue().isBoolean()) {
                map.put(field.getKey(), field.getValue().asBoolean());
            } else if (field.getValue().isTextual()) {
                map.put(field.getKey(), field.getValue().asText());
            }
        }
        return map;
    }


}
