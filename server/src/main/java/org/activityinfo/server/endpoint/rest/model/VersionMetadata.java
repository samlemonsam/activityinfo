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
package org.activityinfo.server.endpoint.rest.model;

public class VersionMetadata {
    private String message;
    private String sourceFilename;
    private String sourceUrl;
    private String sourceMD5;
    private String sourceMetadata;

    /**
     * @return a message describing the change
     */
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * @return The filename of the source file
     */
    public String getSourceFilename() {
        return sourceFilename;
    }

    public void setSourceFilename(String sourceFilename) {
        this.sourceFilename = sourceFilename;
    }

    /**
     * @return the URL of the source file
     */
    public String getSourceUrl() {
        return sourceUrl;
    }

    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    /**
     * @return the MD5 hash of the source file
     */
    public String getSourceMD5() {
        return sourceMD5;
    }

    public void setSourceMD5(String sourceMD5) {
        this.sourceMD5 = sourceMD5;
    }

    public String getSourceMetadata() {
        return sourceMetadata;
    }

    public void setSourceMetadata(String sourceMetadata) {
        this.sourceMetadata = sourceMetadata;
    }

}
