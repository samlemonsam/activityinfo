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
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.*;
import org.codehaus.jackson.map.ser.impl.PropertySerializerMap;
import org.codehaus.jackson.map.ser.std.ContainerSerializerBase;
import org.codehaus.jackson.node.ObjectNode;
import org.codehaus.jackson.type.JavaType;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Map;

/**
 * @author yuriyz on 02/09/2015.
 */
public class RpcMapSerializer extends ContainerSerializerBase<RpcMap> implements ResolvableSerializer {

    /**
     * Map-valued property being serialized with this instance
     *
     * @since 1.7
     */
    protected final BeanProperty _property;

    /**
     * Set of entries to omit during serialization, if any
     */
    protected final HashSet<String> _ignoredEntries;

    /**
     * Whether static types should be used for serialization of values
     * or not (if not, dynamic runtime type is used)
     */
    protected final boolean _valueTypeIsStatic;

    /**
     * Declared type of keys
     *
     * @since 1.7
     */
    protected final JavaType _keyType;

    /**
     * Declared type of contained values
     */
    protected final JavaType _valueType;

    /**
     * Key serializer to use, if it can be statically determined
     *
     * @since 1.7
     */
    protected JsonSerializer<Object> _keySerializer;

    /**
     * Value serializer to use, if it can be statically determined
     *
     * @since 1.5
     */
    protected JsonSerializer<Object> _valueSerializer;

    /**
     * Type identifier serializer used for values, if any.
     */
    protected final TypeSerializer _valueTypeSerializer;

    /**
     * If value type can not be statically determined, mapping from
     * runtime value types to serializers are stored in this object.
     *
     * @since 1.8
     */
    protected PropertySerializerMap _dynamicValueSerializers;

    public RpcMapSerializer() {
        this(null, null, null, false, null, null, null, null);
    }

    public RpcMapSerializer(HashSet<String> ignoredEntries,
                               JavaType keyType, JavaType valueType, boolean valueTypeIsStatic,
                               TypeSerializer vts,
                               JsonSerializer<Object> keySerializer, JsonSerializer<Object> valueSerializer,
                               BeanProperty property) {
        super(RpcMap.class, false);
        _property = property;
        _ignoredEntries = ignoredEntries;
        _keyType = keyType;
        _valueType = valueType;
        _valueTypeIsStatic = valueTypeIsStatic;
        _valueTypeSerializer = vts;
        _keySerializer = keySerializer;
        _valueSerializer = valueSerializer;
        _dynamicValueSerializers = PropertySerializerMap.emptyMap();
    }

    @Override
    public ContainerSerializerBase<?> _withValueTypeSerializer(TypeSerializer vts) {
        RpcMapSerializer ms = new RpcMapSerializer(_ignoredEntries, _keyType, _valueType, _valueTypeIsStatic, vts,
                _keySerializer, _valueSerializer, _property);
        if (_valueSerializer != null) {
            ms._valueSerializer = _valueSerializer;
        }
        return ms;
    }

    /*
    /**********************************************************
    /* JsonSerializer implementation
    /**********************************************************
     */

    @Override
    public void serialize(RpcMap rpcMap, JsonGenerator jgen, SerializerProvider provider) throws IOException {
        jgen.writeStartObject();
        if (!rpcMap.isEmpty()) {
            Map<String, Object> value = rpcMap.getTransientMap();
            if (_valueSerializer != null) {
                serializeFieldsUsing(value, jgen, provider, _valueSerializer);
            } else {
                serializeFields(value, jgen, provider);
            }
        }
        jgen.writeEndObject();
    }

    @Override
    public void serializeWithType(RpcMap rpcMap, JsonGenerator jgen, SerializerProvider provider,
                                  TypeSerializer typeSer)
            throws IOException {
        typeSer.writeTypePrefixForObject(rpcMap, jgen);
        if (!rpcMap.isEmpty()) {
            Map<String, Object> map = rpcMap.getTransientMap();
            if (_valueSerializer != null) {
                serializeFieldsUsing(map, jgen, provider, _valueSerializer);
            } else {
                serializeFields(map, jgen, provider);
            }
        }
        typeSer.writeTypeSuffixForObject(rpcMap, jgen);
    }

