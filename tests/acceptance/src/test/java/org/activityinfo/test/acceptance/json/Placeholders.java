package org.activityinfo.test.acceptance.json;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import org.activityinfo.test.driver.AliasTable;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.JsonNodeFactory;
import org.codehaus.jackson.node.ObjectNode;
import org.codehaus.jackson.node.TextNode;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;



public class Placeholders {

    public static final String ID_PREFIX = "$";
    public static final java.lang.String ALIAS_PREFIX = "~";

    private final AliasTable aliasTable;
    private final JsonNodeFactory factory;

    @Inject
    public Placeholders(AliasTable aliasTable) {
        this.aliasTable = aliasTable;
        this.factory = new ObjectMapper().getNodeFactory();
    }
    
    public JsonNode resolve(JsonNode node) {
        if(node instanceof ObjectNode) {
            return resolveObject((ObjectNode)node);
        
        } else if(node instanceof ArrayNode) {
            return resolveArray((ArrayNode) node);
        
        } else if(node instanceof TextNode) {
            return resolveText((TextNode)node);
            
        } else {
            return node;
        }
    }


    public String resolvePath(String path) {
        List<String> result = new ArrayList<>();
        for(String part : path.split("/")) {
            if(isPlaceholder(part)) {
                result.add(Integer.toString(resolveId(part)));
            } else {
                result.add(part);                
            }
        }
        return Joiner.on("/").join(result);
    }
    
    private ObjectNode resolveObject(ObjectNode node) {
        ObjectNode result = factory.objectNode();
        Iterator<Map.Entry<String, JsonNode>> it = node.getFields();
        while(it.hasNext()) {
            Map.Entry<String, JsonNode> entry = it.next();
            result.put(entry.getKey(), resolve(entry.getValue()));
        }
        return result;
    }
    
    private ArrayNode resolveArray(ArrayNode node) {
        ArrayNode result = factory.arrayNode();
        for (JsonNode aNode : node) {
            result.add(resolve(aNode));
        }
        return result;
    }

    private JsonNode resolveText(TextNode node) {
        String text = node.getTextValue();
        if(isPlaceholder(text)) {
            if (text.startsWith(ID_PREFIX)) {
                return factory.numberNode(resolveId(text));
            } else {
                return factory.textNode(aliasTable.getName(text));
            }
        } else {
            return node;
        }
    }
    
    public String aliasText(String text) {
        if(aliasTable.isName(text)) {
            return aliasTable.alias(text);
        } else {
            return text;
        }
    }

    private int resolveId(String placeholder) {
        return aliasTable.getId(parseName(placeholder));
    }

    public int resolveId(JsonNode node) {
        return resolveId(node.asText());
    }

    public boolean isPlaceholder(String textValue) {
        return textValue.startsWith(ID_PREFIX) || textValue.startsWith(ALIAS_PREFIX);
    }

    public String parseName(String placeholder) {
        Preconditions.checkArgument(isPlaceholder(placeholder));
        String name = placeholder.substring(1);
        if(name.startsWith("{") && name.endsWith("}")) {
            return name.substring(1, name.length()-1);
        } else {
            return name;
        }
    }

    public void bind(String alias, int id) {
        aliasTable.bindId(alias, id);
    }

    public String resolveName(JsonNode expected) {
        String alias = parseName(expected.asText());
        return aliasTable.getName(alias);
    }
}
