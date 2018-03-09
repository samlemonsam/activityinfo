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
package org.activityinfo.io.xform.manifest;

import javax.xml.bind.annotation.XmlElement;
import java.net.URI;

public class MediaFile {

    private String filename;
    private String hash;
    private String downloadUrl;

    /**
     * The unique un-rooted file path for this media file.
     * This un-rooted path must not start with a drive name or slash and must not contain relative path
     * navigations (e.g., . or ..).
     */
    @XmlElement
    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    /**
     * contains the hash value of the media file available for download. The only hash values currently
     * supported are MD5 hashes of the file contents; they are prefixed by md5:.
     * If the hash value identified in the manifest differs from the hash value for a previously-downloaded media file,
     * then the file should be re-fetched from the server.
     */
    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    /**
     * is a fully formed URL for downloading the media file to the device.
     * It may be a valid http or https URL of any structure; the server may require authentication;
     * the server may require a secure (https) channel, etc.
     *
     */
    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public void setDownloadUrl(URI manifest) {
        setDownloadUrl(manifest.toString());
    }
}
