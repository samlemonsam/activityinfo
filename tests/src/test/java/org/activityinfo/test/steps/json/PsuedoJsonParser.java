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
package org.activityinfo.test.steps.json;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.JsonNodeFactory;
import org.codehaus.jackson.node.ObjectNode;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.nodes.*;

import java.io.StringReader;

/**
 * Parses a YAML document into a JsonNode, replacing placeholders
 * for ids and aliases.
 * 
 * <p>The API acceptance tests are meant to ensure that we do not break external applications
 * that interact with AI through the public API. In this case, the precise format of the JSON
 * emitted or expected is important, and so we want to actually validate the representation itself
 * in addition to the business logic.</p>
 * 
 * <p>However, JSON is a bit nasty to include in human readable text files, so purely in the 
 * interests of legibility (or perhaps aesthetics), we parse the blocks as YAML rather than pure
 * JSON. This class merely turns the YAML into a Jackson JSON tree for convenience.</p>
 * 
 * 
 */
class PsuedoJsonParser {

    private final Yaml yaml = new Yaml();
    private final JsonNodeFactory json;

    public PsuedoJsonParser(ObjectMapper objectMapper) {
        this.json = objectMapper.getNodeFactory();
    }

    /**
     * Parses a request body written in YAML, replacing <code>@Placeholders</code> with
     * ids from the alias table.
     * 
     * @param requestBody The request body, written in YAML syntax
     * @return a Jackson JSON tree object.
     */
    public JsonNode parse(String requestBody) {
        Node node = yaml.compose(new StringReader(requestBody));
        return toJson(node);
    }

    private JsonNode toJson(Node node) {
        if(node instanceof MappingNode) {
            return toObjectNode((MappingNode) node);
        }

        if(node instanceof SequenceNode) {
            return toArrayNode((SequenceNode) node);
        }

        if(node instanceof ScalarNode) {
            return toScalarNode((ScalarNode) node);
        }

        throw new IllegalArgumentException("Node: " + node);
    }


    private ObjectNode toObjectNode(MappingNode node) {
        ObjectNode objectNode = json.objectNode();
        for (NodeTuple tuple : node.getValue()) {
            objectNode.put(stringKey(tuple), toJson(tuple.getValueNode()));
        }
        return objectNode;
    }

    private String stringKey(NodeTuple tuple) {
        if(!(tuple.getKeyNode() instanceof ScalarNode)) {
            throw new UnsupportedOperationException("Json keys must be strings");
        }
        return ((ScalarNode) tuple.getKeyNode()).getValue();
    }


    private ArrayNode toArrayNode(SequenceNode node) {
        ArrayNode arrayNode = json.arrayNode();
        for (Node element : node.getValue()) {
            arrayNode.add(toJson(element));
        }
        return arrayNode;
    }


    private JsonNode toScalarNode(ScalarNode node) {
        String value = node.getValue();

        if (!value.contains(".")) { // avoid type mistmatch
            try {
                return json.numberNode(Integer.parseInt(value));
            } catch (NumberFormatException ignore) {
            }
        }

        try {
            Double doubleValue = Double.parseDouble(value);
            return json.numberNode(doubleValue);
        } catch (NumberFormatException ignore) {
        } 

        if(value.equals("true")) {
            return json.booleanNode(true);
        }
        if(value.equals("false")) {
            return json.booleanNode(false);
        }
        
        return json.textNode(value);
    }


}
