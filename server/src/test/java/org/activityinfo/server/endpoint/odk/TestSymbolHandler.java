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
package org.activityinfo.server.endpoint.odk;

import com.google.common.base.Preconditions;
import org.activityinfo.io.xform.xpath.XSymbolException;
import org.activityinfo.io.xform.xpath.XSymbolHandler;
import org.activityinfo.model.legacy.CuidAdapter;
import org.activityinfo.model.resource.ResourceId;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class TestSymbolHandler implements XSymbolHandler {

    private Map<String,String> symbolMap = new HashMap<>();

    public TestSymbolHandler(List<ResourceId> fieldIds) {
        Iterator<ResourceId> it = fieldIds.listIterator();
        while (it.hasNext()) {
            ResourceId id = it.next();
            Character domain = id.getDomain();
            if (domain.equals(CuidAdapter.ATTRIBUTE_DOMAIN)) {
                symbolMap.put(id.asString(), quote(id.asString()));
            } else {
                symbolMap.put(id.asString(), id.asString());
            }
        }
    }

    @Override
    public String resolveSymbol(String symbol) throws XSymbolException {
        Preconditions.checkArgument(symbol != null, "Symbol cannot be null.");
        String resolved = symbolMap.get(symbol);
        if (resolved == null) {
            throw new XSymbolException(symbol);
        }
        return resolved;
    }

    private String quote(String string) {
        return "'" + string + "'";
    }

}
