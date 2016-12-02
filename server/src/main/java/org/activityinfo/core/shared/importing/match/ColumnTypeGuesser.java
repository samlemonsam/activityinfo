package org.activityinfo.core.shared.importing.match;
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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.activityinfo.core.shared.type.converter.FieldParserFactory;
import org.activityinfo.core.shared.type.converter.FieldValueParser;
import org.activityinfo.model.form.FormFieldType;
import org.activityinfo.model.type.FieldTypeClass;
import org.activityinfo.model.type.primitive.BooleanType;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Guesser use converters to guess column type.
 * <p/>
 * Note: If we can NOT convert value to particular type then we
 * don't have other choice then consider it as string.
 *
 * @author yuriyz on 5/9/14.
 */
public class ColumnTypeGuesser {

    private final Map<FieldTypeClass, Integer> typeMap = newFieldMap();

    private final List<String> columnValues;
    private final FieldParserFactory converterFactory;

    public ColumnTypeGuesser(List<String> columnValues, FieldParserFactory converterFactory) {
        this.columnValues = columnValues;
        this.converterFactory = converterFactory;
    }

    public FieldTypeClass guessType() {
        calculateTypeScores();

        final List<Map.Entry<FieldTypeClass, Integer>> copyEntrySet = Lists.newArrayList(typeMap.entrySet());
        Collections.sort(copyEntrySet, new Comparator<Map.Entry<FieldTypeClass, Integer>>() {
            @Override
            public int compare(Map.Entry<FieldTypeClass, Integer> o1, Map.Entry<FieldTypeClass, Integer> o2) {
                return o1.getValue().compareTo(o2.getValue());
            }
        });
        return copyEntrySet.get(copyEntrySet.size() - 1).getKey();
    }

    private void calculateTypeScores() {
        for (String value : columnValues) {
            final Map<FieldTypeClass, Integer> copyMap = Maps.newHashMap(typeMap);

            // we don't need to iterate over string types because input is always string
            copyMap.remove(FieldTypeClass.FREE_TEXT);
            copyMap.remove(FieldTypeClass.NARRATIVE);

            boolean hasMatch = false;
            for (Map.Entry<FieldTypeClass, Integer> entry : copyMap.entrySet()) {
                try {

                    if (isBoolean(value) && entry.getKey().equals(BooleanType.TYPE_CLASS)) {
                        increaseValue(entry.getKey());
                        hasMatch = true;
                        break;
                    }

                    final FieldValueParser stringConverter = converterFactory.createStringConverter(entry.getKey().createType());
                    final Object convertedValue = stringConverter.convert(value);

                    // analyze converted value
                    if (convertedValue != null) {
                        increaseValue(entry.getKey());
                        hasMatch = true;
                        break;
                    }
                } catch (Exception e) {
                    // ignore
                }
            }

            // if no match then we fallback to string type
            if (!hasMatch) {
                final int length = value.length();
                if (length < FormFieldType.FREE_TEXT_LENGTH) {
                    increaseValue(FieldTypeClass.FREE_TEXT);
                } else {
                    increaseValue(FieldTypeClass.NARRATIVE);
                }
            }
        }
    }

    public static boolean isBoolean(String value) {
        return value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false");
    }

    private void increaseValue(FieldTypeClass type) {
        typeMap.put(type, typeMap.get(type) + 1);
    }

    private static Map<FieldTypeClass, Integer> newFieldMap() {
        Map<FieldTypeClass, Integer> typeMap = Maps.newHashMap();
        for (FieldTypeClass type : FormFieldType.values()) {
            typeMap.put(type, 0);
        }
        return typeMap;
    }

}