    /*
    /**********************************************************
    /* JsonSerializer implementation
    /**********************************************************
     */

    /**
     * Method called to serialize fields, when the value type is not statically known.
     */
    public void serializeFields(Map<?, ?> value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
        // If value type needs polymorphic type handling, some more work needed:
        if (_valueTypeSerializer != null) {
            serializeTypedFields(value, jgen, provider);
            return;
        }
        final JsonSerializer<Object> keySerializer = _keySerializer;

        final HashSet<String> ignored = _ignoredEntries;
        final boolean skipNulls = !provider.isEnabled(SerializationConfig.Feature.WRITE_NULL_MAP_VALUES);

        PropertySerializerMap serializers = _dynamicValueSerializers;

        for (Map.Entry<?, ?> entry : value.entrySet()) {
            Object valueElem = entry.getValue();
            // First, serialize key
            Object keyElem = entry.getKey();
            if (keyElem == null) {
                provider.getNullKeySerializer().serialize(null, jgen, provider);
            } else {
                // [JACKSON-314] skip entries with null values?
                if (skipNulls && valueElem == null) continue;
                // One twist: is entry ignorable? If so, skip
                if (ignored != null && ignored.contains(keyElem)) continue;
                keySerializer.serialize(keyElem, jgen, provider);
            }

            // And then value
            if (valueElem == null) {
                provider.defaultSerializeNull(jgen);
            } else {
                Class<?> cc = valueElem.getClass();
                JsonSerializer<Object> serializer = serializers.serializerFor(cc);
                if (serializer == null) {
                    if (_valueType.hasGenericTypes()) {
                        serializer = _findAndAddDynamic(serializers,
                                provider.constructSpecializedType(_valueType, cc), provider);
                    } else {
                        serializer = _findAndAddDynamic(serializers, cc, provider);
                    }
                    serializers = _dynamicValueSerializers;
                }
                try {
                    serializer.serialize(valueElem, jgen, provider);
                } catch (Exception e) {
                    // [JACKSON-55] Need to add reference information
                    String keyDesc = "" + keyElem;
                    wrapAndThrow(provider, e, value, keyDesc);
                }
            }
        }
    }

    /**
     * Method called to serialize fields, when the value type is statically known,
     * so that value serializer is passed and does not need to be fetched from
     * provider.
     */
    protected void serializeFieldsUsing(Map<?, ?> value, JsonGenerator jgen, SerializerProvider provider,
                                        JsonSerializer<Object> ser)
            throws IOException {
        final JsonSerializer<Object> keySerializer = _keySerializer;
        final HashSet<String> ignored = _ignoredEntries;
        final TypeSerializer typeSer = _valueTypeSerializer;
        final boolean skipNulls = !provider.isEnabled(SerializationConfig.Feature.WRITE_NULL_MAP_VALUES);

        for (Map.Entry<?, ?> entry : value.entrySet()) {
            Object valueElem = entry.getValue();
            Object keyElem = entry.getKey();
            if (keyElem == null) {
                provider.getNullKeySerializer().serialize(null, jgen, provider);
            } else {
                // [JACKSON-314] also may need to skip entries with null values
                if (skipNulls && valueElem == null) continue;
                if (ignored != null && ignored.contains(keyElem)) continue;
                keySerializer.serialize(keyElem, jgen, provider);
            }
            if (valueElem == null) {
                provider.defaultSerializeNull(jgen);
            } else {
                try {
                    if (typeSer == null) {
                        ser.serialize(valueElem, jgen, provider);
                    } else {
                        ser.serializeWithType(valueElem, jgen, provider, typeSer);
                    }
                } catch (Exception e) {
                    // [JACKSON-55] Need to add reference information
                    String keyDesc = "" + keyElem;
                    wrapAndThrow(provider, e, value, keyDesc);
                }
            }
        }
    }

