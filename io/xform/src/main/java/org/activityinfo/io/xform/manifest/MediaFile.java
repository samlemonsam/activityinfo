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
