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
package org.activityinfo.store.migrate;

import com.google.appengine.tools.mapreduce.Input;
import com.google.appengine.tools.mapreduce.InputReader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Set of all ids in the user database table.
 */
public class DatabaseInput extends Input<Integer> {
    @Override
    public List<? extends InputReader<Integer>> createReaders() throws IOException {
        List<InputReader<Integer>> readers = new ArrayList<>();
        readers.add(new DatabaseReader());
        return readers;
    }

}
