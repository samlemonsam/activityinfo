package org.activityinfo.legacy.shared.adapter;
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

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.activityinfo.model.form.FormInstance;

import java.util.List;

/**
 * @author yuriyz on 03/12/2015.
 */
public class ListAdapter<T> implements Function<List<T>, List<FormInstance>> {

    private final Function<T, FormInstance> instanceAdapter;

    public ListAdapter(Function<T, FormInstance> instanceAdapter) {
        this.instanceAdapter = instanceAdapter;
    }

    @Override
    public List<FormInstance> apply(List<T> input) {
        return Lists.transform(input, instanceAdapter);
    }
}