package org.activityinfo.test.acceptance.json;

import com.google.common.base.Preconditions;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ObjectNode;
import org.codehaus.jackson.node.TextNode;
import org.yaml.snakeyaml.nodes.ScalarNode;

import java.util.Iterator;

/**
 * Determines whether two json objects are equivalent
 */
public class JsonChecker {


    private Placeholders placeholders;

    public JsonChecker(Placeholders placeholders) {
        this.placeholders = placeholders;
    }

    private boolean isPlaceholder(Object value) {
        if(value instanceof String) {
            String stringValue = (String) value;
            return stringValue.startsWith("$");
        }
        return false;
    }

    public void check(JsonNode expected, JsonNode actual) {
        if(isPlaceholder(expected)) {
            bindPlaceholder(expected, actual);
        } else {
            if (expected.asToken() != actual.asToken()) {
                throw new AssertionError(String.format("Expected:<n%s\nFound:\n%s",
                        expected.asToken(), actual));
            }

            if (expected instanceof ObjectNode) {
                checkObjects((ObjectNode) expected, (ObjectNode) actual);
            } else {
                throw new UnsupportedOperationException("Todo: " + expected.toString());
            }
        }
    }

    private void bindPlaceholder(JsonNode expected, JsonNode actual) {
        String alias = placeholders.parseName(expected.asText());
        int id = actual.getIntValue();
        placeholders.bind(alias, id);
        
    }

    private void checkObjects(ObjectNode expectedObject, ObjectNode actualObject) {
        Iterator<String> it = expectedObject.getFieldNames();
        while (it.hasNext()) {
            String fieldName = it.next();
            JsonNode expectedValue = expectedObject.get(fieldName);
            JsonNode actualValue = actualObject.get(fieldName);
            if(actualValue == null) {
                throw new AssertionError(String.format("Missing field '%s'", fieldName));
            }
            check(expectedValue, actualValue);
        }

        it = actualObject.getFieldNames();
        while(it.hasNext()) {
            String fieldName = it.next();
            if(expectedObject.get(fieldName) == null) {
                throw new AssertionError(String.format("Unexpected field '%s' in response", fieldName));
            }
        }
    }
    
    private boolean isPlaceholder(JsonNode node) {
        if(!(node instanceof TextNode)) {
            return false;
        }
        return isPlaceholder(node.getTextValue());
    }
    
//
//    private Node readYaml(String requestYaml) {
//        return yaml.compose(new StringReader(requestYaml));
//    }
//
//    private Map<String, Object> replacePlaceholders(Map<String, Object> object) {
//        Map<String, Object> output = Maps.newHashMap();
//        for (Map.Entry<String, Object> entry : object.entrySet()) {
//            output.put(entry.getKey(), replacePlaceholders(entry.getValue()));
//        }
//        return output;
//    }
//
//    private Object replacePlaceholders(Object value) {
//        if(isPlaceholder(value)) {
//            return aliasTable.getId(parseAlias(value));
//        } else if(value instanceof Map) {
//            return replacePlaceholders((Map<String, Object>) value);
//        } else if(value instanceof Collection) {
//            throw new UnsupportedOperationException();
//        } else {
//            return value;
//        }
//    }
//
//    private String replacePathPlaceholders(String path) {
//        String[] parts = path.split("/");
//        List<String> evaluatedParts = new ArrayList<>();
//        for(String part : parts) {
//            evaluatedParts.add(replacePlaceholders(part).toString());
//        }
//        return Joiner.on("/").join(evaluatedParts);
//    }

}
