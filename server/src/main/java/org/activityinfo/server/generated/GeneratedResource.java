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
package org.activityinfo.server.generated;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.OutputStream;

public interface GeneratedResource {
    
    public String getId();

    /**
     * @return true if the resource has been generated and is ready to read.
     */
    public boolean isComplete();

    /**
     * @return the publicly-accessible URL for this temporary resource
     */
    public String getDownloadUri();

    /**
     * @return the output stream to which the contents of the file should be
     * written. The stream MUST be closed by the caller.
     */
    public OutputStream openOutputStream() throws IOException;
    
    public void updateProgress(double percentageComplete);
    
    public double getProgress();
    

    /**
     * Serves this resource's content
     */
    Response serve() throws IOException;
}
