package org.activityinfo.model.expr.functions;
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

import com.google.common.collect.Sets;
import org.activityinfo.model.resource.ResourceId;
import org.activityinfo.model.type.FieldType;
import org.activityinfo.model.type.FieldValue;
import org.activityinfo.model.type.NullFieldValue;
import org.activityinfo.model.type.primitive.BooleanFieldValue;
import org.activityinfo.model.type.primitive.BooleanType;

import java.util.List;
import java.util.Set;

/**
 * @author yuriyz on 9/3/14.
 */
public class ContainsAllFunction extends ExprFunction {

    public static final ContainsAllFunction INSTANCE = new ContainsAllFunction();

    private ContainsAllFunction() {
    }

    @Override
    public String getId() {
        return "containsAll";
    }

    @Override
    public String getLabel() {
        return "Includes All";
    }

    @Override
    public BooleanFieldValue apply(List<FieldValue> arguments) {
        if (arguments.get(0).equals(NullFieldValue.INSTANCE)) {
            return BooleanFieldValue.FALSE;
        }

        Set<ResourceId> arg1 = Sets.newHashSet(Casting.toSet(arguments.get(0)));
        for (int i = 1; i < arguments.size(); i++) {
            arg1.removeAll(Casting.toSet(arguments.get(i)));
        }

        return BooleanFieldValue.valueOf(arg1.isEmpty());
    }

    @Override
    public FieldType resolveResultType(List<FieldType> argumentTypes) {
        return BooleanType.INSTANCE;
    }
}
