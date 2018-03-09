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
package org.activityinfo.test.driver.mail.mailinator;

import com.google.common.collect.Maps;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
class MessagePart {
    
    private final Map<String, String> headers = Maps.newHashMap();
    private String body;

    public Map<String, String> getHeaders() {
        return headers;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
    
    public String getContentType() {
        String type = getHeader("Content-Type");
        String parts[] = type.split(";");
        return parts[0];
    }
    
    public String getHeader(String headerName) {
        for (Map.Entry<String, String> header : headers.entrySet()) {
            if(header.getKey().equalsIgnoreCase(headerName)) {
                return header.getValue();
            }
        }
        throw new IllegalStateException("No such header: " + headerName);
    }
}