    protected void serializeTypedFields(Map<?, ?> value, JsonGenerator jgen, SerializerProvider provider)
            throws IOException {
        final JsonSerializer<Object> keySerializer = _keySerializer;
        JsonSerializer<Object> prevValueSerializer = null;
        Class<?> prevValueClass = null;
        final HashSet<String> ignored = _ignoredEntries;
        final boolean skipNulls = !provider.isEnabled(SerializationConfig.Feature.WRITE_NULL_MAP_VALUES);

        for (Map.Entry<?, ?> entry : value.entrySet()) {
            Object valueElem = entry.getValue();
            // First, serialize key
            Object keyElem = entry.getKey();
            if (keyElem == null) {
                provider.getNullKeySerializer().serialize(null, jgen, provider);
            } else {
                // [JACKSON-314] also may need to skip entries with null values
                if (skipNulls && valueElem == null) continue;
                // One twist: is entry ignorable? If so, skip
                if (ignored != null && ignored.contains(keyElem)) continue;
                keySerializer.serialize(keyElem, jgen, provider);
            }

            // And then value
            if (valueElem == null) {
                provider.defaultSerializeNull(jgen);
            } else {
                Class<?> cc = valueElem.getClass();
                JsonSerializer<Object> currSerializer;
                if (cc == prevValueClass) {
                    currSerializer = prevValueSerializer;
                } else {
                    currSerializer = provider.findValueSerializer(cc, _property);
                    prevValueSerializer = currSerializer;
                    prevValueClass = cc;
                }
                try {
                    currSerializer.serializeWithType(valueElem, jgen, provider, _valueTypeSerializer);
                } catch (Exception e) {
                    // [JACKSON-55] Need to add reference information
                    String keyDesc = "" + keyElem;
                    wrapAndThrow(provider, e, value, keyDesc);
                }
            }
        }
    }

    @Override
    public JsonNode getSchema(SerializerProvider provider, Type typeHint) {
        ObjectNode o = createSchemaNode("object", true);
        //(ryan) even though it's possible to statically determine the "value" type of the map,
        // there's no way to statically determine the keys, so the "Entries" can't be determined.
        return o;
    }

    /**
     * Need to get callback to resolve value serializer, if static typing
     * is used (either being forced, or because value type is final)
     */
    @Override
    public void resolve(SerializerProvider provider)
            throws JsonMappingException {
        if (_valueTypeIsStatic && _valueSerializer == null) {
            _valueSerializer = provider.findValueSerializer(_valueType, _property);
        }
        /* 10-Dec-2010, tatu: Let's also fetch key serializer; and always assume we'll
         *   do that just by using static type information
         */
        /* 25-Feb-2011, tatu: May need to reconsider this static checking (since it
         *   differs from value handling)... but for now, it's ok to ensure contextual
         *   aspects are handled; this is done by provider.
         */
        if (_keySerializer == null) {
            _keySerializer = provider.findKeySerializer(_keyType, _property);
        }
    }

    /*
    /**********************************************************
    /* Internal methods
    /**********************************************************
     */

    protected final JsonSerializer<Object> _findAndAddDynamic(PropertySerializerMap map,
                                                              Class<?> type, SerializerProvider provider) throws JsonMappingException {
        PropertySerializerMap.SerializerAndMapResult result = map.findAndAddSerializer(type, provider, _property);
        // did we get a new map of serializers? If so, start using it
        if (map != result.map) {
            _dynamicValueSerializers = result.map;
        }
        return result.serializer;
    }

    protected final JsonSerializer<Object> _findAndAddDynamic(PropertySerializerMap map,
                                                              JavaType type, SerializerProvider provider) throws JsonMappingException {
        PropertySerializerMap.SerializerAndMapResult result = map.findAndAddSerializer(type, provider, _property);
        if (map != result.map) {
            _dynamicValueSerializers = result.map;
        }
        return result.serializer;
    }

}
