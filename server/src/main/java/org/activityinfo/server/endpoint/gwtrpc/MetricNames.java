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
package org.activityinfo.server.endpoint.gwtrpc;

import org.activityinfo.legacy.shared.command.BatchCommand;
import org.activityinfo.legacy.shared.command.Command;


public class MetricNames {
    
    public static String commandNameMetricName(Class<? extends Command> commandClass) {
        if(commandClass.equals(BatchCommand.class)) {
            return "batch";
        }
        
        String name = commandClass.getSimpleName();

        StringBuilder metricName = new StringBuilder();
        int i = 0;
        metricName.append(Character.toLowerCase(name.charAt(i++)));
        while(i < name.length()) {
            char c = name.charAt(i);
            if(Character.isUpperCase(c)) {
                metricName.append('.').append(Character.toLowerCase(c));
            } else {
                metricName.append(c);
            }
            i++;
        }
        return metricName.toString();
    }
}
