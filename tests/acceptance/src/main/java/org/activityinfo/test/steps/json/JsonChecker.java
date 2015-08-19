package org.activityinfo.test.steps.json;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.NullNode;
import org.codehaus.jackson.node.ObjectNode;
import org.codehaus.jackson.node.TextNode;

import java.util.Iterator;

/**
 * Determines whether two json objects are equivalent
 */
class JsonChecker {

    private Placeholders placeholders;
    boolean ignorePositionInArray = false;

    public JsonChecker(Placeholders placeholders) {
        this(placeholders, false);
    }

    public JsonChecker(Placeholders placeholders, boolean ignorePositionInArray) {
        this.placeholders = placeholders;
        this.ignorePositionInArray = ignorePositionInArray;
    }

    private boolean isPlaceholder(Object value) {
        if(value instanceof String) {
            String stringValue = (String) value;
            return stringValue.startsWith("$");
        }
        return false;
    }

    public void check(String path, JsonNode expected, JsonNode actual) {
        if(expected.asText().startsWith(Placeholders.ID_PREFIX)) {
            try {
                bindPlaceholder(expected, actual);
            } catch (RuntimeException e) {
                // in case we can't bind id, check whether actual value equals to placeholder alias
                if (actual.getTextValue().equals(placeholders.resolveName(expected))) {
                    return;
                }
                throw e;
            }

        } else if (expected.isTextual()) {
            if ("null".equals(expected.asText()) && actual instanceof NullNode) {
                return;
            }
            if (!actual.isTextual()) {
                throw new AssertionError(String.format("Expected an text at %s; found: %s", path, actual));
            }
            checkString(path, expected.asText(), actual.asText());

        } else if (expected.isNumber()) {
            if(!actual.isNumber()) {
                throw new AssertionError(String.format("Expected a number at %s; found: %s", path, actual));
            }
            checkNumber(path, expected.asDouble(), actual.asDouble());
            
        } else if (expected instanceof ObjectNode) {
            if(!actual.isObject()) {
                throw new AssertionError(String.format("Expected an object at %s; found: %s", path, actual));
            }
            checkObject(path, (ObjectNode) expected, (ObjectNode) actual);

        } else if(expected instanceof ArrayNode) {
            if (!actual.isArray()) {
                throw new AssertionError(String.format("Expected an array at %s; found: %s", path, actual));
            }
            checkArray(path, (ArrayNode) expected, (ArrayNode) actual);

        } else {
            if(!expected.equals(actual)) {
                throw mismatchException(path, expected, actual);
            }
        }
    }

    private void checkNumber(String path, double expected, double actual) {
        double diff = Math.abs(expected - actual);
        if(diff > 0.0001) {
            throw new AssertionError(String.format("At %s, expected:\n%f\nFound:\n%f",
                    path, expected, actual));
        }
    }

    private void checkString(String path, String expected, String actual) {
        String actualString = placeholders.deAliasText(actual);
        if(!expected.equals(actualString)) {
            if (path.endsWith(".key")) { // key now contains something weird like db$id -> skip it for now
                return;
            }
            throw new AssertionError(String.format("At %s, expected:\n%s\nFound:\n%s",
                    path, expected, actualString));
        }
    }

    public void check(JsonNode expected, JsonNode actual) {
        check("", expected, actual);
    }


    private AssertionError mismatchException(String path, JsonNode expected, JsonNode actual) {
        return new AssertionError(String.format("At %s, expected:\n%s\nFound:\n%s",
                path, expected, actual));
    }

    private void bindPlaceholder(JsonNode expected, JsonNode actual) {
        String alias = placeholders.parseName(expected.asText());
        int id = actual.getIntValue();
        try {
            placeholders.bind(alias, id);
        } catch (IllegalStateException e) {
            // ignore if alia was already bound -> the same alias and id may occur multiple times within yaml
        }
    }

    private void checkObject(String path, ObjectNode expectedObject, ObjectNode actualObject) {
        Iterator<String> it = expectedObject.getFieldNames();
        while (it.hasNext()) {
            String fieldName = it.next();
            JsonNode expectedValue = expectedObject.get(fieldName);
            JsonNode actualValue = actualObject.get(fieldName);
            if (actualValue == null && fieldName.startsWith(Placeholders.ID_PREFIX)) { // cover case when field name may be placeholder
                Iterator<String> actualFieldNames = actualObject.getFieldNames();
                while(actualFieldNames.hasNext()) {
                    String current = actualFieldNames.next();
                    if (current.startsWith(placeholders.parseName(fieldName) + "_")) {
                        fieldName = current;
                        break;
                    }
                }
                actualValue = actualObject.get(fieldName);
            }
            if (actualValue == null) {
                throw new AssertionError(String.format("Missing field '%s'", fieldName));
            }
            check(String.format("%s.%s", path, fieldName), expectedValue, actualValue);
        }

        it = actualObject.getFieldNames();
        while(it.hasNext()) {
            String fieldName = it.next();
            if(expectedObject.get(fieldName) == null) {
                // check, maybe we have placeholder in expected object
                if (fieldName.contains("_")) {
                    String placeholder = Placeholders.ID_PREFIX + fieldName.substring(0, fieldName.indexOf("_"));
                    if (expectedObject.get(placeholder) != null) {
                        return;
                    }
                }
                throw new AssertionError(String.format("Unexpected field '%s' in response", fieldName));
            }
        }
    }


    private void checkArray(String path, ArrayNode expectedArray, ArrayNode actualArray) {
        int expectedArraySize = expectedArray.size();
        if (expectedArraySize != actualArray.size()) {
            throw new AssertionError(String.format("At %s, Expected an array with %d elements, found %d.",
                    path, expectedArraySize, actualArray.size()));
        }

        if (ignorePositionInArray) {
            for (int i = 0; i < expectedArraySize; ++i) {
                JsonNode expectedValue = expectedArray.get(i);

                boolean hasMatch = false;
                Error firstException = null;
                for (int j = 0; j < actualArray.size(); j++) {
                    JsonNode actualValue = actualArray.get(j);

                    try {
                        check(String.format("%s[%d]", path, j), expectedValue, actualValue);
                        hasMatch = true;
                        break;
                    } catch (Throwable e) {
                        if (firstException == null && e instanceof Error) {
                            firstException = (Error) e;
                        }
                    }
                }

                if (!hasMatch) {
                    throw firstException;
                }

            }
        } else {
            for (int i = 0; i < expectedArraySize; ++i) {
                JsonNode expectedValue = expectedArray.get(i);
                JsonNode actualValue = actualArray.get(i);
                check(String.format("%s[%d]", path, i), expectedValue, actualValue);
            }
        }
    }


    private boolean isPlaceholder(JsonNode node) {
        if(!(node instanceof TextNode)) {
            return false;
        }
        return isPlaceholder(node.getTextValue());
    }


}
