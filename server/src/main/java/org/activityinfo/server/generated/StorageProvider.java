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

import java.io.IOException;

public interface StorageProvider {

    /**
     * Creates a web-accessible temporary file
     *
     * @param mimeType the mime type of the file
     * @param filename The name of the user-facing file
     * @return
     * @throws IOException
     */
    GeneratedResource create(String mimeType, String filename) throws IOException;

    /**
     * Provides a handle for a previously created {@code GeneratedResource}
     * 
     * @param id the id of the  {@code GeneratedResource}
     * @return a handle to the  {@code GeneratedResource} identified by the given {@code id}
     * @throws java.lang.IllegalArgumentException if a {@code GeneratedResource} with given {@code id}
     * could not be found.
     */
    GeneratedResource get(String id);


}
