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
package org.activityinfo.analysis;

import org.activityinfo.model.formula.SourceRange;
import org.activityinfo.store.query.shared.NodeMatch;

public class FieldReference {
    private SourceRange sourceRange;
    private NodeMatch match;
    private String description;

    public FieldReference(SourceRange sourceRange, NodeMatch match) {
        this.sourceRange = sourceRange;
        this.match = match;
    }

    public SourceRange getSourceRange() {
        return sourceRange;
    }

    public String getDescription() {
        if(match.isEnumBoolean()) {
            return match.getFieldNode().getField().getLabel() + " is " + match.getEnumItem().getLabel();
        } else {
            return match.getFieldNode().getField().getLabel();
        }
    }

    public NodeMatch getMatch() {
        return match;
    }
}
