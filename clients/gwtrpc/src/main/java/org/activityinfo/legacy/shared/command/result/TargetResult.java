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
package org.activityinfo.legacy.shared.command.result;

import org.activityinfo.legacy.shared.model.TargetDTO;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonMethod;
import org.codehaus.jackson.annotate.JsonValue;

import java.util.Arrays;
import java.util.List;

@JsonAutoDetect(JsonMethod.NONE)
public class TargetResult extends ListResult<TargetDTO> implements CommandResult {

    public TargetResult() {

    }

    public TargetResult(List<TargetDTO> targets) {
        super(targets);
    }

    public TargetResult(TargetDTO... targets) {
        super(Arrays.asList(targets));
    }
    
    @Override
    @JsonValue
    public List<TargetDTO> getData() {
        return super.getData();
    }
}
